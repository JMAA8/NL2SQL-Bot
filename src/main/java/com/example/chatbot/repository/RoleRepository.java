package com.example.chatbot.repository;

import com.example.chatbot.entity.Role;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RoleRepository implements PanacheRepository<Role> {

    // Rolle anhand des Namens finden
    public Role findByName(String roleName) {
        return find("roleName", roleName).firstResult();
    }

    // Optional zurückgeben, falls keine Rolle gefunden wird
    public Optional<Role> findOptionalByName(String roleName) {
        return Optional.ofNullable(findByName(roleName));
    }

    // Alle Rollen abrufen
    public List<Role> getAllRoles() {
        return listAll();
    }
}
