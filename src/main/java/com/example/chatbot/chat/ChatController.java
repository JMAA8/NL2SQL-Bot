package com.example.chatbot.chat;

import com.example.chatbot.entityMongoDB.Chat;
import com.example.chatbot.entityMongoDB.ChatMessage;
import com.example.chatbot.llm.LLMService;
import com.example.chatbot.service.ChatService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatController {

    @Inject
    LLMService llmService;

    @Inject
    ChatService chatService;

    // Abruf aller Chats eines Benutzers (Liste auf der linken Seite)
    @GET
    @Path("/user/{userId}")
    public Response getUserChats(@PathParam("userId") Long userId) {

        List<Chat> chats = chatService.getChatsByUserId(userId);
        System.out.println("ChatController - /user - ChatListe: " + chats);
        return Response.ok(chats).build();
    }

    // Abruf aller Nachrichten eines spezifischen Chats
    @GET
    @Path("/{chatId}")
    public Response getChatMessages(@PathParam("chatId") String chatId) {
        List<ChatMessage> messages = chatService.getMessagesByChatId(chatId);
        return Response.ok(messages).build();
    }

    // Neuen Chat starten oder Nachricht in bestehenden Chat senden
    @POST
    @Path("/message")
    public Response sendMessage(ChatRequest request) {
        System.out.println("Chat Controller - /message - Request: " + request);
        Chat chat = chatService.handleChatMessage(request.getUserId(), request.getChatId(), request.getPrompt());
        System.out.println("Chat Controller - /message - Chat: " + chat.getChatId());
        return Response.ok(chat).build();
    }

    // Titel eines Chats ändern
    @PUT
    @Path("/{chatId}/title")
    @Consumes(MediaType.APPLICATION_JSON) // JSON akzeptieren
    public Response updateChatTitle(@PathParam("chatId") String chatId, Map<String, String> body) {
        String newTitle = body.get("newTitle"); // Titel aus dem Request-Body extrahieren
        if (newTitle == null || newTitle.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Titel darf nicht leer sein.")
                    .build();
        }

        Chat updatedChat = chatService.updateChatTitle(chatId, newTitle);
        if (updatedChat == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Chat mit der ID " + chatId + " wurde nicht gefunden.")
                    .build();
        }

        return Response.ok(updatedChat).build();
    }


    // Einen Chat löschen
    @DELETE
    @Path("/{chatId}")
    public Response deleteChat(@PathParam("chatId") String chatId) {
        chatService.deleteChat(chatId);
        return Response.ok("Chat gelöscht").build();
    }
}
