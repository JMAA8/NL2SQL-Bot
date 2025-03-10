package com.example.chatbot.service;

import com.example.chatbot.entityMongoDB.Embedding;
import com.example.chatbot.repository.EmbeddingRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;


@ApplicationScoped
public class EmbeddingService {

    @Inject
    EmbeddingRepository repository;

    public void saveJsonFileToMongo(Document jsonData, Long associationId, String documentName, String association) {
        // Lade die JSON-Datei als String
        //String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));

        // Konvertiere den String zu einem BSON Document für MongoDB
        //Document jsonData = Document.parse(jsonContent);

        // Speichere es in der Datenbank
        Embedding embedding = new Embedding();
        embedding.associationEm = association;
        embedding.associationIdEm = associationId;
        embedding.documentNameEm = documentName;
        embedding.jsonData = jsonData;

        repository.persist(embedding);
        System.out.println("JSON-Datei wurde gespeichert!");
    }
}

