package com.example.chatbot.controller;

import com.example.chatbot.entity.User;
import com.example.chatbot.repository.UserRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN") // Nur Benutzer mit der Rolle ADMIN dürfen zugreifen
public class AdminController {

    @Inject
    UserRepository userRepository;

    // Liste aller Benutzer abrufen
    @GET
    @Path("/users")
    public Response getAllUsers() {
        List<User> users = userRepository.listAll();
        return Response.ok(users).build();
    }

    // Benutzer löschen
    @DELETE
    @Path("/user/{id}")
    public Response deleteUser(@PathParam("id") Long id) {
        User user = userRepository.findById(id);
        if (user != null) {
            userRepository.delete(user);
            return Response.ok("Benutzer gelöscht").build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    // Benutzerrolle aktualisieren
    @PUT
    @Path("/user/{id}/role")
    public Response updateUserRole(@PathParam("id") Long id, @QueryParam("role") String role) {
        User user = userRepository.findById(id);
        if (user != null) {
            // TODO: Rolle zuweisen (abhängig von deiner Rollenlogik)
            return Response.ok("Rolle aktualisiert").build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}

