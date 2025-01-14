package com.example.chatbot.chat;

import com.example.chatbot.llm.LLMService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatController {

    @Inject
    LLMService llmService;

    @POST
    public Response chat(ChatRequest request) {
        String response = llmService.getResponse(request.getPrompt());
        return Response.ok(new ChatResponse(response)).build();
    }
}
