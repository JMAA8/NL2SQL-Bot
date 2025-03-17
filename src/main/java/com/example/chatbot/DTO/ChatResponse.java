package com.example.chatbot.DTO;

public class ChatResponse {
    private String response;

    public ChatResponse(String response) {
        this.response = response;
    }

    // Getter und Setter
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}

