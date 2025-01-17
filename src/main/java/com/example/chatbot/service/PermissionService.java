package com.example.chatbot.service;

import com.example.chatbot.entity.Permission;
import com.example.chatbot.entity.Role;
import com.example.chatbot.entity.User;
import com.example.chatbot.repository.PermissionRepository;
import com.example.chatbot.repository.RoleRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class PermissionService {

    @Inject
    PermissionRepository permissionRepository;

    @Inject
    RoleRepository roleRepository;

    // Überprüfen, ob ein Benutzer eine bestimmte Berechtigung hat
    public boolean hasPermission(User user, String permissionName) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getPermissionName().equals(permissionName));
    }

    // Berechtigung zu einer Rolle hinzufügen
    public void addPermissionToRole(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName);
        Permission permission = permissionRepository.findByName(permissionName);

        if (role != null && permission != null) {
            role.getPermissions().add(permission);
            roleRepository.persist(role);
        } else {
            throw new IllegalArgumentException("Rolle oder Berechtigung existiert nicht.");
        }
    }

    // Berechtigung von einer Rolle entfernen
    public void removePermissionFromRole(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName);
        Permission permission = permissionRepository.findByName(permissionName);

        if (role != null && permission != null) {
            role.getPermissions().remove(permission);
            roleRepository.persist(role);
        } else {
            throw new IllegalArgumentException("Rolle oder Berechtigung existiert nicht.");
        }
    }

    // Liste aller Berechtigungen abrufen
    public List<Permission> getAllPermissions() {
        return permissionRepository.getAllPermissions();
    }
}
