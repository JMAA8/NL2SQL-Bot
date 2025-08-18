package com.example.chatbot.unidb;

import com.example.chatbot.llm.LLMClient;
import com.example.chatbot.llm.LLMRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class NL2SQLService {
    @Inject
    LLMClient llmClient;

    private static final String SYSTEM =
            "Du bist ein NL2SQL-Generator für PostgreSQL. Regeln: " +
                    "NUR EINE SQL-Zeile, kein Semikolon/Kommentar. IMMER READ-ONLY (SELECT). " +
                    "Nur folgende Tabellen/Spalten: ..." +
                    "Nutze nur das Schema der Uni-DB:\n" +
                    "studenten(student_id, matrikelnummer, vorname, nachname, geburtsdatum, email, studiengang, einschreibedatum, status),\n" +
                    "professoren(professor_id, vorname, nachname, email, fachbereich, raum),\n" +
                    "kurse(kurs_id, kursname, beschreibung, ects, semester, professor_id),\n" +
                    "kursbelegung(student_id, kurs_id, belegungsdatum),\n" +
                    "pruefungen(pruefung_id, kurs_id, pruefungsdatum, pruefungsart, raum),\n" +
                    "anmeldung_pruefung(student_id, pruefung_id, anmeldedatum, note),\n" +
                    "rollen(rolle_id, rollenname), benutzer(nutzername, passwort_hash, email, rolle_id, student_id, professor_id).";

    public String generateSql(String userQuestion) {
        var req = new LLMRequest(userQuestion, ""); // kein Dokutext anhängen!
        req.setModel("gpt-4"); // oder dein Standard
        // Ersetze die system message in LLMRequest:
        req.getMessages().set(0, new LLMRequest.Message("system", SYSTEM));
        return llmClient.sendRequest(req).getChoices().get(0).getMessage().getContent().trim();
    }
}
