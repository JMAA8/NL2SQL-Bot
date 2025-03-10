package com.example.chatbot.Embedding;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;
import jakarta.ws.rs.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CheckSimilarity {

    @Inject
    OpenAIService openAIService;

   @Inject
   EmbeddingResource embeddingResource;

    @ApplicationScoped
    public String checkSimilarity(String prompt) {

        try {
            String queryText = prompt;


            // 🔹 2️⃣ OpenAI-Embedding für den Suchtext abrufen
            JSONArray queryEmbeddingArray = openAIService.getEmbedding(queryText);


            if (queryEmbeddingArray.isEmpty()) {
                String NoSearchEmbedding = "Embedding für Anfrage Fehler, ignoriere dieses Prompt";
                return NoSearchEmbedding;
            }
            JSONArray queryEmbedding = queryEmbeddingArray.getJSONObject(0).getJSONArray("embedding");

            // 🔹 3️⃣ JSON-Datei mit gespeicherten Embeddings laden
            String jsonFilePath = "/Users/mickey/ChatbotEmbedding/embeddings.json";
            File file = new File(jsonFilePath);
            if (!file.exists()) {
                String NoEmbedding = "Keine Embedding gefunden, ignoriere dieses Prompt";
                return NoEmbedding;
            }
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)), StandardCharsets.UTF_8);
            JSONArray jsonEmbeddings = new JSONArray(jsonContent);




            double bestScore = 0.0;
            String bestFilePath = "";
            int bestTokenstart = 0;
            int bestTokenend = 0;

            for (int i = 0; i < jsonEmbeddings.length(); i++) {
                JSONArray docChunks = jsonEmbeddings.getJSONArray(i);


                for (int j = 0; j < docChunks.length(); j++) {
                    JSONObject chunkData = docChunks.getJSONObject(j);

                    String filepath = chunkData.getString("path");
                    int tokenstart = chunkData.getInt("token_start");
                    int tokenend = chunkData.getInt("token_end");
                    JSONArray storedEmbedding = chunkData.getJSONArray("embedding").getJSONObject(0).getJSONArray("embedding");





                    try {
                        double similarityScore = cosineSimilarity(queryEmbedding, storedEmbedding);



                        if (similarityScore > bestScore) {
                            bestScore = similarityScore;
                            bestFilePath = filepath;
                            bestTokenstart = tokenstart;
                            bestTokenend = tokenend;

                        }
                    } catch (Exception e) {
                        System.out.println("❌ Fehler bei Datei: " + filepath + " | Chunk: " + (j + 1) + "/" + docChunks.length());
                        e.printStackTrace();
                    }


                }
            }

            if (bestScore == 0.0) {
                String NoDoc= "Kein passendes Dokument gefunden";
                return NoDoc;

            }


            String bestFileText = embeddingResource.extractText(bestFilePath);

            //Rückgabe des Files zur Beantwortung des Users
            return bestFileText;


        }  catch (Exception e){
            e.printStackTrace();
            String WrongCalc = "Fehler beim Berechnen, ignoriere diesen Prompt";
            return WrongCalc;
        }
    }

    // 🔹 Methode zur Berechnung der Kosinus-Ähnlichkeit
    private double cosineSimilarity(JSONArray vectorA, JSONArray vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length(); i++) {
            double a = vectorA.getDouble(i);
            double b = vectorB.getDouble(i);
            dotProduct += a * b;
            normA += Math.pow(a, 2);
            normB += Math.pow(b, 2);
        }



        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));

        System.out.println("📊 Dot-Product: " + dotProduct + " | NormA: " + normA + " | NormB: " + normB + " | Similarity: " + similarity);

        return similarity;
    }

}
