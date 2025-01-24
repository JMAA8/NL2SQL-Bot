package com.example.chatbot.entity;

import java.util.ArrayList;
import java.util.List;
import io.quarkus.mongodb.panache.PanacheMongoEntity;

public class Chat extends PanacheMongoEntity {
    private Long id;
    private Long userId;
    private String title; // Optional: Titel des Chats
    private List<ChatMessage> messages = new ArrayList<>();

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
