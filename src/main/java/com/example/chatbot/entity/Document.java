package com.example.chatbot.entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

import java.time.LocalDateTime;

@MongoEntity(collection = "Document")
public class Document extends PanacheMongoEntity {
    public String userId;
    public String documentName;
    public String content;
    public LocalDateTime timestamp;
}
