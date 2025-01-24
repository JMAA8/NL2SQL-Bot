package com.example.chatbot.service;

import com.example.chatbot.entity.Chat;
import com.example.chatbot.entity.ChatMessage;
import com.example.chatbot.repository.ChatRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ChatService {
    @Inject
    ChatRepository ChatRepository;


    //Erstellen/Speichern/bearbeiten muss überarbeitet werden!!!!


    // Speichert eine neue Nachricht und erstellt ggf. einen neuen Chat

   /*
    public ChatMessage saveMessage(Long userId, Long chatId, String prompt, String response) {
        // Logik zum Speichern der Nachricht (Datenbank/MongoDB-Integration später)
        ChatMessage message = new ChatMessage(chatId, userId, prompt, response);
        ChatRepository.persist(message);
        return message;
    }
    */


    // Alle Chats eines Benutzers abrufen
    public List<Chat> getChatsByUserId(Long userId) {
        // Hier Datenbankabfrage hinzufügen
        return ChatRepository.find("userId", userId).list();
    }

    // Nachrichten eines spezifischen Chats abrufen
    public List<ChatMessage> getMessagesByChatId(Long chatId) {
        // Hier Datenbankabfrage hinzufügen
        return new ArrayList<>(); // Platzhalter
    }
}
