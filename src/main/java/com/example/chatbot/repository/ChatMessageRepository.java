package com.example.chatbot.repository;

import com.example.chatbot.entityMongoDB.ChatMessage;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ChatMessageRepository implements PanacheMongoRepository<ChatMessage> {

    // Nachrichten basierend auf der Chat-ID abrufen
    public List<ChatMessage> findMessagesByChatId(String chatId) {
        return find("chatId", chatId).list();
    }

    // Eine neue Nachricht speichern
    public void persistMessage(ChatMessage message) {
        persist(message);
    }

    // Nachricht löschen
    public void deleteMessage(ChatMessage message) {
        delete(message);
    }
}
