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
            System.out.println("LLMService Prompt: " + prompt);
            LLMResponse response = llmClient.sendRequest(request); // LLMClient aufrufen
            System.out.println("LLMService: Antwort erfolgreich erhalten.");

            // Antwort verarbeiten
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String llmReply = response.getChoices().get(0).getMessage().getContent();
                System.out.println("Antwort vom LLM: " + llmReply);
                return llmReply; // Inhalt der Nachricht zurückgeben
            }

            System.err.println("Keine gültige Antwort vom LLM erhalten.");
            return "Keine Antwort vom LLM erhalten.";
        } catch (RuntimeException e) {
            // Fehlerbehandlung und Protokollierung
            System.err.println("Fehler bei der Kommunikation mit dem LLM: " + e.getMessage());
            return "Fehler bei der Kommunikation mit dem LLM: " + e.getMessage();
        }
    }
}
