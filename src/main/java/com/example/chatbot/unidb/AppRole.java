package com.example.chatbot.unidb;

public enum AppRole {
    ADMIN, ADVANCED_USER, BASIC_USER;

    public static AppRole fromAny(String raw) {
        if (raw == null) return BASIC_USER;
        String r = raw.trim().toLowerCase();
        if (r.contains("admin") || r.contains("administrator")) return ADMIN;
        if (r.contains("advanced_user") || r.contains("professor")) return ADVANCED_USER;
        if (r.contains("basic_user") || r.contains("student")) return BASIC_USER;
        return BASIC_USER;
    }
}
