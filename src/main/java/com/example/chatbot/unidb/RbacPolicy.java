package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;

@ApplicationScoped
public class RbacPolicy {


    // erlaubte Spalten je Rolle (auf deine Tables/Spalten abgestimmt)
    private static final Map<String, Set<String>> STUDENT_ALLOW = Map.of(
            "studenten", Set.of("student_id","vorname","nachname","studiengang","einschreibedatum","status"),
            "professoren", Set.of("professor_id","vorname","nachname","fachbereich","raum"),
            "kurse", Set.of("kurs_id","kursname","beschreibung","ects","semester","professor_id"),
            "kursbelegung", Set.of("student_id","kurs_id","belegungsdatum"),
            "pruefungen", Set.of("pruefung_id","kurs_id","pruefungsdatum","pruefungsart","raum"),
            // „note“ ist erlaubt, aber nur im Self-Scope (wird in RbacValidator geprüft)
            "anmeldung_pruefung", Set.of("student_id","pruefung_id","anmeldedatum","note")
    );

    private static final Map<String, Set<String>> PROFESSOR_ALLOW = Map.of(
            "studenten", Set.of("student_id","vorname","nachname","studiengang","status","einschreibedatum"),
            "professoren", Set.of("professor_id","vorname","nachname","email","fachbereich","raum"),
            "kurse", Set.of("kurs_id","kursname","beschreibung","ects","semester","professor_id"),
            "kursbelegung", Set.of("student_id","kurs_id","belegungsdatum"),
            "pruefungen", Set.of("pruefung_id","kurs_id","pruefungsdatum","pruefungsart","raum"),
            "anmeldung_pruefung", Set.of("student_id","pruefung_id","anmeldedatum","note")
    );

    /** Tabellen + erlaubte Spalten; null => „alles erlaubt“ (Admin). */
    public Map<String, Set<String>> allowedFor(AppRole role) {
        if (role == null) role = AppRole.BASIC_USER;
        return switch (role) {
            case ADMIN -> null; // Admin darf alles (wir bleiben trotzdem read-only)
            case ADVANCED_USER -> PROFESSOR_ALLOW;
            case BASIC_USER -> STUDENT_ALLOW;
        };
    }

    /** global sensible Spalten (rollenabhängig geschützt) */
    public Set<String> sensitiveColumns() {
        return Set.of("email","note");
    }

    /** Tabellen, die bei Studierenden Self-Scope erfordern (RLS-ähnlich) */
    public Set<String> studentRowScopeTables() {
        return Set.of("studenten","kursbelegung","anmeldung_pruefung");
    }
}
