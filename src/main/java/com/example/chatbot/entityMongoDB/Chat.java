package com.example.chatbot.entityMongoDB;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

@MongoEntity(collection = "Chat")
public class Chat extends PanacheMongoEntity {
    private String chatId; // Chat ID
    private Long userId;
    private String title; // Optional: Titel des Chats
    private List<ChatMessage> messages = new ArrayList<>();
    private LocalDateTime createdAt; // Zeitpunkt der Chat-Erstellung

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now(); // Setzt automatisch die aktuelle Zeit
        }
    }

    // Getter und Setter
    public String getChatId() {
        return id.toString();
    }


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}