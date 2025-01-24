package com.example.chatbot.entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.time.LocalDateTime;

public class Document extends PanacheMongoEntity {
    public String userId;
    public String documentName;
    public String content;
    public LocalDateTime timestamp;
}
