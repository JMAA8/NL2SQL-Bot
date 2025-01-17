package com.example.chatbot.service;

import com.example.chatbot.entity.Role;
import com.example.chatbot.entity.User;
import com.example.chatbot.repository.RoleRepository;
import com.example.chatbot.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    // Benutzer registrieren
    public User registerUser(String username, String password) {
        Role basicRole = roleRepository.findByName("BASIC_USER");
        if (basicRole == null) {
            throw new IllegalStateException("BASIC_USER Rolle ist nicht in der Datenbank vorhanden.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRoles(Set.of(basicRole));
        userRepository.persist(user);
        return user;
    }

    // Benutzerprofil aktualisieren
    public void updateUserProfile(Long userId, User updatedUser) {
        User existingUser = userRepository.findById(userId);
        if (existingUser == null) {
            throw new IllegalArgumentException("Benutzer existiert nicht.");
        }

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setPassword(updatedUser.getPassword());
        userRepository.persist(existingUser);
    }

    // Rolle zu einem Benutzer hinzufügen
    public void assignRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId);
        Role role = roleRepository.findByName(roleName);

        if (user != null && role != null) {
            user.getRoles().add(role);
            userRepository.persist(user);
        } else {
            throw new IllegalArgumentException("Benutzer oder Rolle existiert nicht.");
        }
    }

    // Rolle von einem Benutzer entfernen
    public void removeRoleFromUser(Long userId, String roleName) {
        User user = userRepository.findById(userId);
        Role role = roleRepository.findByName(roleName);

        if (user != null && role != null) {
            user.getRoles().remove(role);
            userRepository.persist(user);
        } else {
            throw new IllegalArgumentException("Benutzer oder Rolle existiert nicht.");
        }
    }

    // Alle Benutzer mit einer bestimmten Rolle abrufen
    public Set<User> getUsersByRole(String roleName) {
        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Rolle existiert nicht.");
        }
        return userRepository.listAll().stream()
                .filter(user -> user.getRoles().contains(role))
                .collect(Collectors.toSet());
    }

    // Benutzer abrufen nach ID
    public Optional<User> getUserById(Long userId) {
        return Optional.ofNullable(userRepository.findById(userId));
    }

    // Benutzer abrufen nach Benutzername
    public Optional<User> getUserByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    // Neuen Benutzer hinzufügen
    public void addUser(User newUser) {
        if (userRepository.findByUsername(newUser.getUsername()) != null) {
            throw new IllegalArgumentException("Ein Benutzer mit diesem Benutzernamen existiert bereits.");
        }
        userRepository.persist(newUser);
    }

    // Alle Benutzer abrufen
    public List<User> getAllUsers() {
        return userRepository.listAll();
    }

    // Benutzer löschen nach ID
    public boolean deleteUserById(Long userId) {
        User user = userRepository.findById(userId);
        if (user != null) {
            userRepository.delete(user);
            return true;
        } else {
            throw new IllegalArgumentException("Benutzer mit der ID existiert nicht.");
        }
    }
}
