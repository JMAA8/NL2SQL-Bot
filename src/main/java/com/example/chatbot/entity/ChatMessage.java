package com.example.chatbot.entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.time.LocalDateTime;

public class ChatMessage extends PanacheMongoEntity {
    private Long chatId;
    private Long userId;
    private String prompt;
    private String response;
    private LocalDateTime timestamp; // Zeitpunkt der Nachrichtenerstellung

    public ChatMessage() {
        // Default-Konstruktor
    }

    public ChatMessage(Long chatId, Long userId, String prompt, String response) {
        this.chatId = chatId;
        this.userId = userId;
        this.prompt = prompt;
        this.response = response;
        this.timestamp = LocalDateTime.now(); // Zeitstempel setzen
    }

    // Getter und Setter
    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
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