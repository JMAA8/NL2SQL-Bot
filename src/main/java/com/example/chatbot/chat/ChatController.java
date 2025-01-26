package com.example.chatbot.chat;

import com.example.chatbot.entityMongoDB.Chat;
import com.example.chatbot.llm.LLMService;
import com.example.chatbot.service.ChatService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

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
        return Response.ok(chats).build();
    }

    // Abruf aller Nachrichten eines spezifischen Chats
    @GET
    @Path("/{chatId}")
    public Response getChatMessages(@PathParam("chatId") Long chatId) {
        List<Chat> messages = chatService.getMessagesByChatId(chatId);
        return Response.ok(messages).build();
    }

    // Neuen Chat starten oder Nachricht in bestehenden Chat senden
    @POST
    @Path("/message")
    public Response sendMessage(ChatRequest request) {
        System.out.println("Chat Controller - /message - Request: " + request);
        Chat chat = chatService.handleChatMessage(request.getUserId(), request.getChatId(), request.getPrompt());
        System.out.println("Chat Controller - /message - Chat: " + chat);
        return Response.ok(chat).build();
    }

    // Titel eines Chats ändern
    @PUT
    @Path("/{chatId}/title")
    public Response updateChatTitle(@PathParam("chatId") String chatId, @QueryParam("newTitle") String newTitle) {
        Chat updatedChat = chatService.updateChatTitle(chatId, newTitle);
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
