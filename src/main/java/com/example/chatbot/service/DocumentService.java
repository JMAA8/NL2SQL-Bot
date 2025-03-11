package com.example.chatbot.service;

import com.example.chatbot.Embedding.EmbeddingProcessor;
import com.example.chatbot.Embedding.EmbeddingResource;
import com.example.chatbot.Embedding.OpenAIService;
import com.example.chatbot.entityMongoDB.DocumentMongoDB;
import com.example.chatbot.entityMongoDB.Embedding;
import com.example.chatbot.repository.DocumentRepository;
import com.example.chatbot.repository.EmbeddingRepository;
import com.example.chatbot.repository.GroupRepository;
import com.mongodb.client.model.Filters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MultivaluedMap;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@ApplicationScoped
public class DocumentService {

    @Inject
    DocumentRepository documentRepository;

    @Inject
    GroupRepository groupRepository;

    @Inject
    EmbeddingRepository embeddingRepository;

    @Inject
    EmbeddingResource embeddingResource;

    @Inject
    OpenAIService openAIService;

    @Inject
    EmbeddingService embeddingService;

    @Inject
    EmbeddingProcessor embeddingProcessor;

    //Dokument speichern USER
    @Transactional
    public void saveDocument(MultipartFormDataInput input) {
        try {
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
            DocumentMongoDB documentMongoDB = new DocumentMongoDB();

            // Benutzer-ID abrufen
            if (uploadForm.containsKey("userId")) {
                String userId = uploadForm.get("userId").get(0).getBody(String.class, null);
                documentMongoDB.associationId = Long.parseLong(userId);
            }

            // Dokumentname abrufen
            if (uploadForm.containsKey("documentName")) {
                documentMongoDB.documentName = uploadForm.get("documentName").get(0).getBody(String.class, null);
            }

            // Dateiinhalt abrufen und als Binary speichern
            if (uploadForm.containsKey("file")) {
                InputPart filePart = uploadForm.get("file").get(0);
                MultivaluedMap<String, String> headers = filePart.getHeaders();
                String fileName = extractFileName(headers);

                if (documentMongoDB.documentName == null || documentMongoDB.documentName.isEmpty()) {
                    documentMongoDB.documentName = fileName;
                }

                InputStream inputStream = filePart.getBody(InputStream.class, null);
                byte[] fileBytes = inputStream.readAllBytes(); // Datei in Byte-Array umwandeln
                documentMongoDB.content = new Binary(fileBytes);
            }

            documentMongoDB.association = "USER";
            documentRepository.persist(documentMongoDB);
            System.out.println("saveDocument (User) - erfolgreiches Speichern des Docs");

            //Embedding
            System.out.println("Bevor Embedding Name: " + documentMongoDB.documentName);
           if  (documentMongoDB.documentName.toLowerCase().endsWith(".pdf")){
               String userId = uploadForm.get("userId").get(0).getBody(String.class, null);
               String file = embeddingResource.extractTextMongoDB(documentMongoDB.content);
               System.out.println("String File");
               JSONArray MongoEmbeddingArray = embeddingProcessor.processTextChunks(file, openAIService, documentMongoDB.documentName);
               System.out.println("OpenAIService hat geklappt");
               List<Document> documentList = new ArrayList<>();


               for (int i = 0; i < MongoEmbeddingArray.length(); i++) {
                   JSONObject jsonObject = MongoEmbeddingArray.getJSONObject(i);
                   Document doc = Document.parse(jsonObject.toString());
                   documentList.add(doc);
               }
               System.out.println("JsonArray zu Document hat geklappt");
               System.out.println("UserId:" + userId);
               Embedding counter = embeddingRepository.find("associationIdEm = ?1 and associationEm = ?2", documentMongoDB.associationId, documentMongoDB.association).firstResult();

               if (counter != null) {
                   System.out.println("Es existiert bereits ein Embedding für diesen Nutzer.");
                   // Existierendes Embedding abrufen
                   Embedding existingEmbedding = embeddingRepository.find(
                           "associationIdEm = ?1 and associationEm = ?2",
                           documentMongoDB.associationId, documentMongoDB.association
                   ).firstResult();

                   System.out.println("Existierendes Embedding abgerufen Exisitierendes Embedding: " + existingEmbedding);

                   // Hol die bestehende Liste aus `jsonData`, falls vorhanden
                   List<Document> existingData = existingEmbedding.getJsonData();
                   if (existingData == null) {
                       existingData = new ArrayList<>();
                   }
                   System.out.println("Liste aus jsonData geholt: " + existingData.size() + " Elemente");
                   System.out.println("Liste JsonData " + existingData);

                   System.out.println("Neues Embedding: " + documentList);



                   // Aktualisiere das JSON-Feld
                   existingData.addAll(documentList);
                   System.out.println("JSON-Feld wurde aktualisiert");


                   existingEmbedding.setJsonData(existingData);
                  //embeddingRepository.persist(existingEmbedding);
                   //embeddingRepository.persistOrUpdate(existingEmbedding);
                   embeddingRepository.update(existingEmbedding);

                    // Speichere das aktualisierte Embedding
                   //embeddingRepository.update(existingEmbedding);
                   System.out.println("Bestehendes Embedding wurde aktualisiert!");

               } else {
                   System.out.println("Kein Embedding gefunden, kann erstellt werden.");

                   embeddingService.saveJsonFileToMongo(documentList, documentMongoDB.associationId, "Embedding.json", documentMongoDB.association);
               }


           }


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
    public List<DocumentMongoDB> getDocumentsByUserId(Long userId) {
        return documentRepository.find("associationId = ?1 and association like ?2", userId, "USER").list();
    }

    @Transactional
    public List<DocumentMongoDB> searchDocuments(Long userId, String search) {
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

    public List<DocumentMongoDB> getDocumentsByGroupId(Long groupId) {
        return documentRepository.find("associationId = ?1 and association like ?2", groupId, "GROUP").list();
    }


    //Datei speichern GROUP
    @Transactional
    public void uploadDocumentGroup(MultipartFormDataInput input) {
        try {
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
            DocumentMongoDB documentMongoDB = new DocumentMongoDB();

            // Gruppen-ID abrufen und in Long umwandeln
            if (uploadForm.containsKey("groupId")) {
                String groupId = uploadForm.get("groupId").get(0).getBody(String.class, null);
                documentMongoDB.associationId = Long.parseLong(groupId);
            }

            // Dokumentname abrufen (falls explizit gesendet)
            if (uploadForm.containsKey("documentName")) {
                documentMongoDB.documentName = uploadForm.get("documentName").get(0).getBody(String.class, null);
            }

            // Dateiinhalt abrufen und als Binary speichern
            if (uploadForm.containsKey("file")) {
                InputPart filePart = uploadForm.get("file").get(0);
                MultivaluedMap<String, String> headers = filePart.getHeaders();

                // Extrahiere den Dateinamen aus dem Content-Disposition-Header
                String fileName = extractFileName(headers);
                if (documentMongoDB.documentName == null || documentMongoDB.documentName.isEmpty()) {
                    documentMongoDB.documentName = fileName; // Falls kein Name übergeben wurde, verwende den Dateinamen
                }

                // Datei als Byte-Array speichern
                InputStream inputStream = filePart.getBody(InputStream.class, null);
                byte[] fileBytes = inputStream.readAllBytes(); // Datei in Byte-Array umwandeln

                documentMongoDB.content = new Binary(fileBytes);
            }

            documentMongoDB.association = "GROUP";
            documentRepository.persist(documentMongoDB);
            System.out.println("✅ Datei erfolgreich gespeichert!");

        } catch (Exception e) {
            throw new RuntimeException("❌ Fehler beim Speichern des Dokuments", e);
        }
    }




}
