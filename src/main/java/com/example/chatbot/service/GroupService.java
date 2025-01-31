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
    public Group createGroup(String groupName, Long ownerId) {
        User owner = userRepository.findById(ownerId);
        if (owner == null) {
            throw new IllegalArgumentException("Besitzer nicht gefunden.");
        }

        Group group = new Group();
        group.setGroupName(groupName);
        group.setOwner(owner);
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
