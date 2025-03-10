package com.example.chatbot.entityMongoDB;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.persistence.PrePersist;
import org.bson.types.Binary;

import java.time.LocalDateTime;

@MongoEntity(collection = "Documents")
public class Document extends PanacheMongoEntity {
    public Long associationId;
    public String documentName;
    public Binary content;
    public String association;
    public LocalDateTime timestamp = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}

