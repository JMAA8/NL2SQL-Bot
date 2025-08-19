package com.example.chatbot.service;

import com.example.chatbot.entityMongoDB.Chat;
import com.example.chatbot.entityMongoDB.ChatMessage;
import com.example.chatbot.repository.ChatMessageRepository;
import com.example.chatbot.repository.ChatRepository;
import com.example.chatbot.unidb.NL2SQLService;
import com.example.chatbot.unidb.BenchRepo;
import com.example.chatbot.unidb.UnidbReadRepo;
import com.example.chatbot.unidb.RunState;
import com.example.chatbot.unidb.SemanticAnalyzer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import com.example.chatbot.llm.LLMService;
import java.time.Instant;
import java.util.regex.Pattern;




import java.util.List;

@ApplicationScoped
public class ChatService {

    @Inject
    ChatRepository chatRepository;

    @Inject
    ChatMessageRepository chatMessageRepository;

    @Inject
    LLMService llmService;

    @Inject
    NL2SQLService nl2sql;

    @Inject
    BenchRepo bench;

    @Inject
    UnidbReadRepo unidb;

    @Inject
    RunState runState;

    @Inject
    SemanticAnalyzer semanticAnalyzer;

    // Nachricht speichern und Chat verwalten
    @Transactional
    public Chat handleChatMessage(Long userId, String chatId, String prompt) {
        long t0 = System.nanoTime(); // E2E-Startzeit messen

        Chat chat = (chatId != null) ? chatRepository.findByChatId(chatId) : null;
        if (chat == null) {
            chat = new Chat();
            chat.setUserId(userId);
            chat.setTitle(prompt.split("\\s+")[0]);
            chat.setCreatedAt(Instant.now());
            chatRepository.persistChat(chat);
        }

        String route = simpleRoute(prompt); // "db" | "docs"
        String finalAnswer;

        try {
            if ("db".equals(route)) {
                // 1) Benchmark-Run sicherstellen
                runState.ensureRun(bench, "WebApp Baseline", "v9");
                int testNo = runState.nextTestNo();

                // 2) NL->SQL generieren (oder "BLOCK")
                String gen = nl2sql.generateSql(prompt);
                Integer qno = bench.findQuestionNoByExactText(prompt); // kann null sein
                double e2eMs = (System.nanoTime() - t0) / 1_000_000.0;

                // 3) Vorab-Log (legt den evaluation_result-Eintrag an)
                bench.evalNoActor(
                        runState.getRunId(),
                        (qno != null ? qno : 0),
                        testNo,
                        prompt,
                        gen,
                        e2eMs
                );

                // 4) ReadOnly + RBAC (leicht) bestimmen
                boolean isReadOnly = unidb.isSelectOnly(gen);
                java.util.List<String> semErrs = semanticAnalyzer.detect(gen, prompt);
                boolean rbacOk = semErrs.stream().noneMatch("PII_REQUEST"::equals);
                String rbacViolationsJson = rbacOk ? "[]" : "[\"PII_REQUEST\"]";

                // 5) Zwei Pfade: BLOCK/nicht-SELECT => NICHT ausführen; SELECT => ausführen & messen
                String normSql = bench.normalizeSql(gen);
                Boolean exactMatch = null, execAcc = null;
                String compAccJson;

                if ("BLOCK".equalsIgnoreCase(gen) || !isReadOnly) {
                    // Keine Ausführung – nur Clause-Flags schreiben, damit component_accuracy gefüllt ist
                    compAccJson = bench.buildClauseFlagsJson(gen); // erzeugt {"has_group_by":..., "has_order_by":..., ...}

                    bench.updateAfterExec(
                            runState.getRunId(), testNo,
                            normSql,
                            isReadOnly,
                            rbacOk, rbacViolationsJson,
                            /*exec_ok*/ null, /*exec_error*/ null,
                            /*exec_ms*/ null, /*row_count*/ null, /*result_hash*/ null,
                            /*exact_match*/ exactMatch, /*exec_accuracy*/ execAcc,
                            compAccJson
                    );

                    finalAnswer = "BLOCK".equalsIgnoreCase(gen)
                            ? "Diese Abfrage wurde aus Sicherheitsgründen blockiert."
                            : "BLOCK (kein SELECT).";

                } else {
                    // 5b) Ausführen & Hashen (Ergebnisvorschau + Messwerte)
                    UnidbReadRepo.QueryRun run = unidb.runAndHashSelect(gen, 25); // führt aus, misst exec_ms, row_count, result_hash

                    // 6) Referenz-SQL laden (falls Benchmark-Frage vorhanden)
                    String refSql = (qno != null) ? bench.fetchRefSqlByQuestionNo(qno) : null;

                    // 7) Exact-Match (SQL-Text) & Execution-Accuracy (Ergebnis-Hash)
                    compAccJson = bench.buildClauseFlagsJson(gen); // Basis-Flags immer
                    if (run.execOk && refSql != null && unidb.isSelectOnly(refSql)) {
                        exactMatch = bench.sqlEqualsNormalized(gen, refSql);
                        var ref = unidb.hashOnly(refSql);
                        execAcc = (ref.execOk && ref.resultHash != null && ref.resultHash.equals(run.resultHash));
                        // Tabellen-Set-Jaccard ergänzen
                        compAccJson = bench.mergeCompAccWithJaccard(compAccJson, gen, refSql);
                    }

                    // 8) Alles in evaluation_result updaten
                    bench.updateAfterExec(
                            runState.getRunId(), testNo,
                            normSql,
                            /*is_read_only*/ true,
                            rbacOk, rbacViolationsJson,
                            run.execOk, run.execError, run.execMs, run.rowCount, run.resultHash,
                            exactMatch, execAcc,
                            compAccJson
                    );

                    // 9) Semantische Fehler zusätzlich anhängen (optional)
                    if (!semErrs.isEmpty()) bench.appendSemanticErrors(runState.getRunId(), testNo, semErrs);

                    // 10) Antwort für den Chat
                    finalAnswer = run.preview; // Markdown-Tabelle oder _(keine Zeilen)_
                }

            } else {
                // Dein bestehender Dokumente/Plain-LLM Pfad
                finalAnswer = llmService.getResponse(prompt);
            }
        } catch (Exception e) {
            finalAnswer = "Fehler: " + e.getMessage();
        }

        // 8) Chat-Nachricht speichern & Chat auffrischen
        ChatMessage msg = new ChatMessage(chat.getChatId(), userId, prompt, finalAnswer);
        chatMessageRepository.persistMessage(msg);
        chat.setMessages(chatMessageRepository.findMessagesByChatId(chat.getChatId()));
        chatRepository.update(chat);
        return chat;
    }

