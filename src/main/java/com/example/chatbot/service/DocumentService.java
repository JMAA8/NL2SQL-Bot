package com.example.chatbot.service;

import com.example.chatbot.entityMongoDB.Document;
import com.example.chatbot.repository.DocumentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DocumentService {

    @Inject
    DocumentRepository documentRepository;

    @Transactional
    public void saveDocument(MultipartFormDataInput input) {
        try {
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
            Document document = new Document();

            // Benutzer-ID aus der Anfrage abrufen
            if (uploadForm.containsKey("userId")) {
                String userId = uploadForm.get("userId").get(0).getBody(String.class, null);
                document.userId = userId;
            }

            // Dokumentname abrufen
            if (uploadForm.containsKey("documentName")) {
                String documentName = uploadForm.get("documentName").get(0).getBody(String.class, null);
                document.documentName = documentName;
            }

            // Dateiinhalt abrufen
            if (uploadForm.containsKey("file")) {
                InputStream inputStream = uploadForm.get("file").get(0).getBody(InputStream.class, null);
                document.content = readInputStream(inputStream);
            }

            documentRepository.persist(document);
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Speichern des Dokuments", e);
        }
    }

    @Transactional
    public List<Document> getDocumentsByUserId(String userId) {
        return documentRepository.find("userId", userId).list();
    }

    @Transactional
    public List<Document> searchDocuments(String userId, String search) {
        return documentRepository.list("userId = ?1 and lower(documentName) like ?2", userId, "%" + search.toLowerCase() + "%");
    }

    private String readInputStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }
}
