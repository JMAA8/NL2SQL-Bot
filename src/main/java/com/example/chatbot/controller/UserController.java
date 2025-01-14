package com.example.chatbot.controller;

import com.example.chatbot.entity.Role;
import com.example.chatbot.entity.User;
import com.example.chatbot.repository.RoleRepository;
import com.example.chatbot.repository.UserRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashSet;
import java.util.Set;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"USER", "ADMIN"}) // Zugriff für Benutzer und Administratoren
public class UserController {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    // Eigene Profildaten abrufen
    @GET
    @Path("/profile")
    public Response getProfile(@QueryParam("username") String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return Response.ok(user).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    // Profil aktualisieren
    @PUT
    @Path("/profile")
    public Response updateProfile(@QueryParam("username") String username, User updatedUser) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setUsername(updatedUser.getUsername());
            user.setPassword(updatedUser.getPassword());
            userRepository.persist(user);
            return Response.ok("Profil aktualisiert").build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    // Neuen Benutzer hinzufügen
    @POST
    @Path("/add")
    @RolesAllowed("ADMIN") // Nur Administratoren dürfen neue Benutzer hinzufügen
    public Response addUser(User newUser) {
        if (userRepository.find("username", newUser.getUsername()).firstResult() != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Ein Benutzer mit diesem Benutzernamen existiert bereits.").build();
        }

        // Rollen verknüpfen, falls vorhanden
        Set<Role> roles = new HashSet<>();
        if (newUser.getRoles() != null) {
            for (Role role : newUser.getRoles()) {
                Role existingRole = roleRepository.find("roleName", role.getRoleName()).firstResult();
                if (existingRole != null) {
                    roles.add(existingRole);
                }
            }
        }
        newUser.setRoles(roles);

        userRepository.persist(newUser);
        return Response.status(Response.Status.CREATED)
                .entity("Benutzer erfolgreich erstellt: " + newUser.getUsername()).build();
    }
}
