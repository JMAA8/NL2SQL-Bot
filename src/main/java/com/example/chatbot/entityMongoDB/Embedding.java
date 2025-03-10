package com.example.chatbot.entityMongoDB;

import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.persistence.PrePersist;
import org.bson.Document;

import java.time.LocalDateTime;

@MongoEntity(collection = "Embeddings")
public class Embedding {

    public Long associationIdEm;
    public String documentNameEm;
    public Document jsonData;
    public String associationEm;
    public LocalDateTime timestampEm = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.timestampEm == null) {
            this.timestampEm = LocalDateTime.now();
        }
    }
}
