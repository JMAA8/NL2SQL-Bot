package com.example.chatbot.service;

import com.example.chatbot.entity.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ChatContextService {

    public String buildContext(User user, String prompt) {
        StringBuilder context = new StringBuilder();

        // Beispiel: Kontext basierend auf Benutzerrollen
        context.append("Benutzer: ").append(user.getUsername()).append("\n");
        context.append("Rollen: ").append(user.getRoles()).append("\n");
        context.append("Frage: ").append(prompt);

        return context.toString();
    }
}
