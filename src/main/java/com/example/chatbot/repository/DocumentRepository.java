package com.example.chatbot.repository;

import com.example.chatbot.entity.Document;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DocumentRepository implements PanacheMongoRepository<Document> {}