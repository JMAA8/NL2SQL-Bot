package com.example.chatbot.repository;

import com.example.chatbot.entity.Role;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RoleRepository implements PanacheRepository<Role> {

    public Role findByName(String roleName) {
        return find("roleName", roleName).firstResult();
    }
}
