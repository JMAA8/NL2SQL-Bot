package com.example.chatbot.service;

import com.example.chatbot.entityMongoDB.Chat;
import com.example.chatbot.entityMongoDB.ChatMessage;
import com.example.chatbot.repository.ChatRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ChatService {

    @Inject
    ChatRepository chatRepository;

    // Nachricht speichern und Chat verwalten
    @Transactional
    public Chat handleChatMessage(Long userId, String chatId, String prompt) {
        Chat chat = (chatId != null) ? chatRepository.findByChatId(chatId) : null;
        System.out.println("chatService chatId: " + chatId);
        System.out.println("chatService Prompt: " + prompt);
        try {
        if (chatId == null) {
            System.out.println("chatService - if - ChatId: ");
            chat = new Chat();
            System.out.println("neuer Chat wird erstellt");
            chat.setUserId(userId);
            System.out.println("neuer Chat userId: " + chat.getUserId());
            chat.setTitle(prompt.split(" ")[0]); // Erstes Wort als Titel
            System.out.println("neuer Chat Titel: " + chat.getTitle());

            chatRepository.persist(chat);
            System.out.println("chatService - gesetzte ChatId: " + chat.getChatId());

        }
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Verarbeiten der Nachricht", e);
        }

        ChatMessage message = new ChatMessage(chat.getChatId(), userId, prompt, "Antwort von LLM"); // Dummy LLM-Antwort
        chatRepository.persistMessage(message);

        chat.getMessages().add(message);
        chatRepository.update(chat);
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
    public List<Chat> getMessagesByChatId(Long chatId) {
        return chatRepository.findMessagesByChatId(chatId);
    }
}