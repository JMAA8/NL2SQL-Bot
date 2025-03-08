package com.example.chatbot.service;

import com.example.chatbot.DTO.GroupDTO;
import com.example.chatbot.entity.Group;
import com.example.chatbot.entity.GroupUser;
import com.example.chatbot.entity.User;
import com.example.chatbot.repository.GroupRepository;
import com.example.chatbot.repository.GroupUserRepository;
import com.example.chatbot.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class GroupService {

    @Inject
    GroupRepository groupRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    GroupUserRepository groupUserRepository;

    // Gruppe erstellen
    @Transactional
    public Group createGroup(String groupName, Long ownerId, String password) {
        System.out.println("GroupService - create - " + groupName + "/ " + ownerId + "/ "+ password);

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
    @Transactional
    public void addUserToGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId);
        User user = userRepository.findById(userId);

        if (group == null || user == null) {
            throw new IllegalArgumentException("Gruppe oder Benutzer nicht gefunden.");
        }

        GroupUser groupUser = new GroupUser();
        groupUser.setGroup(group);
        groupUser.setUser(user);

        groupUserRepository.persist(groupUser);
    }

    // Benutzer aus einer Gruppe entfernen
    @Transactional
    public void removeUserFromGroup(Long groupId, Long userId) {
        Optional<GroupUser> groupUserOpt = groupUserRepository.find("group.id = ?1 and user.id = ?2", groupId, userId).firstResultOptional();

        if (groupUserOpt.isPresent()) {
            groupUserRepository.delete(groupUserOpt.get());
        } else {
            throw new IllegalArgumentException("Benutzer nicht in dieser Gruppe.");
        }
    }

    // Suche nach Gruppe per ID oder Name
    public Optional<Group> findGroup(String identifier) {
        try {
            Long groupId = Long.parseLong(identifier);
            return groupRepository.findByIdOptional(groupId);
        } catch (NumberFormatException e) {
            return groupRepository.find("groupName", identifier).firstResultOptional();
        }
    }

    // Gruppe beitreten, wenn Passwort stimmt
    @Transactional
    public boolean joinGroup(Long groupId, Long userId, String password) {
        System.out.println("GroupService - joinGroup - groupId: " + groupId + ", userId: " + userId);

        if (groupId == null || userId == null) {
            throw new IllegalArgumentException("groupId und userId dürfen nicht null sein");
        }

        Optional<Group> groupOpt = groupRepository.findByIdOptional(groupId);
        if (groupOpt.isEmpty()) {
            System.out.println("Gruppe nicht gefunden mit ID: " + groupId);
            return false;
        }

        Group group = groupOpt.get();

        // Prüfe Passwort
        if (!group.getPassword().equals(password)) {
            System.out.println("Falsches Passwort für Gruppe: " + group.getGroupName());
            return false;
        }

        Optional<User> userOpt = userRepository.findByIdOptional(userId);
        if (userOpt.isEmpty()) {
            System.out.println("User nicht gefunden mit ID: " + userId);
            return false;
        }

        User user = userOpt.get();

        // Überprüfen, ob der Benutzer bereits in der Gruppe ist
        Optional<GroupUser> existingMembership = groupUserRepository.find("group.id = ?1 and user.id = ?2", groupId, userId)
                .firstResultOptional();
        if (existingMembership.isPresent()) {
            System.out.println("User ist bereits Mitglied der Gruppe.");
            return false;
        }

        // Benutzer zur Gruppe hinzufügen
        GroupUser newMembership = new GroupUser();
        newMembership.setGroup(group);
        newMembership.setUser(user);
        groupUserRepository.persist(newMembership);

        System.out.println("User erfolgreich zur Gruppe hinzugefügt.");
        return true;
    }

    // Gruppe löschen
    @Transactional
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

    // Alle Gruppen eines Users abrufen (mit richtiger Query)
    @Transactional
    public List<GroupDTO> getGroupsByUserId(Long userId) {
        return groupRepository.getEntityManager().createQuery(
                        "SELECT NEW com.example.chatbot.DTO.GroupDTO(g.id, g.groupName) FROM Group g JOIN g.members gu WHERE gu.user.id = :userId", GroupDTO.class)
                .setParameter("userId", userId)
                .getResultList();
    }


    // Alle Gruppen abrufen
    public List<GroupDTO> getAllGroups() {
        return groupRepository.listAll().stream()
                .map(group -> new GroupDTO(group.getId(), group.getGroupName()))
                .collect(Collectors.toList());
    }

    public List<User> getUsersByGroupId(Long groupId) {
        return groupRepository.findByIdOptional(groupId)
                .map(group -> Optional.ofNullable(group.getMembers())
                        .orElse(Collections.emptySet())
                        .stream()
                        .map(GroupUser::getUser)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList()); // Falls Gruppe nicht existiert, gib eine leere Liste zurück
    }

    public boolean isUserOwner(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Gruppe nicht gefunden.");
        }
        return group.getOwner().getId().equals(userId);
    }


}
