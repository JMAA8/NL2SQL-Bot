package com.example.chatbot.controller;

import com.example.chatbot.entity.User;
import com.example.chatbot.repository.UserRepository;
import com.example.chatbot.service.JwtService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    UserRepository userRepository;

    @Inject
    JwtService jwtService;

    @POST
    @Path("/login")
    public Response login(UserCredentials credentials) {
        User user = userRepository.findByUsername(credentials.getUsername());
        if (user != null && user.getPassword().equals(credentials.getPassword())) {
            Set<String> roles = user.getRoles().stream().map(role -> role.getRoleName()).collect(Collectors.toSet());
            String token = jwtService.generateToken(user.getUsername(), roles);
            return Response.ok("{\"token\": \"" + token + "\"}").build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }



    @GET
    @Path("/test")
    @PermitAll // Dieser Endpunkt ist für alle zugänglich
    public Response test() {
        return Response.ok("Test erfolgreich").build();
    }



    @GET
    @Path("/generate-admin-token")
    @RolesAllowed("ADMIN") // Nur Admins dürfen ein Admin-Token generieren (optional)
    public Response generateAdminToken() {
        // Beispiel-Benutzerinformationen für den Admin
        String username = "admin";
        Set<String> roles = Set.of("ADMIN");

        // JWT-Token generieren
        String token = jwtService.generateToken(username, roles);

        // Token zurückgeben
        return Response.ok("{\"token\": \"" + token + "\"}").build();
    }

}

