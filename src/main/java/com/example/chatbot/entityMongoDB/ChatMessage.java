package com.example.chatbot.entityMongoDB;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

import java.time.LocalDateTime;

@MongoEntity(collection = "ChatMessage")
public class ChatMessage extends PanacheMongoEntity {
    private String chatId; // Manuelle Verknüpfung zur Chat-Entität
    private Long userId;
    private String prompt;
    private String response;
    private LocalDateTime timestamp;

    public ChatMessage() {
        // Default-Konstruktor
    }

    public ChatMessage(String chatId, Long userId, String prompt, String response) {
        this.chatId = chatId;
        this.userId = userId;
        this.prompt = prompt;
        this.response = response;
        this.timestamp = LocalDateTime.now(); // Zeitstempel setzen
    }

    // Getter und Setter
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
