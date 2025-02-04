package com.example.chatbot.controller;

import com.example.chatbot.DTO.UserCredentials;
import com.example.chatbot.service.JwtService;
import com.example.chatbot.service.UserService;
import jakarta.annotation.security.PermitAll;
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
    UserService userService;

    @Inject
    JwtService jwtService;

    @POST
    @Path("/login")
    @PermitAll
    public Response login(UserCredentials credentials) {
        System.out.println("AuthController: Methode /login aufgerufen");
        // Überprüfung des Optional<User>
        return userService.getUserByUsername(credentials.getUsername())
                .filter(user -> user.getPassword().equals(credentials.getPassword()))
                .map(user -> {
                    Set<String> roles = user.getRoles().stream()
                            .map(role -> role.getRoleName())
                            .collect(Collectors.toSet());
                    String token = jwtService.generateToken(user.getUsername(), user.getId(), roles);
                    return Response.ok("{\"token\": \"" + token + "\"}").build();
                })
                .orElse(Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Invalid credentials").build());
    }

    @POST
    @Path("/register")
    @PermitAll
    public Response register(UserCredentials credentials) {
        try {
            userService.registerUser(credentials.getUsername(), credentials.getPassword());
            return Response.status(Response.Status.CREATED).entity("User successfully registered").build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/test")
    @PermitAll
    public Response test() {
        return Response.ok("Test erfolgreich").build();
    }

    /*@GET
    @Path("/generate-admin-token")
    @RolesAllowed("ADMIN")
    public Response generateAdminToken() {
        String username = "admin";
        Set<String> roles = Set.of("ADMIN");
        String token = jwtService.generateToken(username, id, roles);
        return Response.ok("{\"token\": \"" + token + "\"}").build();
    }*/
}
