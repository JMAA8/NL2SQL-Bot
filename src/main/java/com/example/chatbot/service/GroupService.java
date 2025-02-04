package com.example.chatbot.service;

import com.example.chatbot.entity.Group;
import com.example.chatbot.entity.User;
import com.example.chatbot.repository.GroupRepository;
import com.example.chatbot.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GroupService {

    @Inject
    GroupRepository groupRepository;

    @Inject
    UserRepository userRepository;

    // Gruppe erstellen
    @Transactional
    public Group createGroup(String groupName, Long ownerId, String password) {
        System.out.println(
                "GroupService - create - " + groupName + "/ " + ownerId + "/ "+ password
        );
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID ist null!");
        }
        User owner = userRepository.findById(ownerId);
        if (owner == null) {
            throw new IllegalArgumentException("Besitzer nicht gefunden.");
        }

        Group group = new Group();
        group.setGroupName(groupName);
        group.setOwner(owner);
        group.setPassword(password);
        groupRepository.persist(group);
        return group;
    }

    // Benutzer zu einer Gruppe hinzufügen
    public void addUserToGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId);
        User user = userRepository.findById(userId);

        if (group == null || user == null) {
            throw new IllegalArgumentException("Gruppe oder Benutzer nicht gefunden.");
        }

        group.getMembers().add(user);
        groupRepository.persist(group);
    }

    // Benutzer aus einer Gruppe entfernen
    public void removeUserFromGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId);
        User user = userRepository.findById(userId);

        if (group == null || user == null) {
            throw new IllegalArgumentException("Gruppe oder Benutzer nicht gefunden.");
        }

        group.getMembers().remove(user);
        groupRepository.persist(group);
    }

    @Transactional
    public boolean joinGroup(Long userId, Long groupId, String password) {
        User user = userRepository.findById(userId);
        Group group = groupRepository.findById(groupId);

        if (group == null || user == null) {
            return false;
        }

        // Prüfen, ob der User bereits in der Gruppe ist
        if (group.getMembers().contains(user)) {
            return true;
        }

        // Passwort überprüfen
        if (group.getPassword().equals(password)) {
            group.getMembers().add(user);
            return true;
        }

        return false; // Falsches Passwort
    }

    // Gruppe löschen
    public void deleteGroup(Long groupId) {
        Group group = groupRepository.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Gruppe nicht gefunden.");
        }
        groupRepository.delete(group);
    }

    // Gruppe abrufen
    public Optional<Group> getGroupById(Long groupId) {
        return Optional.ofNullable(groupRepository.findById(groupId));
    }

    @Transactional
    public List<Group> getGroupsByUserId(Long userId) {
        return groupRepository.findGroupsByUserId(userId);
    }

    // Alle Gruppen abrufen
    public List<Group> getAllGroups() {
        return groupRepository.listAll();
    }
}
