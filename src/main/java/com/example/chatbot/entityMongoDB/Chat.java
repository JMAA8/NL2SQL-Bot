package com.example.chatbot.entityMongoDB;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;


import java.time.LocalDateTime;

@MongoEntity(collection = "Chat")
public class Chat extends PanacheMongoEntity {
    private String chatId;
    private Long userId;
    private String title;
    private List<ChatMessage> messages = new ArrayList<>();
    private Instant createdAt;




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

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
