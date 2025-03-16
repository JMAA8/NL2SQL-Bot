package com.example.chatbot.repository;

import com.example.chatbot.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public User findByUsername(String username) {
        return find("username", username).firstResult();
    }

    // Findet alle Benutzer anhand einer Liste von IDs
    public List<User> findUsersByIds(List<Long> userIds) {
        return list("id IN ?1", userIds);
    }

    //Findet einen Benutzer
    public User findById(Long userId) {
        return find("id", userId).firstResult();
    }

}

