package com.example.chatbot.repository;

import com.example.chatbot.entityMongoDB.Chat;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ChatRepository implements PanacheMongoRepository<Chat> {

    // Alle Chats eines Benutzers basierend auf der User-ID abrufen
    public List<Chat> findByUserId(Long userId) {
        return list("userId", userId);
    }

    // Einen bestimmten Chat basierend auf der Chat-ID abrufen
    public Chat findByChatId(String chatId) {
        return find("chatId", chatId).firstResult();
    }

    // Einen neuen Chat speichern
    public void persistChat(Chat chat) {
        persist(chat);
    }

    // Bestehenden Chat aktualisieren
    public void updateChat(Chat chat) {
        update(chat);
    }

    // Chat löschen
    public void deleteChat(Chat chat) {
        delete(chat);
    }
}
