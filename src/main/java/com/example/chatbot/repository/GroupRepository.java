package com.example.chatbot.repository;

import com.example.chatbot.entity.Group;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class GroupRepository implements PanacheRepository<Group> {
    public List<Group> findGroupsByUserId(Long userId) {
        return find("SELECT g FROM Group g JOIN g.members m WHERE m.id = ?1", userId).list();
    }
    public Group findById(Long groupId) {
        return find("id", groupId).firstResult();
    }
}

