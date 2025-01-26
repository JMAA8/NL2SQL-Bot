package com.example.chatbot.repository;

import com.example.chatbot.entity.Chat;
import com.example.chatbot.entity.ChatMessage;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import static io.quarkus.hibernate.orm.panache.Panache.getEntityManager;


@ApplicationScoped
public class ChatRepository implements PanacheMongoRepository<Chat> {

    public List<Chat> findByUserId(Long userId) {
        return list("userId", userId);
    }

    public Chat findByChatId(Long chatId) {
        return find("chatId", chatId).firstResult();
    }

    public List<Chat> findMessagesByChatId(Long chatId) {
        return list("chatId", chatId);
    }

    public void persistMessage(ChatMessage message) {
        getEntityManager().persist(message);
    }
}
