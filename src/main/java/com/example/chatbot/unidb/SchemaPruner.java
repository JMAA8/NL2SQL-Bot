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

    private static final Map<Pattern, Set<String>> HINTS = Map.of(
            Pattern.compile("\\b(student|studierend|matrikel|einschreib|note|email)", Pattern.CASE_INSENSITIVE),
            Set.of("studenten","kursbelegung","anmeldung_pruefung"),
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
            for (var e : HINTS.entrySet()) if (e.getKey().matcher(nl).find()) out.addAll(e.getValue());
        }
        if (out.isEmpty()) out = new LinkedHashSet<>(ALL_TABLES);
        return out;
    }

    public Set<String> filterByRole(Set<String> tables, AppRole role) {
        if (role == AppRole.ADMIN) return tables;
        if (role == AppRole.ADVANCED_USER) {
            return keep(tables, Set.of("studenten","professoren","kurse","kursbelegung","pruefungen","anmeldung_pruefung"));
        }
        // BASIC_USER (Student)
        return keep(tables, Set.of("studenten","kurse","kursbelegung","pruefungen","anmeldung_pruefung","professoren"));
    }

    private Set<String> keep(Set<String> in, Set<String> allow) {
        Set<String> out = new LinkedHashSet<>();
        for (String t : in) if (allow.contains(t)) out.add(t);
        return out;
    }

    public String buildSchemaSnippet(Set<String> tables, AppRole role) {
        StringBuilder sb = new StringBuilder();
        if (tables.contains("studenten")) {
            if (role == AppRole.BASIC_USER)
                sb.append("studenten(student_id, vorname, nachname, studiengang, status)\n");
            else
                sb.append("studenten(student_id, matrikelnummer, vorname, nachname, geburtsdatum, email, studiengang, einschreibedatum, status)\n");
        }
        if (tables.contains("professoren"))
            sb.append("professoren(professor_id, vorname, nachname, email, fachbereich, raum)\n");
        if (tables.contains("kurse"))
            sb.append("kurse(kurs_id, kursname, beschreibung, ects, semester, professor_id)\n");
        if (tables.contains("kursbelegung"))
            sb.append("kursbelegung(student_id, kurs_id, belegungsdatum)\n");
        if (tables.contains("pruefungen"))
            sb.append("pruefungen(pruefung_id, kurs_id, pruefungsdatum, pruefungsart, raum)\n");
        if (tables.contains("anmeldung_pruefung"))
            sb.append("anmeldung_pruefung(student_id, pruefung_id, anmeldedatum, note)\n");
        if (role == AppRole.ADMIN && tables.contains("benutzer"))
            sb.append("benutzer(nutzername, email, rolle_id, student_id, professor_id)\n");
        return sb.toString();
    }
}
