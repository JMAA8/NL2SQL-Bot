package com.example.chatbot.repository;

import com.example.chatbot.entity.Chat;
import com.example.chatbot.entity.ChatMessage;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ChatRepository implements PanacheMongoRepository<Chat> {}
