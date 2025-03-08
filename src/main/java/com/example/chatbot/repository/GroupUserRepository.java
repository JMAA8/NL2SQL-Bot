package com.example.chatbot.repository;

import com.example.chatbot.entity.GroupUser;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GroupUserRepository implements PanacheRepository<GroupUser> {
    // Findet alle Benutzer-IDs einer Gruppe
    public List<Long> findUserIdsByGroupId(Long groupId) {
        return find("group.id", groupId).stream()
                .map(groupUser -> groupUser.getUser().getId()) // Nur die Benutzer-IDs zurückgeben
                .collect(Collectors.toList());
    }
}

