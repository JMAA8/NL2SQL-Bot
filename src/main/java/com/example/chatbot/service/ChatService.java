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
                runState.ensureRun(bench, "WebApp Baseline", "v0"); // -> bench.evaluation_run

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

    // --- sehr einfache Heuristik: Uni-Schlüsselwörter => DB ---
    private String simpleRoute(String p) {
        String s = p.toLowerCase();
        if (s.matches(".*\\b(kurs|kurse|ects|pr(ü|u)fung|student|professor|note|einschreib|belegung|rolle|benutzer|raum|semester)\\b.*"))
            return "db";
        return "docs";
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