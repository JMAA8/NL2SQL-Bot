package com.example.chatbot.chat;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "Chat")
public class ChatRequest {
    private Long userId; // Benutzer-ID
    private Long chatId; // Chat-ID (optional, für bestehende Chats)
    private String prompt;

    // Getter und Setter
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {

        this.userId = userId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {

        this.chatId = chatId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {

        this.prompt = prompt;
    }
}
