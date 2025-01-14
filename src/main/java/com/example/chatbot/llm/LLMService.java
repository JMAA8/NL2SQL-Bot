package com.example.chatbot.llm;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LLMService {

    @Inject
    LLMClient llmClient; // RESTEasy-Client verwenden

    public String getResponse(String prompt) {
        // Anfrage erstellen
        LLMRequest request = new LLMRequest(prompt);

        try {
            // API über den LLMClient aufrufen
            LLMResponse response = llmClient.sendRequest(request);

            // Antwort verarbeiten
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getText();
            }
            return "Keine Antwort vom LLM erhalten.";
        } catch (RuntimeException e) {
            // Fehlerbehandlung
            return "Fehler bei der Kommunikation mit dem LLM: " + e.getMessage();
        }
    }
}
