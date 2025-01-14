package com.example.chatbot.llm;

public class LLMRequest {

    private String model;
    private String prompt;
    private int max_tokens;

    public LLMRequest(String prompt) {
        this.model = "text-davinci-003"; // Beispielmodell
        this.prompt = prompt;
        this.max_tokens = 100; // Maximale Tokens
    }

    // Getter und Setter
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public int getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(int max_tokens) {
        this.max_tokens = max_tokens;
    }
}
