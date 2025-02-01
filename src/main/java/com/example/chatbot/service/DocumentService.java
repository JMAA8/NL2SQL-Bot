package com.example.chatbot.service;

import com.example.chatbot.entityMongoDB.Document;
import com.example.chatbot.repository.DocumentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MultivaluedMap;
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

            // Benutzer-ID abrufen und in Long umwandeln
            if (uploadForm.containsKey("userId")) {
                String userId = uploadForm.get("userId").get(0).getBody(String.class, null);
                document.userId = Long.parseLong(userId);
            }

            // Dokumentname abrufen (falls explizit gesendet)
            if (uploadForm.containsKey("documentName")) {
                document.documentName = uploadForm.get("documentName").get(0).getBody(String.class, null);
            }

            // Dateiinhalt abrufen und den Namen aus dem Upload extrahieren
            if (uploadForm.containsKey("file")) {
                InputPart filePart = uploadForm.get("file").get(0);
                MultivaluedMap<String, String> headers = filePart.getHeaders();

                // Extrahiere den Dateinamen aus dem Content-Disposition-Header
                String fileName = extractFileName(headers);
                if (document.documentName == null || document.documentName.isEmpty()) {
                    document.documentName = fileName; // Falls kein Name übergeben wurde, verwende den Dateinamen
                }

                InputStream inputStream = filePart.getBody(InputStream.class, null);
                document.content = readInputStream(inputStream);
            }

            documentRepository.persist(document);
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Speichern des Dokuments", e);
        }
    }

    // Methode zum Extrahieren des Dateinamens aus den Headers
    private String extractFileName(MultivaluedMap<String, String> headers) {
        String contentDisposition = headers.getFirst("Content-Disposition");
        if (contentDisposition != null) {
            for (String part : contentDisposition.split(";")) {
                if (part.trim().startsWith("filename")) {
                    return part.split("=")[1].trim().replace("\"", "");
                }
            }
        }
        return "Unbenanntes_Dokument";
    }



    @Transactional
    public List<Document> getDocumentsByUserId(Long userId) {
        return documentRepository.find("userId", userId).list();
    }

    @Transactional
    public List<Document> searchDocuments(Long userId, String search) {
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

    @Transactional
    public void deleteDocument(String documentId) {
        documentRepository.delete("id", documentId);
    }
}
