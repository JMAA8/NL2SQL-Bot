package com.example.chatbot.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class LLMClient {

    @ConfigProperty(name = "llm.api.key") // Der Key aus application.properties
    String apiKey;

    private static final String LLM_API_URL = "https://api.openai.com/v1/chat/completions";

    public String getApiKey() {
        return apiKey;
    }

    public LLMResponse sendRequest(LLMRequest request) {
        Client client = ClientBuilder.newClient();

        System.out.println("LLMClient Aufruf mit Request: " + request);

        try {
            Response response = client.target(LLM_API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey) // API-Key aus application.properties hinzufügen
                    .post(Entity.json(request)); // Request-Daten senden

            System.out.println("LLMClient erfolgreiches Versenden");

            // Lies die Rohdaten aus der Antwort
            String rawResponse = response.readEntity(String.class);
            System.out.println("Raw API Response: " + rawResponse);

            // Überprüfe den HTTP-Status
            if (response.getStatus() == 200) {
                // Verarbeite die Rohdaten mit ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    LLMResponse llmResponse = objectMapper.readValue(rawResponse, LLMResponse.class); // JSON-Daten verarbeiten
                    System.out.println("Erfolgreiche Antwort: " + llmResponse);
                    return llmResponse;
                } catch (JsonProcessingException e) {
                    System.err.println("Fehler beim Verarbeiten der JSON-Antwort: " + e.getMessage());
                    throw new RuntimeException("Fehler beim Parsen der Antwort vom LLM", e);
                }
            } else {
                System.err.println("Fehler: " + response.getStatus() + " - " + rawResponse);
                throw new RuntimeException("Fehler bei der Anfrage: " + response.getStatus());
            }
        } finally {
            client.close(); // Ressourcen freigeben
        }
    }
}



