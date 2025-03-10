package com.example.chatbot.Embedding;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONArray;
import org.json.JSONObject;

@ApplicationScoped
public class OpenAIService {

    @ConfigProperty(name = "llm.api.key")
    String apiKey;

    private static final String OPENAI_EMBEDDING_URL = "https://api.openai.com/v1/embeddings";

    public JSONArray getEmbedding(String text) {
        try {
            Client client = ClientBuilder.newClient();

            // OpenAI API JSON-Request erstellen
            JsonObject requestBody = new JsonObject();
            requestBody.put("input", text);
            requestBody.put("model", "text-embedding-ada-002");

            Response response = client.target(OPENAI_EMBEDDING_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .post(Entity.json(requestBody.toString()));

            String responseString = response.readEntity(String.class);
            response.close();

            JSONObject jsonResponse = new JSONObject(responseString);
            if (jsonResponse.has("error")) {
                System.out.println("❌ OpenAI Fehler: " + jsonResponse.getJSONObject("error").getString("message"));
                return new JSONArray();
            }


            return jsonResponse.getJSONArray("data");

        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

}
