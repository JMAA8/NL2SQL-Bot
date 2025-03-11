package com.example.chatbot.Embedding;

import org.json.JSONArray;
import org.json.JSONObject;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;


@ApplicationScoped
public class EmbeddingProcessor {

    private static final int TOKEN_LIMIT = 7000;

    public JSONArray processTextChunks(String text, OpenAIService openAIService, String docName) {

        List<String> chunks = splitTextIntoChunks(text, TOKEN_LIMIT);
        JSONArray embeddingsArray = new JSONArray();

        int startToken = 1;

        for (String chunk : chunks) {
            JSONArray embedding = openAIService.getEmbedding(chunk);

            JSONObject chunkData = new JSONObject();
            chunkData.put("path", docName);
            chunkData.put("token_start", startToken);
            chunkData.put("token_end", startToken + TOKEN_LIMIT - 1);
            chunkData.put("embedding", embedding);

            embeddingsArray.put(chunkData);
            startToken += TOKEN_LIMIT;
        }
        return embeddingsArray;
    }

    private List<String> splitTextIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += chunkSize) {
            chunks.add(text.substring(i, Math.min(length, i + chunkSize)));
        }
        return chunks;
    }
}