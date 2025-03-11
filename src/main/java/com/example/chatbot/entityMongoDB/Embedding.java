package com.example.chatbot.entityMongoDB;

import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.persistence.PrePersist;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.List;

@MongoEntity(collection = "Embeddings")
public class Embedding {

    public Long associationIdEm;
    public String documentNameEm;
    public List<Document> jsonData;
    public String associationEm;
    public LocalDateTime timestampEm = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.timestampEm == null) {
            this.timestampEm = LocalDateTime.now();
        }
    }

    public Long getAssociationIdEm() {
        return associationIdEm;
    }

    public void setAssociationIdEm(Long associationIdEm) {
        this.associationIdEm = associationIdEm;
    }

    public String getDocumentNameEm() {
        return documentNameEm;
    }

    public void setDocumentNameEm(String documentNameEm) {
        this.documentNameEm = documentNameEm;
    }

    public List<Document> getJsonData() {
        return jsonData;
    }

    public void setJsonData(List<Document> jsonData) {
        this.jsonData = jsonData;
    }

    public String getAssociationEm() {
        return associationEm;
    }

    public void setAssociationEm(String associationEm) {
        this.associationEm = associationEm;
    }
}
