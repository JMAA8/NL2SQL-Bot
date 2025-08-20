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

    // RBAC/Schema & JWT/Identity
    @Inject SchemaPruner schemaPruner;
    @Inject RbacValidator rbacValidator;
    @Inject JwtRoleService jwtRoleService;
    @Inject IdentityService identityService;

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
                runState.ensureRun(bench, "WebApp Optimization", "v1");
                int testNo = runState.nextTestNo();

                // (A) JWT → AppRole + benutzer_id → IdentityContext
                AppRole appRole = jwtRoleService.getCurrentAppRole();
                Long benutzerId = jwtRoleService.getCurrentUserId();
                IdentityService.IdentityContext idCtx = identityService.resolveByBenutzerId(benutzerId, appRole);

                // (B) Schema-Pruning (rollen-/frageabhängig)
                var tables = schemaPruner.filterByRole(schemaPruner.suggestTables(prompt), appRole);
                String schemaSnippet = schemaPruner.buildSchemaSnippet(tables, appRole);

                // (C) NL→SQL mit Identitäts-/RLS-Hinweisen (eine Zeile, read-only)
                String gen = nl2sql.generateSql(prompt, schemaSnippet, idCtx, appRole);

                // (D) Logging (Vorab)
                Integer qno = bench.findQuestionNoByExactText(prompt);
                double e2eMs = (System.nanoTime() - t0) / 1_000_000.0;
                bench.evalNoActor(runState.getRunId(), (qno != null ? qno : 0), testNo, prompt, gen, e2eMs);

                // (E) Guards
                boolean isReadOnly = unidb.isSelectOnly(gen);
                var rbac = rbacValidator.check(appRole, gen);
                String violJson = toJsonArray(rbac.violations);

                if (!isReadOnly) {
                    String compAccJson = bench.buildClauseFlagsJson(gen);
                    bench.updateAfterExec(runState.getRunId(), testNo, bench.normalizeSql(gen),
                            false, rbac.ok, violJson,
                            null, null, null, null, null,
                            null, null, compAccJson);
                    finalAnswer = "BLOCK (kein SELECT).";
                } else {
                    String effectiveSql = rbac.ok ? gen : "SELECT COUNT(*) AS cnt FROM (" + gen + ") x";

                    var run = unidb.runAndHashSelect(effectiveSql, 25);

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

                    List<String> semErrs = semanticAnalyzer.detect(effectiveSql, prompt);
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
