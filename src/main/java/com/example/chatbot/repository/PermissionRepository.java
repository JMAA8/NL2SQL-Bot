package com.example.chatbot.repository;

import com.example.chatbot.entity.Permission;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PermissionRepository implements PanacheRepository<Permission> {

    // Berechtigung anhand des Namens finden
    public Permission findByName(String permissionName) {
        return find("permissionName", permissionName).firstResult();
    }

    // Alle Berechtigungen abrufen
    public List<Permission> getAllPermissions() {
        return listAll();
    }
}
