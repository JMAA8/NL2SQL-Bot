package com.example.chatbot.repository;


import com.example.chatbot.entityMongoDB.Embedding;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmbeddingRepository implements PanacheMongoRepository<Embedding> {
}
