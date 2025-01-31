package com.example.chatbot.controller;

import com.example.chatbot.entity.Group;
import com.example.chatbot.entity.User;
import com.example.chatbot.service.GroupService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupController {

    @Inject
    GroupService groupService;

    // Gruppe erstellen
    @POST
    @RolesAllowed({"ADMIN", "ADVANCED_USER"})
    public Response createGroup(@QueryParam("groupName") String groupName, @QueryParam("ownerId") Long ownerId) {
        try {
            Group group = groupService.createGroup(groupName, ownerId);
            return Response.status(Response.Status.CREATED).entity(group).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
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

    @GET
    @Path("/joined/{userId}")
    public Response getJoinedGroups(@PathParam("userId") Long userId) {
        List<Group> groups = groupService.getGroupsByUserId(userId);
        if (groups.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Keine Gruppen gefunden").build();
        }
        return Response.ok(groups).build();
    }

    // Alle Gruppen abrufen
    @GET
    @RolesAllowed({"ADMIN"})
    public Response getAllGroups() {
        return Response.ok(groupService.getAllGroups()).build();
    }

    // Gruppe und Mitglieder abrufen
    @GET
    @Path("/{groupId}")
    @RolesAllowed({"ADMIN", "ADVANCED_USER"})
    public Response getGroup(@PathParam("groupId") Long groupId) {
        return groupService.getGroupById(groupId)
                .map(group -> Response.ok(group).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).entity("Gruppe nicht gefunden.").build());
    }
}
