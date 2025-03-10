package com.example.chatbot.service;

import com.example.chatbot.entity.Group;
import com.example.chatbot.entityMongoDB.Document;
import com.example.chatbot.repository.DocumentRepository;
import com.example.chatbot.repository.GroupRepository;
import com.example.chatbot.repository.GroupUserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MultivaluedMap;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
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

    @Inject
    GroupRepository groupRepository;

    //Dokument speichern USER
    @Transactional
    public void saveDocument(MultipartFormDataInput input) {
        try {
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
            Document document = new Document();

            // Benutzer-ID abrufen
            if (uploadForm.containsKey("userId")) {
                String userId = uploadForm.get("userId").get(0).getBody(String.class, null);
                document.associationId = Long.parseLong(userId);
            }

            // Dokumentname abrufen
            if (uploadForm.containsKey("documentName")) {
                document.documentName = uploadForm.get("documentName").get(0).getBody(String.class, null);
            }

            // Dateiinhalt abrufen und als Binary speichern
            if (uploadForm.containsKey("file")) {
                InputPart filePart = uploadForm.get("file").get(0);
                MultivaluedMap<String, String> headers = filePart.getHeaders();
                String fileName = extractFileName(headers);

                if (document.documentName == null || document.documentName.isEmpty()) {
                    document.documentName = fileName;
                }

                InputStream inputStream = filePart.getBody(InputStream.class, null);
                byte[] fileBytes = inputStream.readAllBytes(); // Datei in Byte-Array umwandeln
                document.content = new Binary(fileBytes);
            }

            document.association = "USER";
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
        return documentRepository.find("associationId = ?1 and association like ?2", userId, "USER").list();
    }

    @Transactional
    public List<Document> searchDocuments(Long userId, String search) {
        return documentRepository.list("associationId = ?1 and lower(documentName) like ?2 and association like ?3", userId, "%" + search.toLowerCase() +"%", "USER");
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
        System.out.println("DocumentService - delete - documentId: " + documentId);

        try {
            ObjectId objectId = new ObjectId(documentId);
            //documentRepository.deleteById(objectId);
            documentRepository.delete("_id", objectId);
            System.out.println("Dokument gelöscht: " + documentId);
        } catch (IllegalArgumentException e) {
            System.err.println("Ungültige ObjectId: " + documentId);
            throw new RuntimeException("Fehlerhafte ObjectId: " + documentId, e);
        }
    }

    public List<Document> getDocumentsByGroupId(Long groupId) {
        return documentRepository.find("associationId = ?1 and association like ?2", groupId, "GROUP").list();
    }


    //Datei speichern GROUP
    @Transactional
    public void uploadDocumentGroup(MultipartFormDataInput input) {
        try {
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
            Document document = new Document();

            // Gruppen-ID abrufen und in Long umwandeln
            if (uploadForm.containsKey("groupId")) {
                String groupId = uploadForm.get("groupId").get(0).getBody(String.class, null);
                document.associationId = Long.parseLong(groupId);
            }

            // Dokumentname abrufen (falls explizit gesendet)
            if (uploadForm.containsKey("documentName")) {
                document.documentName = uploadForm.get("documentName").get(0).getBody(String.class, null);
            }

            // Dateiinhalt abrufen und als Binary speichern
            if (uploadForm.containsKey("file")) {
                InputPart filePart = uploadForm.get("file").get(0);
                MultivaluedMap<String, String> headers = filePart.getHeaders();

                // Extrahiere den Dateinamen aus dem Content-Disposition-Header
                String fileName = extractFileName(headers);
                if (document.documentName == null || document.documentName.isEmpty()) {
                    document.documentName = fileName; // Falls kein Name übergeben wurde, verwende den Dateinamen
                }

                // Datei als Byte-Array speichern
                InputStream inputStream = filePart.getBody(InputStream.class, null);
                byte[] fileBytes = inputStream.readAllBytes(); // Datei in Byte-Array umwandeln

                document.content = new Binary(fileBytes);
            }

            document.association = "GROUP";
            documentRepository.persist(document);
            System.out.println("✅ Datei erfolgreich gespeichert!");

        } catch (Exception e) {
            throw new RuntimeException("❌ Fehler beim Speichern des Dokuments", e);
        }
    }




}
