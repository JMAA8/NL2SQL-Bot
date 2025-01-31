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
@RolesAllowed({"BASIC_USER", "ADVANCED_USER", "ADMIN"}) // Zugriff für Benutzer und Administratoren
public class UserController {

    @Inject
    UserService userService;

    // Eigene Profildaten abrufen
    @GET
    @Path("/profile/{id}")
    public Response getProfile(@PathParam("id") Long id) {
        System.out.println("UserController - profile - Id: " + id);
        return userService.getUserById(id)
                .map(user -> Response.ok(user).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    // Profil aktualisieren
    @PUT
    @Path("/updateProfile/{id}")
    public Response updateProfile(@PathParam("id") Long id, User updatedUser) {
        System.out.println("UserController - UpdateProfil - Id: " + id);
        try {
            userService.updateUserProfile(id, updatedUser);
            return Response.ok("Profil aktualisiert").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }



}
