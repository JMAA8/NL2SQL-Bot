package com.example.chatbot.service;

import com.example.chatbot.entityMongoDB.Chat;
import com.example.chatbot.entityMongoDB.ChatMessage;
import com.example.chatbot.repository.ChatMessageRepository;
import com.example.chatbot.repository.ChatRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ChatService {

    @Inject
    ChatRepository chatRepository;

    @Inject
    ChatMessageRepository chatMessageRepository;

    // Nachricht speichern und Chat verwalten
    @Transactional
    public Chat handleChatMessage(Long userId, String chatId, String prompt) {
        Chat chat = (chatId != null) ? chatRepository.findByChatId(chatId) : null;
        System.out.println("chatService chatId: " + chatId);
        System.out.println("chatService Prompt: " + prompt);

        try {
            // Falls `chatId` null ist, wird ein neuer Chat erstellt
            if (chat == null) {
                System.out.println("chatService - if - Neuer Chat wird erstellt");
                chat = new Chat();
                chat.setUserId(userId);
                chat.setTitle(prompt.split(" ")[0]); // Erstes Wort des Prompts als Titel
                chatRepository.persistChat(chat); // Chat speichern
                System.out.println("Neuer Chat erstellt mit ID: " + chat.getChatId());
            }

            // Nachricht erstellen
            ChatMessage message = new ChatMessage(chat.getChatId(), userId, prompt, "Antwort von LLM"); // Dummy-Antwort
            System.out.println("ChatService - handleChatMessage - Nachricht wird erstellt");

            // Nachricht speichern
            chatMessageRepository.persistMessage(message);
            System.out.println("Nachricht gespeichert mit ChatId: " + message.getChatId());

            // Manuelles Verknüpfen der Nachricht mit dem Chat
            List<ChatMessage> messages = chatMessageRepository.findMessagesByChatId(chat.getChatId());
            messages.add(message);
            chat.setMessages(messages);

            // Chat aktualisieren
            chatRepository.update(chat);
            System.out.println("Chat aktualisiert mit neuen Nachrichten: " + chat.getMessages());

        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Verarbeiten der Nachricht", e);
        }

        return chat;
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
        Chat chat = chatRepository.findByChatId(chatId);
        if (chat != null) {
            chatRepository.delete(chat);
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