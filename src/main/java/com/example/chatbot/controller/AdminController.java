package com.example.chatbot.controller;

import com.example.chatbot.DTO.UserCredentials;
import com.example.chatbot.entity.User;
import com.example.chatbot.service.UserService;
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
    UserService userService;

    // Liste aller Benutzer abrufen
    @GET
    @Path("/users")
    public Response getAllUsers() {
        System.out.println("AdminController/getAllUser: Userliste aufgerufen");
        List<User> users = userService.getAllUsers();
        System.out.println("AdminController/getAllUsers: Userliste:" + users);
        return Response.ok(users).build();
    }

    //Neuen Benutzer erstellen
    @POST
    @Path("/add")
    @RolesAllowed("ADMIN") // Nur Administratoren dürfen neue Benutzer hinzufügen
    public Response addUser(UserCredentials credentials) {
        System.out.println("AdminController - addUser - Username: " + credentials.getUsername());
        String name = credentials.getUsername();
        String password = credentials.getPassword();
        try {
            userService.registerUser(name, password);
            return Response.status(Response.Status.CREATED)
                    .entity("Benutzer erfolgreich erstellt: " + credentials.getUsername())
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
    // Benutzer löschen
    @DELETE
    @Path("/user/{id}")
    public Response deleteUser(@PathParam("id") Long id) {
        if (userService.deleteUserById(id)) {
            return Response.ok("Benutzer gelöscht").build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    // Benutzerrolle aktualisieren
    @PUT
    @Path("/user/{id}/role")
    public Response updateUserRole(@PathParam("id") Long id, @QueryParam("role") String role) {
        try {
            userService.assignRoleToUser(id, role);
            return Response.ok("Rolle erfolgreich aktualisiert").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    // Rolle von einem Benutzer entfernen
    @DELETE
    @Path("/user/{id}/role")
    public Response removeUserRole(@PathParam("id") Long id, @QueryParam("role") String role) {
        try {
            userService.removeRoleFromUser(id, role);
            return Response.ok("Rolle erfolgreich entfernt").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}
