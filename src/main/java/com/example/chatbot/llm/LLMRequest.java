package com.example.chatbot.llm;

import java.util.ArrayList;
import java.util.List;

public class LLMRequest {

    private String model;
    private List<Message> messages;
    private int max_tokens;

    public LLMRequest(String prompt, String bestFileText) {
        this.model = "gpt-4"; // Modellwahl

        this.messages = new ArrayList<>();
        this.messages.add(new Message("system", "You are a helpful assistant."));

        // Füge den Nutzer-Prompt und den extrahierten PDF-Text hinzu
        String userMessage = prompt + "\n\nHere is some relevant information from a document that may help you:\n\n" + bestFileText;

        this.messages.add(new Message("user", userMessage));
        System.out.println("LLMRequest - erfolgreiches Hinzufügen zu Message: " + messages);

        this.max_tokens = 1000; // Erhöht, falls mehr Kontext notwendig ist
    }

    // Getter und Setter
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public int getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(int max_tokens) {
        this.max_tokens = max_tokens;
    }

    // Innere Klasse für Nachrichten
    public static class Message {
        private String role; // "system", "user", oder "assistant"
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
