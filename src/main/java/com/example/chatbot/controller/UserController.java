package com.example.chatbot.controller;

import com.example.chatbot.entity.User;
import com.example.chatbot.service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"USER", "ADMIN"}) // Zugriff für Benutzer und Administratoren
public class UserController {

    @Inject
    UserService userService;

    // Eigene Profildaten abrufen
    @GET
    @Path("/profile/{id}")
    public Response getProfile(@PathParam("id") Long id) {
        return userService.getUserById(id)
                .map(user -> Response.ok(user).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    // Profil aktualisieren
    @PUT
    @Path("/profile/{id}")
    public Response updateProfile(@PathParam("id") Long id, User updatedUser) {
        try {
            userService.updateUserProfile(id, updatedUser);
            return Response.ok("Profil aktualisiert").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    // Neuen Benutzer hinzufügen
    @POST
    @Path("/add")
    @RolesAllowed("ADMIN") // Nur Administratoren dürfen neue Benutzer hinzufügen
    public Response addUser(User newUser) {
        try {
            userService.addUser(newUser);
            return Response.status(Response.Status.CREATED)
                    .entity("Benutzer erfolgreich erstellt: " + newUser.getUsername())
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    // Rolle zu einem Benutzer hinzufügen
    @PUT
    @Path("/user/{id}/add-role")
    @RolesAllowed("ADMIN")
    public Response addRoleToUser(@PathParam("id") Long id, @QueryParam("roleName") String roleName) {
        try {
            userService.assignRoleToUser(id, roleName);
            return Response.ok("Rolle erfolgreich hinzugefügt").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    // Rolle von einem Benutzer entfernen
    @DELETE
    @Path("/user/{id}/remove-role")
    @RolesAllowed("ADMIN")
    public Response removeRoleFromUser(@PathParam("id") Long id, @QueryParam("roleName") String roleName) {
        try {
            userService.removeRoleFromUser(id, roleName);
            return Response.ok("Rolle erfolgreich entfernt").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}
