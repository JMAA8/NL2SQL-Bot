package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.regex.Pattern;

@ApplicationScoped
public class SchemaPruner {
    private static final Set<String> ALL_TABLES = Set.of(
            "studenten","professoren","kurse","kursbelegung",
            "pruefungen","anmeldung_pruefung","rollen","benutzer"
    );

    // sehr einfache Keyword→Tabellen-Heuristik; kann später durch Embeddings ersetzt werden
    private static final Map<Pattern, Set<String>> HINTS = Map.of(
            Pattern.compile("\\b(student|studierend|email|matrikel|einschreib)", Pattern.CASE_INSENSITIVE),
            Set.of("studenten","benutzer"),
            Pattern.compile("\\b(prof|dozent)", Pattern.CASE_INSENSITIVE),
            Set.of("professoren","kurse","pruefungen"),
            Pattern.compile("\\b(kurs|ects|semester|beleg)", Pattern.CASE_INSENSITIVE),
            Set.of("kurse","kursbelegung","anmeldung_pruefung"),
            Pattern.compile("\\b(pr(ü|u)fung|pruef)", Pattern.CASE_INSENSITIVE),
            Set.of("pruefungen","anmeldung_pruefung")
    );

    public Set<String> suggestTables(String nl) {
        Set<String> out = new LinkedHashSet<>();
        if (nl != null) {
            for (var e : HINTS.entrySet()) {
                if (e.getKey().matcher(nl).find()) out.addAll(e.getValue());
            }
        }
        if (out.isEmpty()) out = new LinkedHashSet<>(ALL_TABLES); // Fallback
        return out;
    }

    // ---- bestehende String-API (bleibt erhalten) ----
    public Set<String> filterByRole(Set<String> tables, String role) {
        if (role == null) role = "Student";
        switch (role) {
            case "Admin":
                return tables;
            case "Professor":
                // z. B. Professoren sehen keine Admin-/RBAC-Tabellen
                return keep(tables, Set.of("studenten","professoren","kurse","kursbelegung","pruefungen","anmeldung_pruefung"));
            default: // Student
                // Studierende: keine benutzer/rollen (Admin-Tabellen)
                return keep(tables, Set.of("studenten","kurse","kursbelegung","pruefungen","anmeldung_pruefung","professoren"));
        }
    }

    public String buildSchemaSnippet(Set<String> tables) {
        // baseline (ohne Rolle) – enthält alle Spalten der Tabellen in 'tables'
        return buildSchemaSnippet(tables, (AppRole)null);
    }

    // ---- NEU: AppRole-Overloads (Option B) ----
    public Set<String> filterByRole(Set<String> tables, AppRole role) {
        return filterByRole(tables, mapToRoleString(role));
    }

    public String buildSchemaSnippet(Set<String> tables, AppRole role) {
        String r = mapToRoleString(role);
        StringBuilder sb = new StringBuilder("Nutze ausschließlich dieses Schema:\n");

        // Für Students sensible Spalten (z.B. E-Mail) weglassen; Professor darf studenten.email, Admin alles.
        boolean allowStudentEmail = "Professor".equals(r) || "Admin".equals(r);
        boolean allowBenutzer = "Admin".equals(r); // benutzer(*) nur für Admin

        if (tables.contains("studenten")) {
            if (allowStudentEmail) {
                sb.append("studenten(student_id, matrikelnummer, vorname, nachname, geburtsdatum, email, studiengang, einschreibedatum, status)\n");
            } else {
                sb.append("studenten(student_id, matrikelnummer, vorname, nachname, geburtsdatum, studiengang, einschreibedatum, status)\n");
            }
        }
        if (tables.contains("professoren")) {
            sb.append("professoren(professor_id, vorname, nachname, email, fachbereich, raum)\n");
        }
        if (tables.contains("kurse")) {
            sb.append("kurse(kurs_id, kursname, beschreibung, ects, semester, professor_id)\n");
        }
        if (tables.contains("kursbelegung")) {
            sb.append("kursbelegung(student_id, kurs_id, belegungsdatum)\n");
        }
        if (tables.contains("pruefungen")) {
            sb.append("pruefungen(pruefung_id, kurs_id, pruefungsdatum, pruefungsart, raum)\n");
        }
        if (tables.contains("anmeldung_pruefung")) {
            sb.append("anmeldung_pruefung(student_id, pruefung_id, anmeldedatum, note)\n");
        }
        if (tables.contains("rollen") && "Admin".equals(r)) {
            sb.append("rollen(rolle_id, rollenname)\n");
        }
        if (tables.contains("benutzer") && allowBenutzer) {
            sb.append("benutzer(benutzer_id, nutzername, email, rolle_id, student_id, professor_id)\n");
        }
        return sb.toString();
    }

    // ---- helpers ----
    private static String mapToRoleString(AppRole role) {
        if (role == null) return "Student";
        return switch (role) {
            case ADMIN -> "Admin";
            case ADVANCED_USER -> "Professor";
            case BASIC_USER -> "Student";
        };
    }

    private Set<String> keep(Set<String> in, Set<String> allow) {
        Set<String> out = new LinkedHashSet<>();
        for (String t : in) if (allow.contains(t)) out.add(t);
        return out;
    }
}
