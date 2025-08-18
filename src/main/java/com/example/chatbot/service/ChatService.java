package com.example.chatbot.service;

import com.example.chatbot.entityMongoDB.Chat;
import com.example.chatbot.entityMongoDB.ChatMessage;
import com.example.chatbot.repository.ChatMessageRepository;
import com.example.chatbot.repository.ChatRepository;
import com.example.chatbot.unidb.NL2SQLService;
import com.example.chatbot.unidb.BenchRepo;
import com.example.chatbot.unidb.UnidbReadRepo;
import com.example.chatbot.unidb.RunState;
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

    // Nachricht speichern und Chat verwalten
    @Transactional
    public Chat handleChatMessage(Long userId, String chatId, String prompt) {
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
                runState.ensureRun(bench, "WebApp Baseline", "v4"); // -> bench.evaluation_run

                int testNo = runState.nextTestNo();
                String gen = nl2sql.generateSql(prompt); // SQL oder "BLOCK"
                Integer qno = bench.findQuestionNoByExactText(prompt); // 1..25 (kann null sein)

                // 2) Ergebnis im Benchmark loggen (NO-ACTOR-Variante)
                bench.evalNoActor(runState.getRunId(),
                        qno != null ? qno : 0,
                        testNo,
                        prompt,
                        gen,
                        0 /* e2e-latency-ms, kannst du später messen */);

                // 3) Vorschau aus DB oder BLOCK-Hinweis
                if (!"BLOCK".equalsIgnoreCase(gen)) {
                    finalAnswer = unidb.previewSelect(gen, 25); // hübsche Vorschau bis 25 Zeilen
                } else {
                    finalAnswer = "Diese Abfrage wurde aus Sicherheitsgründen blockiert.";
                }
            } else {
                // Dein bestehender Pfad (Dokumente/Plain LLM)
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
                    "belegung|belegungen|belegt|kursbelegung|" +
                    // Noten (inkl. Durchschnitt/„keine Note“/offen)
                    "note|noten|durchschnittsnote|schnittnote|offen|keine\\s+note|" +
                    // An-/Abmeldungen zu Prüfungen
                    "anmeldung|anmeldungen|angemeldet|" +
                    // Admin-/RBAC-Tabellen
                    "benutzer|nutzer|rolle|rollen|" +
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