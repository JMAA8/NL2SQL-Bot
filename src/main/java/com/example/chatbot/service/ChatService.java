package com.example.chatbot.service;

import com.example.chatbot.entityMongoDB.Chat;
import com.example.chatbot.entityMongoDB.ChatMessage;
import com.example.chatbot.repository.ChatMessageRepository;
import com.example.chatbot.repository.ChatRepository;
import com.example.chatbot.unidb.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import com.example.chatbot.llm.LLMService;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

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

    // NEU:
    @Inject SchemaPruner schemaPruner;
    @Inject RbacValidator rbacValidator;

    @Transactional
    public Chat handleChatMessage(Long userId, String chatId, String prompt) {
        long t0 = System.nanoTime(); // E2E-Startzeit messen

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
                // 1) Benchmark-Run sicherstellen
                runState.ensureRun(bench, "WebApp Baseline", "v9");
                int testNo = runState.nextTestNo();

                // 2) Schema-Pruning (rollen- und frageabhängig)
                String role = currentUserRole(); // TODO: aus Security-Kontext ziehen
                var tables = schemaPruner.filterByRole(schemaPruner.suggestTables(prompt), role);
                String schemaSnippet = schemaPruner.buildSchemaSnippet(tables);

                // 3) NL->SQL (eine Variante; optional: generateCandidates+Ranking)
                String gen = nl2sql.generateSql(prompt, schemaSnippet);

                // 4) Frage-Mapping & E2E
                Integer qno = bench.findQuestionNoByExactText(prompt); // kann null sein
                double e2eMs = (System.nanoTime() - t0) / 1_000_000.0;

                // 5) Vorab-Log (legt evaluation_result an)
                bench.evalNoActor(runState.getRunId(), (qno != null ? qno : 0), testNo, prompt, gen, e2eMs);

                // 6) Guards (ReadOnly + RBAC)
                boolean isReadOnly = unidb.isSelectOnly(gen);   // ACHTUNG: Methode in UnidbReadRepo public machen
                var rbac = rbacValidator.check(role, gen);
                String violJson = toJsonArray(rbac.violations);

                if (!isReadOnly) {
                    // BLOCK → nur Flags loggen
                    String compAccJson = bench.buildClauseFlagsJson(gen);
                    bench.updateAfterExec(
                            runState.getRunId(), testNo,
                            bench.normalizeSql(gen),
                            /*is_read_only*/ false,
                            rbac.ok, violJson,
                            /*exec_ok*/ null, /*exec_error*/ null,
                            /*exec_ms*/ null, /*row_count*/ null, /*result_hash*/ null,
                            /*exact_match*/ null, /*exec_accuracy*/ null,
                            compAccJson
                    );
                    finalAnswer = "BLOCK (kein SELECT).";
                } else {
                    String effectiveSql = rbac.ok ? gen : "SELECT COUNT(*) AS cnt FROM (" + gen + ") x";

                    // 7) Ausführen & messen
                    UnidbReadRepo.QueryRun run = unidb.runAndHashSelect(effectiveSql, 25);

                    // 8) Gold-Referenz (falls vorhanden)
                    String refSql = (qno != null) ? bench.fetchRefSqlByQuestionNo(qno) : null;

                    Boolean exactMatch = null, execAcc = null;
                    String compAccJson = bench.buildClauseFlagsJson(effectiveSql);

                    if (run.execOk && refSql != null && unidb.isSelectOnly(refSql)) {
                        exactMatch = bench.sqlEqualsNormalized(effectiveSql, refSql);
                        var ref = unidb.hashOnly(refSql); // nur Hash/Count
                        execAcc = (ref.execOk && ref.resultHash != null && ref.resultHash.equals(run.resultHash));
                        compAccJson = bench.mergeCompAccWithJaccard(compAccJson, effectiveSql, refSql);
                    }

                    // 9) Update Log
                    bench.updateAfterExec(
                            runState.getRunId(), testNo,
                            bench.normalizeSql(effectiveSql),
                            /*is_read_only*/ true,
                            rbac.ok, violJson,
                            run.execOk, run.execError, run.execMs, run.rowCount, run.resultHash,
                            exactMatch, execAcc, compAccJson
                    );

                    // 10) Semantiklabels anhängen
                    List<String> semErrs = semanticAnalyzer.detect(effectiveSql, prompt);
                    if (!semErrs.isEmpty()) bench.appendSemanticErrors(runState.getRunId(), testNo, semErrs);

                    // 11) Antwort
                    finalAnswer = run.preview; // hübsche Markdown-Tabelle oder _(keine Zeilen)_
                }
            } else {
                // Plain-LLM/Dokumente
                finalAnswer = llmService.getResponse(prompt);
            }
        } catch (Exception e) {
            finalAnswer = "Fehler: " + e.getMessage();
        }

        // Chatnachricht persistieren
        ChatMessage msg = new ChatMessage(chat.getChatId(), userId, prompt, finalAnswer);
        chatMessageRepository.persistMessage(msg);
        chat.setMessages(chatMessageRepository.findMessagesByChatId(chat.getChatId()));
        chatRepository.update(chat);
        return chat;
    }

    private String currentUserRole() {
        // TODO: aus Security-Kontext. Bis dahin Standard:
        return "Student";
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

    // Route-Heuristik
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
