package com.example.chatbot.chat;

import com.example.chatbot.entity.Chat;
import com.example.chatbot.entity.ChatMessage;
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
    ChatService chatService; // Neu: Service für Chat-Verwaltung

    // Neue Nachricht senden (neuer oder bestehender Chat)
    @POST
    public Response sendMessage(ChatRequest request) {
        String llmResponse = llmService.getResponse(request.getPrompt());
        ChatMessage message = chatService.saveMessage(request.getUserId(), request.getChatId(), request.getPrompt(), llmResponse);
        return Response.ok(message).build();
    }

    // Alle Chats eines Benutzers abrufen
    @GET
    @Path("/user")
    public Response getUserChats(@QueryParam("userId") Long userId) {
        List<Chat> chats = chatService.getChatsByUserId(userId);
        return Response.ok(chats).build();
    }

    // Nachrichten eines spezifischen Chats abrufen
    @GET
    @Path("/{chatId}")
    public Response getChatMessages(@PathParam("chatId") Long chatId) {
        List<ChatMessage> messages = chatService.getMessagesByChatId(chatId);
        return Response.ok(messages).build();
    }
}
