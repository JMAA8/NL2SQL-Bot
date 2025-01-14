package com.example.chatbot.llm;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class LLMClient {

    private static final String LLM_API_URL = "https://api.openai.com/v1/completions";
    private static final String API_KEY = "dein-api-schlüssel"; // Deinen API-Schlüssel hier einfügen

    public LLMResponse sendRequest(LLMRequest request) {
        // RESTEasy Client erstellen
        Client client = ClientBuilder.newClient();

        try {
            // HTTP-POST-Anfrage senden
            Response response = client.target(LLM_API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + API_KEY) // API-Key als Header hinzufügen
                    .post(Entity.json(request)); // Request-Daten senden

            // Antwort verarbeiten
            if (response.getStatus() == 200) {
                return response.readEntity(LLMResponse.class); // JSON-Antwort in LLMResponse umwandeln
            } else {
                System.err.println("Fehler: " + response.getStatus() + " - " + response.readEntity(String.class));
                throw new RuntimeException("Fehler bei der Anfrage: " + response.getStatus());
            }
        } finally {
            client.close(); // Ressourcen freigeben
        }
    }
}
