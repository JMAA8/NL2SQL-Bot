package com.example.chatbot.controller;

import com.example.chatbot.DTO.GroupCreate;
import com.example.chatbot.DTO.GroupDTO;
import com.example.chatbot.DTO.GroupJoinRequest;
import com.example.chatbot.entity.Group;
import com.example.chatbot.entity.User;
import com.example.chatbot.service.GroupService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupController {

    @Inject
    GroupService groupService;

    // Gruppe erstellen
    @POST
    @RolesAllowed({"ADMIN", "ADVANCED_USER"})
    public Response createGroup(GroupCreate request) {

        System.out.println(
                "GroupController - create - Name/Owner/Password: " + request.groupNameCreated + "/ " + request.ownerIdCreated + "/ "+ request.groupPasswordCreated
        );
        if (request.groupNameCreated == null || request.groupPasswordCreated == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Name und Passwort erforderlich").build();
        }

        groupService.createGroup(request.groupNameCreated, request.ownerIdCreated, request.groupPasswordCreated);
        return Response.ok("Gruppe erstellt").build();
    }

    // Benutzer zu Gruppe hinzufügen
    @PUT
    @Path("/{groupId}/add-user/{userId}")
    @RolesAllowed({"ADMIN", "ADVANCED_USER"})
    public Response addUserToGroup(@PathParam("groupId") Long groupId, @PathParam("userId") Long userId) {
        try {
            groupService.addUserToGroup(groupId, userId);
            return Response.ok("Benutzer erfolgreich zur Gruppe hinzugefügt.").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    // Benutzer aus Gruppe entfernen
    @PUT
    @Path("/{groupId}/remove-user/{userId}")
    @RolesAllowed({"ADMIN", "ADVANCED_USER"})
    public Response removeUserFromGroup(@PathParam("groupId") Long groupId, @PathParam("userId") Long userId) {
        try {
            groupService.removeUserFromGroup(groupId, userId);
            return Response.ok("Benutzer erfolgreich aus der Gruppe entfernt.").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    // Gruppe löschen
    @DELETE
    @Path("/{groupId}")
    @RolesAllowed("ADMIN")
    public Response deleteGroup(@PathParam("groupId") Long groupId) {
        try {
            groupService.deleteGroup(groupId);
            return Response.ok("Gruppe erfolgreich gelöscht.").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    //Gruppen des Users abrufen
    @GET
    @Path("/joined/{userId}")
    public Response getJoinedGroups(@PathParam("userId") Long userId) {
        List<GroupDTO> groups = groupService.getGroupsByUserId(userId);
        return Response.ok(groups).build();
    }

    /*
    @POST
    @Path("/join")
    public Response joinGroup(Map<String, String> joinData) {
        Long userId = Long.parseLong(joinData.get("userId"));
        Long groupId = Long.parseLong(joinData.get("groupId"));
        String password = joinData.get("password");

        boolean success = groupService.joinGroup(userId, groupId, password);
        if (success) {
            return Response.ok("Gruppe erfolgreich beigetreten").build();
        }
        return Response.status(Response.Status.FORBIDDEN).entity("Falsches Passwort oder Gruppe nicht gefunden").build();
    }
     */

    // Suche nach Gruppe per ID oder Name
    @GET
    @Path("/search/{identifier}")
    public Response searchGroup(@PathParam("identifier") String identifier) {
        Optional<Group> group = groupService.findGroup(identifier);
        return group.map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    // Gruppe mit Passwort beitreten
    @POST
    @Path("/join")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response joinGroup(GroupJoinRequest request) {
        System.out.println("Join Request erhalten: groupId=" + request.groupId + ", username=" + request.userId);

        if (request.groupId == null || request.userId == null || request.password == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Fehlende Parameter!").build();
        }

        boolean success = groupService.joinGroup(request.groupId, request.userId, request.password);

        if (success) {
            return Response.ok("Beitritt erfolgreich!").build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Falsches Passwort oder Gruppe nicht gefunden!").build();
        }
    }

    // Alle Gruppen abrufen
    @GET
    @RolesAllowed({"ADMIN"})
    public Response getAllGroups() {
        List<GroupDTO> groups = groupService.getAllGroups();
        return Response.ok(groups).build();
    }


    // Gruppe und Mitglieder abrufen
    @GET
    @Path("/{groupId}")
    @RolesAllowed({"ADMIN", "ADVANCED_USER"})
    public Response getGroup(@PathParam("groupId") Long groupId) {
        System.out.println("GroupController - getGroup");
        return groupService.getGroupById(groupId)
                .map(group -> Response.ok(group).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).entity("Gruppe nicht gefunden.").build());
    }

    @GET
    @Path("/{groupId}/isOwner")
    @RolesAllowed({"ADMIN", "ADVANCED_USER"})
    public Response checkIfUserIsOwner(@PathParam("groupId") Long groupId, @QueryParam("userId") Long userId) {
        boolean isOwner = groupService.isUserOwner(groupId, userId);
        return Response.ok(Map.of("isOwner", isOwner)).build();
    }
    @GET
    @Path("/{groupId}/users")
    @RolesAllowed({"ADMIN", "ADVANCED_USER"})
    public Response getUsersByGroup(@PathParam("groupId") Long groupId) {
        List<User> users = groupService.getUsersByGroupId(groupId);
        return Response.ok(users).build();
    }

}
