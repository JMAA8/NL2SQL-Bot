package com.example.chatbot.entityMongoDB;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

@MongoEntity(collection = "Documents")
public class Document extends PanacheMongoEntity {
    public Long userId;
    public String documentName;
    public String content;
    public LocalDateTime timestamp = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}

