package com.example.chatbot.service;

import com.example.chatbot.entity.Chat;
import com.example.chatbot.entity.ChatMessage;
import com.example.chatbot.repository.ChatRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class ChatService {

    @Inject
    ChatRepository chatRepository;

    // Nachricht speichern und Chat verwalten
    public Chat handleChatMessage(Long userId, Long chatId, String prompt) {
        Chat chat = (chatId != null) ? chatRepository.findByChatId(chatId) : null;

        if (chat == null) {
            chat = new Chat();
            chat.setUserId(userId);
            chat.setTitle(prompt.split(" ")[0]); // Erstes Wort als Titel
            chatRepository.persist(chat);
        }

        ChatMessage message = new ChatMessage(chat.getChatId(), userId, prompt, "Antwort von LLM"); // Dummy LLM-Antwort
        chatRepository.persistMessage(message);

        chat.getMessages().add(message);
        chatRepository.update(chat);
        return chat;
    }

    // Titel eines Chats aktualisieren
    public Chat updateChatTitle(Long chatId, String newTitle) {
        Chat chat = chatRepository.findByChatId(chatId);
        if (chat != null) {
            chat.setTitle(newTitle);
            chatRepository.update(chat);
        }
        return chat;
    }

    // Chat löschen
    public void deleteChat(Long chatId) {
        Chat chat = chatRepository.findByChatId(chatId);
        if (chat != null) {
            chatRepository.delete(chat);
        }
    }

    // Alle Chats eines Benutzers abrufen
    public List<Chat> getChatsByUserId(Long userId) {
        return chatRepository.findByUserId(userId);
    }

    // Nachrichten eines spezifischen Chats abrufen
    public List<Chat> getMessagesByChatId(Long chatId) {
        return chatRepository.findMessagesByChatId(chatId);
    }
}