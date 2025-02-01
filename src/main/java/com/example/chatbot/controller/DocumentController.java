package com.example.chatbot.controller;

import com.example.chatbot.entity.Group;
import com.example.chatbot.entityMongoDB.Document;
import com.example.chatbot.service.DocumentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.util.Collections;
import java.util.List;

@Path("/api/documents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DocumentController {

    @Inject
    DocumentService documentService;

    // Neues Dokument speichern
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response saveDocument(@MultipartForm MultipartFormDataInput input) {
        try {
            System.out.println("DocumentController - saveDocument - Input: " + input);
            documentService.saveDocument(input);
            return Response.ok("Dokument erfolgreich gespeichert").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Speichern des Dokuments: " + e.getMessage()).build();
        }
    }

    // Dokumente eines Benutzers abrufen
    @GET
    @Path("/{userId}")
    public Response getDocumentsByUserId(@PathParam("userId") Long userId, @QueryParam("search") String search) {
        System.out.println("DocumentController - Get by Id - userId: " + userId);

        List<Document> documents;

        if (search != null && !search.isEmpty()) {
            System.out.println("DocumentController - Get by Id - Search: " + search);
            documents = documentService.searchDocuments(userId, search);
        } else {
            documents = documentService.getDocumentsByUserId(userId);
        }

        if (documents.isEmpty()) {
            System.out.println("DocumentController - Get - Keine Dokumente vorhanden: " + documents);
            return Response.ok(Collections.emptyList()).build(); // Leere Liste zurückgeben
        }

        return Response.ok(documents).build();
    }


    @DELETE
    @Path("/{documentId}")
    public Response deleteDocument(@PathParam("documentId") String documentId) {
        documentService.deleteDocument(documentId);
        return Response.ok("Dokument gelöscht").build();
    }
}
