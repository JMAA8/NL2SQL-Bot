package com.example.chatbot.nextcloud;

import java.util.concurrent.ConcurrentHashMap;

public class TokenStorage {
    private static final ConcurrentHashMap<String, String> tokenStore = new ConcurrentHashMap<>();

    public static void saveToken(String userId, String accessToken) {
        tokenStore.put(userId, accessToken);
    }

    public static String getToken(String userId) {
        return tokenStore.get(userId);
    }

    public static void removeToken(String userId) {
        tokenStore.remove(userId);
    }
}
