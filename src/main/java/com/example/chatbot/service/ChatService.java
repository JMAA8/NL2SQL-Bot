package com.example.chatbot.service;

import com.example.chatbot.entityMongoDB.Chat;
import com.example.chatbot.entityMongoDB.ChatMessage;
import com.example.chatbot.repository.ChatMessageRepository;
import com.example.chatbot.repository.ChatRepository;
import com.example.chatbot.unidb.*;
import com.example.chatbot.unidb.RbacValidator.IdentityContext;
import com.example.chatbot.llm.LLMService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class ChatService {

    @Inject ChatRepository chatRepository;
    @Inject ChatMessageRepository chatMessageRepository;
    @Inject LLMService llmService;

    @Inject NL2SQLService nl2sql;
    @Inject BenchRepo bench;
    @Inject UnidbReadRepo unidb;
    @Inject RunState runState;
    @Inject SemanticAnalyzer semanticAnalyzer;

    // RBAC/Schema & JWT/Identity
    @Inject SchemaPruner schemaPruner;
    @Inject RbacValidator rbacValidator;
    @Inject JwtRoleService jwtRoleService;
    @Inject IdentityService identityService;
    @Inject SqlLinter sqlLinter;

    @Transactional
    public Chat handleChatMessage(Long userId, String chatId, String prompt) {
        long t0 = System.nanoTime();

        Chat chat = (chatId != null) ? chatRepository.findByChatId(chatId) : null;
        if (chat == null) {
            chat = new Chat();
            chat.setUserId(userId);
            chat.setTitle((prompt == null || prompt.isBlank()) ? "NeuerChat" : prompt.split("\\s+")[0]);
            chat.setCreatedAt(Instant.now());
            chatRepository.persistChat(chat);
        }

        String finalAnswer;
        try {
            if ("db".equals(simpleRoute(prompt))) {
                runState.ensureRun(bench, "WebApp Optimization", "v9"); // neue Pipeline-Version
                int testNo = runState.nextTestNo();

                // (A) Rolle + Identity
                AppRole appRole = jwtRoleService.getCurrentAppRole();
                Long benutzerId = jwtRoleService.getCurrentUserId();
                IdentityService.IdentityContext resolved = identityService.resolveByBenutzerId(benutzerId, appRole);

                // RbacValidator.IdentityContext aus resolved ableiten
                RbacValidator.IdentityContext idCtx = new RbacValidator.IdentityContext(
                        resolved == null ? null : resolved.benutzerId,
                        resolved == null ? null : resolved.studentId,
                        resolved == null ? null : resolved.professorId
                );

                // (B) Schema-Pruning
                var tables = schemaPruner.filterByRole(schemaPruner.suggestTables(prompt), appRole);
                String schemaSnippet = schemaPruner.buildSchemaSnippet(tables, appRole);

                // (C1) NL→SQL
                String gen = nl2sql.generateSql(prompt, schemaSnippet, resolved, appRole);

                // Sofort sanitisieren
                gen = sqlLinter.stripTrailingSemicolons(gen);
                gen = sqlLinter.normalizeWhitespace(gen);

                // (C2) Lint + Auto-Repair (nur einmal)
                var lintIssues = sqlLinter.lint(gen);
                if (!lintIssues.isEmpty()) {
                    String hint = sqlLinter.buildHint(lintIssues);
                    String repaired = nl2sql.repairWithError(prompt, schemaSnippet, gen, hint);
                    if (repaired != null && !repaired.isBlank()) {
                        repaired = sqlLinter.extractFirstSelect(repaired); // <— NEU: nur die SELECT-Zeile
                        repaired = sqlLinter.stripTrailingSemicolons(repaired);
                        repaired = sqlLinter.normalizeWhitespace(repaired);
                        if (unidb.isSelectOnly(repaired)) gen = repaired;
                    }
                }

                 // (C2b) NEU: auch ohne LLM-Repair sauber nur SELECT herausschneiden
                gen = sqlLinter.extractFirstSelect(gen);

                // (C2c) NEU: Platzhalter-IDs gegen echte ID tauschen
                gen = sqlLinter.substituteStudentIdPlaceholders(gen, resolved == null ? null : resolved.studentId);

                // (C3) NEU: Self-Scope deterministisch injizieren (kein weiterer LLM-Call!)
                if (appRole == AppRole.BASIC_USER) {
                    gen = sqlLinter.injectSelfPredicateIfNeeded(gen, resolved == null ? null : resolved.studentId);
                }
                // (D) Vorab-Log
                Integer qno = bench.findQuestionNoByExactText(prompt);
                double e2eMs = (System.nanoTime() - t0) / 1_000_000.0;
                bench.evalNoActor(runState.getRunId(), (qno != null ? qno : 0), testNo, prompt, gen, e2eMs);

                // (E) Guards: ReadOnly + RBAC
                boolean isReadOnly = unidb.isSelectOnly(gen);
                var rbac = rbacValidator.check(appRole, idCtx, gen);
                String violJson = toJsonArray(rbac.violations);

                if (!isReadOnly) {
                    String compAccJson = bench.buildClauseFlagsJson(gen);
                    bench.updateAfterExec(runState.getRunId(), testNo, bench.normalizeSql(gen),
                            false, rbac.ok, violJson,
                            null, null, null, null, null,
                            null, null, compAccJson);
                    finalAnswer = "BLOCK (kein SELECT).";
                } else {
                    // Degradierung: bei RBAC-Verletzung aggregieren
                    String effectiveSql = rbac.ok ? gen
                            : (gen.toLowerCase().contains("count(") ? gen : "SELECT COUNT(*) AS cnt FROM (" + gen + ") x");

                    var run = unidb.runAndHashSelect(effectiveSql, 25);

                    // (F) Defensiver Repair nach DB-Fehler
                    if (!run.execOk && run.execError != null) {
                        // 1) Semikolons nochmal hart entfernen & retry
                        String eff = sqlLinter.stripTrailingSemicolons(effectiveSql);
                        eff = sqlLinter.normalizeWhitespace(eff);
                        if (!eff.equals(effectiveSql) && unidb.isSelectOnly(eff)) {
                            effectiveSql = eff;
                            run = unidb.runAndHashSelect(effectiveSql, 25);
                        }
                        // 2) Falls weiter Fehler: LLM-Repair mit echter DB-Fehlermeldung
                        if (!run.execOk) {
                            String repaired = nl2sql.repairWithError(prompt, schemaSnippet, effectiveSql, run.execError);
                            if (repaired != null && !repaired.isBlank()) {
                                repaired = sqlLinter.stripTrailingSemicolons(repaired);
                                repaired = sqlLinter.normalizeWhitespace(repaired);
                                if (unidb.isSelectOnly(repaired)) {
                                    effectiveSql = repaired;
                                    run = unidb.runAndHashSelect(effectiveSql, 25);
                                }
                            }
                        }
                    }

                    // Gold-Vergleich (falls vorhanden)
                    String refSql = (qno != null) ? bench.fetchRefSqlByQuestionNo(qno) : null;
                    Boolean exactMatch = null, execAcc = null;
                    String compAccJson = bench.buildClauseFlagsJson(effectiveSql);
                    if (run.execOk && refSql != null && unidb.isSelectOnly(refSql)) {
                        exactMatch = bench.sqlEqualsNormalized(effectiveSql, refSql);
                        var ref = unidb.hashOnly(refSql);
                        execAcc = (ref.execOk && ref.resultHash != null && ref.resultHash.equals(run.resultHash));
                        compAccJson = bench.mergeCompAccWithJaccard(compAccJson, effectiveSql, refSql);
                    }

                    bench.updateAfterExec(runState.getRunId(), testNo, bench.normalizeSql(effectiveSql),
                            true, rbac.ok, violJson,
                            run.execOk, run.execError, run.execMs, run.rowCount, run.resultHash,
                            exactMatch, execAcc, compAccJson);

                    var semErrs = semanticAnalyzer.detect(effectiveSql, prompt);
                    if (!semErrs.isEmpty()) bench.appendSemanticErrors(runState.getRunId(), testNo, semErrs);

                    finalAnswer = run.preview;
                }
            } else {
                finalAnswer = llmService.getResponse(prompt);
            }
        } catch (Exception e) {
            finalAnswer = "Fehler: " + e.getMessage();
        }

        ChatMessage msg = new ChatMessage(chat.getChatId(), userId, prompt, finalAnswer);
        chatMessageRepository.persistMessage(msg);
        chat.setMessages(chatMessageRepository.findMessagesByChatId(chat.getChatId()));
        chatRepository.update(chat);
        return chat;
    }

    private String toJsonArray(List<String> xs) {
        if (xs == null || xs.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < xs.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('"').append(xs.get(i).replace("\"", "\\\"")).append('"');
        }
        sb.append(']');
        return sb.toString();
    }

    private static final Pattern DB_HINTS = Pattern.compile(
            ".*\\b(" +
                    "kurs|kurse|kursen|kursname|kursnamen|datenbanken|ects|semester|ss\\d{4}|ws\\d{4}|" +
                    "pr(ü|u)fung|pr(ü|u)fungen|pruefung|pruefungen|pr(ü|u)fungsdatum|pruefungsdatum|" +
                    "student|studenten|studierende|studierenden|" +
                    "professor|professoren|dozent|dozenten|" +
                    "belegung|belegungen|belegt|kursbelegung|kursbelegungen|" +
                    "note|noten|durchschnittsnote|schnittnote|offen|keine\\s+note|" +
                    "anmeldung|anmeldungen|angemeldet|" +
                    "benutzer|nutzer|rolle|rollen|rechte|schema|" +
                    "raum|räume|raeume|" +
                    "anzahl|top-?\\s*\\d+|meisten" +
                    ")\\b.*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private String simpleRoute(String p) {
        String s = (p == null) ? "" : p;
        return DB_HINTS.matcher(s).matches() ? "db" : "docs";
    }

    // --- kleine Helfer für Self-Scope-Check ---
    private boolean usesStudentScopeTables(String sql) {
        if (sql == null) return false;
        String s = sql.toLowerCase();
        return s.contains(" studenten") || s.contains(" kursbelegung") || s.contains(" anmeldung_pruefung")
                || s.contains(" studenten ") || s.contains(" kursbelegung ") || s.contains(" anmeldung_pruefung ");
    }

    private boolean hasSelfPredicate(String sql, Long studentId) {
        if (sql == null || studentId == null) return false;
        String needle = "student_id = " + studentId;
        return sql.toLowerCase().contains(needle.toLowerCase());
    }

    private String sanitizeStudentIdPlaceholders(String sql, Long studentId) {
        if (sql == null || studentId == null) return sql;
        String[] tokens = {
                "eigene id","deine id","meine id",
                "your_student_id","my_student_id","student_self_id"
        };
        for (String t : tokens) {
            // 'token' → 123
            sql = sql.replaceAll("(?i)'\\s*" + Pattern.quote(t) + "\\s*'", String.valueOf(studentId));
            // token   → 123
            sql = sql.replaceAll("(?i)\\b" + Pattern.quote(t) + "\\b", String.valueOf(studentId));
        }
        return sql;
    }

    // --- Zusatz-APIs (unverändert)
    @Transactional
    public Chat updateChatTitle(String chatId, String newTitle) {
        Chat chat = chatRepository.findByChatId(chatId);
        if (chat != null) {
            chat.setTitle(newTitle);
            chatRepository.update(chat);
        }
        return chat;
    }

    @Transactional
    public void deleteChat(String chatId) {
        Chat chat = chatRepository.findByChatId(chatId);
        if (chat != null) {
            List<ChatMessage> messages = chatMessageRepository.findMessagesByChatId(chatId);
            for (ChatMessage message : messages) chatMessageRepository.delete(message);
            chatRepository.delete(chat);
        }
    }

    @Transactional
    public List<Chat> getChatsByUserId(Long userId) {
        return chatRepository.findByUserId(userId);
    }

    @Transactional
    public List<ChatMessage> getMessagesByChatId(String chatId) {
        return chatMessageRepository.findMessagesByChatId(chatId);
    }
}




