package com.example.chatbot.controller;

import com.example.chatbot.entityMongoDB.Document;
import com.example.chatbot.service.DocumentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

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
    public List<Document> getDocumentsByUserId(@PathParam("userId") String userId, @QueryParam("search") String search) {
        if (search != null && !search.isEmpty()) {
            return documentService.searchDocuments(userId, search);
        }
        return documentService.getDocumentsByUserId(userId);
    }
}
