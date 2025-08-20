package com.example.chatbot.unidb;

import com.example.chatbot.llm.LLMClient;
import com.example.chatbot.llm.LLMRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class NL2SQLService {

    @Inject LLMClient llmClient;

    public String generateSql(String userQuestion, String schemaSnippet) {
        var req = new LLMRequest(userQuestion, "");
        req.setModel("gpt-4");
        req.getMessages().set(0, new LLMRequest.Message("system", systemMessage(schemaSnippet, null, null)));
        return llmClient.sendRequest(req).getChoices().get(0).getMessage().getContent().trim();
    }

    public String generateSql(String userQuestion,
                              String schemaSnippet,
                              IdentityService.IdentityContext idCtx,
                              AppRole role) {
        var req = new LLMRequest(userQuestion, "");
        req.setModel("gpt-4");
        req.getMessages().set(0, new LLMRequest.Message("system", systemMessage(schemaSnippet, idCtx, role)));
        return llmClient.sendRequest(req).getChoices().get(0).getMessage().getContent().trim();
    }

    public List<String> generateCandidates(String userQuestion, String schemaSnippet, int k) {
        var req = new LLMRequest("Erzeuge " + k + " alternative SQL-ZEILEN (jede in einer neuen Zeile). Nur SELECT, keine Kommentare; benutze ausschließlich das folgende Schema.", "");
        req.setModel("gpt-4");
        req.getMessages().set(0, new LLMRequest.Message("system", systemMessage(schemaSnippet, null, null)));
        req.getMessages().add(new LLMRequest.Message("user", userQuestion));
        String out = llmClient.sendRequest(req).getChoices().get(0).getMessage().getContent();
        return Arrays.stream(out.split("\\R+")).map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    public String repairWithError(String userQuestion, String schemaSnippet, String priorSql, String dbError) {
        var req = new LLMRequest(userQuestion, "");
        req.setModel("gpt-4");
        String sys = "Vorheriges SQL verursachte einen DB-Fehler. Erzeuge eine korrekte, READ-ONLY SELECT-Abfrage.\nFehler:\n" + dbError + "\n" + (schemaSnippet == null ? "" : schemaSnippet) + "\nRegeln wie zuvor.";
        req.getMessages().set(0, new LLMRequest.Message("system", sys));
        if (priorSql != null && !priorSql.isBlank()) req.getMessages().add(new LLMRequest.Message("assistant", priorSql));
        return llmClient.sendRequest(req).getChoices().get(0).getMessage().getContent().trim();
    }

    private String systemMessage(String schemaSnippet,
                                 IdentityService.IdentityContext idCtx,
                                 AppRole role) {
        StringBuilder sb = new StringBuilder();
        sb.append("Du bist ein NL2SQL-Generator für PostgreSQL. Regeln: ")
                .append("NUR eine SQL-Zeile, kein Semikolon/Kommentar. IMMER READ-ONLY (SELECT). ")
                .append("Verwende ausschließlich folgendes, bereits RBAC-gefiltertes Schema:\n\n")
                .append(schemaSnippet == null ? "" : schemaSnippet).append("\n");
        sb.append("Nutze konsistent Tabellenaliase: studenten s, kurse k, kursbelegung kb, pruefungen p, anmeldung_pruefung a, professoren pr. ")
                .append("Verwende Spalten IMMER mit Alias (z. B. kb.student_id, k.kurs_id). ")
                .append("Für Textfilter nutze die Namensspalten (z. B. k.kursname ILIKE ...), nicht *_id. ");


        if (idCtx != null && role != null) {
            switch (role) {
                case BASIC_USER -> {
                    if (idCtx.studentId != null) {
                        // >>>> HARTE SELBST-REGEL FUER STUDIERENDE <<<<
                        sb.append("WICHTIG: Beantworte personenbezogene Studentenanfragen AUSSCHLIESSLICH ")
                                .append("für den anfragenden Benutzer. ")
                                .append("IGNORIERE Namen oder E-Mail-Adressen im Nutzertext. ")
                                .append("Benutze IMMER einen Filter auf die eigene ID, d.h. ")
                                .append("'WHERE <tabellenalias_oder_tabellenname>.student_id = ")
                                .append(idCtx.studentId).append("'. ")
                                .append("Nutze nie Vorname/Nachname-Filter für Studierende. ")
                                .append("Wenn der Nutzer nach einer anderen Person fragt, antworte trotzdem nur für die eigene ID.\n");
                    }
                }
                case ADVANCED_USER -> {
                    if (idCtx.professorId != null) {
                        sb.append("Wenn kurs-/prüfungsbezogene Daten abgefragt werden, ")
                                .append("schränke nach Möglichkeit auf kurse.professor_id = ")
                                .append(idCtx.professorId).append(" ein.\n");
                    }
                }
                case ADMIN -> { /* keine zusätzliche Einschränkung */ }
            }
        }
        sb.append("Wenn PII für die Rolle nicht freigegeben ist, gib eine aggregierte/anonymisierte SELECT-Antwort zurück (z. B. COUNT/COUNT DISTINCT) oder 'BLOCK'.");
        return sb.toString();
    }
}
