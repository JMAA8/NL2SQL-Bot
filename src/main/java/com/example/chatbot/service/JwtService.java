package com.example.chatbot.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Set;

@ApplicationScoped
public class JwtService {

    public String generateToken(String username, Set<String> roles) {
        return Jwt.issuer("chatbotBackend")
                .subject(username)
                .groups(roles)
                .expiresAt(System.currentTimeMillis() / 1000 + 3600)
                .sign();
    }
}

