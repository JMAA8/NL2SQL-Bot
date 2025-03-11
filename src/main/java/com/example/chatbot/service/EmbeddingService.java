package com.example.chatbot.service;

import com.example.chatbot.entityMongoDB.Embedding;
import com.example.chatbot.repository.EmbeddingRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;

import java.util.List;


@ApplicationScoped
public class EmbeddingService {

    @Inject
    EmbeddingRepository repository;

    public void saveJsonFileToMongo(List<Document> jsonData, Long associationId, String documentName, String association) {

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

