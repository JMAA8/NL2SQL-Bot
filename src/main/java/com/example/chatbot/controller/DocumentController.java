package com.example.chatbot.controller;

import com.example.chatbot.entity.Document;
import com.example.chatbot.service.DocumentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/documents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DocumentController {

    @Inject
    DocumentService documentService;

    @POST
    public void saveDocument(Document document) {
        documentService.saveDocument(document);
    }

    @GET
    @Path("/{userId}")
    public List<Document> getDocumentsByUserId(@PathParam("userId") String userId) {
        return documentService.getDocumentsByUserId(userId);
    }
}