    private static final Pattern DB_HINTS = Pattern.compile(
            // Enthält .* an Anfang/Ende, damit matches() auf ganze Zeile passt
            ".*\\b(" +
                    // Kurse / Kursnamen / ECTS / Semester
                    "kurs|kurse|kursen|kursname|kursnamen|datenbanken|ects|semester|ss\\d{4}|ws\\d{4}|" +
                    // Prüfungen (mit Umlaut- und 'ue'-Variante) + Prüfungsdatum
                    "pr(ü|u)fung|pr(ü|u)fungen|pruefung|pruefungen|pr(ü|u)fungsdatum|pruefungsdatum|" +
                    // Studierende / Studenten
                    "student|studenten|studierende|studierenden|" +
                    // Professoren / Dozenten
                    "professor|professoren|dozent|dozenten|" +
                    // Belegungen
                    "belegung|belegungen|belegt|kursbelegung|kursbelegungen|" +
                    // Noten (inkl. Durchschnitt/„keine Note“/offen)
                    "note|noten|durchschnittsnote|schnittnote|offen|keine\\s+note|" +
                    // An-/Abmeldungen zu Prüfungen
                    "anmeldung|anmeldungen|angemeldet|" +
                    // Admin-/RBAC-Tabellen
                    "benutzer|nutzer|rolle|rollen|Rechte|Schema|" +
                    // Räume
                    "raum|räume|raeume|" +
                    // Zähl- und Ranking-Trigger
                    "anzahl|top-?\\s*\\d+|meisten" +
                    ")\\b.*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    // --- sehr einfache Heuristik: Uni-Schlüsselwörter => DB ---
    private String simpleRoute(String p) {
        String s = (p == null) ? "" : p;
        return DB_HINTS.matcher(s).matches() ? "db" : "docs";
    }



    // Titel eines Chats aktualisieren
    @Transactional
    public Chat updateChatTitle(String chatId, String newTitle) {
        Chat chat = chatRepository.findByChatId(chatId);
        if (chat != null) {
            chat.setTitle(newTitle);
            chatRepository.update(chat);
        }
        return chat;
    }

    // Chat löschen
    @Transactional
    public void deleteChat(String chatId) {
        // Den Chat anhand der Chat-ID abrufen
        Chat chat = chatRepository.findByChatId(chatId);

        if (chat != null) {
            // Alle Nachrichten mit der entsprechenden Chat-ID abrufen
            List<ChatMessage> messages = chatMessageRepository.findMessagesByChatId(chatId);

            // Alle Nachrichten löschen
            for (ChatMessage message : messages) {
                chatMessageRepository.delete(message);
            }

            // Den Chat selbst löschen
            chatRepository.delete(chat);
            System.out.println("Chat und zugehörige Nachrichten wurden gelöscht: " + chatId);
        } else {
            System.out.println("Kein Chat mit der ID " + chatId + " gefunden.");
        }
    }

    // Alle Chats eines Benutzers abrufen
    @Transactional
    public List<Chat> getChatsByUserId(Long userId) {
        return chatRepository.findByUserId(userId);
    }

    // Nachrichten eines spezifischen Chats abrufen
    @Transactional
    public List<ChatMessage> getMessagesByChatId(String chatId) {
        return chatMessageRepository.findMessagesByChatId(chatId);
    }
}