package com.example.chatbot.unidb;

import com.example.chatbot.llm.LLMClient;
import com.example.chatbot.llm.LLMRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class NL2SQLService {
    @Inject
    LLMClient llmClient;

    private String systemMessage(String schemaSnippet) {
        return "Du bist ein NL2SQL-Generator für PostgreSQL. Regeln: " +
                "NUR eine SQL-Zeile, kein Semikolon/Kommentar. IMMER READ-ONLY (SELECT). " +
                "Verwende ausschließlich folgendes, bereits RBAC-gefiltertes Schema:\n\n" +
                (schemaSnippet == null ? "" : schemaSnippet) + "\n" +
                "Wenn der Nutzer nach PII fragt, die für seine Rolle nicht freigegeben ist, antworte mit einem SELECT, " +
                "das nur aggregierte/anonymisierte Werte liefert (z.B. COUNT/COUNT DISTINCT) oder 'BLOCK'.";
    }

    public String generateSql(String userQuestion, String schemaSnippet) {
        var req = new LLMRequest(userQuestion, "");
        req.setModel("gpt-4");
        req.getMessages().set(0, new LLMRequest.Message("system", systemMessage(schemaSnippet)));
        return llmClient.sendRequest(req).getChoices().get(0).getMessage().getContent().trim();
    }

    public List<String> generateCandidates(String userQuestion, String schemaSnippet, int k) {
        var req = new LLMRequest(
                "Erzeuge " + k + " alternative SQL-ZEILEN (jede in einer neuen Zeile). " +
                        "Nur SELECT, keine Kommentare; benutze ausschließlich das folgende Schema.", "");
        req.setModel("gpt-4");
        req.getMessages().set(0, new LLMRequest.Message("system", systemMessage(schemaSnippet)));
        req.getMessages().add(new LLMRequest.Message("user", userQuestion));
        String out = llmClient.sendRequest(req).getChoices().get(0).getMessage().getContent();
        return Arrays.stream(out.split("\\R+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    public String repairWithError(String userQuestion, String schemaSnippet, String priorSql, String dbError) {
        var req = new LLMRequest(userQuestion, "");
        req.setModel("gpt-4");
        String sys = "Vorheriges SQL verursachte einen DB-Fehler. Erzeuge eine korrekte, READ-ONLY SELECT-Abfrage.\n" +
                "Fehler:\n" + dbError + "\n" +
                (schemaSnippet == null ? "" : schemaSnippet) + "\nRegeln wie zuvor.";
        req.getMessages().set(0, new LLMRequest.Message("system", sys));
        if (priorSql != null && !priorSql.isBlank()) {
            req.getMessages().add(new LLMRequest.Message("assistant", priorSql));
        }
        return llmClient.sendRequest(req).getChoices().get(0).getMessage().getContent().trim();
    }
}
