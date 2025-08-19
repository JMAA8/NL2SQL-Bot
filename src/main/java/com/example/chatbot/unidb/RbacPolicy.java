package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;

@ApplicationScoped
public class RbacPolicy {
    // erlaubte Spalten pro Rolle (Minimalbeispiel; erweitere bei Bedarf)
    private static final Map<String, Map<String, Set<String>>> ALLOW = Map.of(
            "Student", Map.of(
                    "studenten", Set.of("vorname","nachname","studiengang","status"), // KEINE email default
                    "kurse", Set.of("kurs_id","kursname","ects","semester","professor_id"),
                    "kursbelegung", Set.of("student_id","kurs_id","belegungsdatum"),
                    "pruefungen", Set.of("pruefung_id","kurs_id","pruefungsdatum","pruefungsart","raum"),
                    "anmeldung_pruefung", Set.of("student_id","pruefung_id","anmeldedatum","note"),
                    "professoren", Set.of("vorname","nachname","fachbereich","raum")
            ),
            "Professor", Map.of(
                    "studenten", Set.of("student_id","vorname","nachname","studiengang","status","einschreibedatum"),
                    "kurse", Set.of("kurs_id","kursname","ects","semester","professor_id"),
                    "kursbelegung", Set.of("student_id","kurs_id","belegungsdatum"),
                    "pruefungen", Set.of("pruefung_id","kurs_id","pruefungsdatum","pruefungsart","raum"),
                    "anmeldung_pruefung", Set.of("student_id","pruefung_id","anmeldedatum","note"),
                    "professoren", Set.of("vorname","nachname","fachbereich","raum","email")
            ),
            "Admin", Map.of() // alles erlaubt
    );

    public Map<String, Set<String>> allowedFor(String role) {
        if ("Admin".equals(role)) return null; // null = alles erlaubt
        return ALLOW.getOrDefault(role == null ? "Student" : role, Map.of());
    }
}