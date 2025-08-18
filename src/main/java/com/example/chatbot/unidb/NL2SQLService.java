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
                    "Wenn personenbezogene/sensible Daten (E-Mail, Passworthashes, Geburtsdaten, individuelle Note) => BLOCK.";

    public String generateSql(String userQuestion) {
        var req = new LLMRequest(userQuestion, ""); // kein Dokutext anhängen!
        req.setModel("gpt-4"); // oder dein Standard
        // Ersetze die system message in LLMRequest:
        req.getMessages().set(0, new LLMRequest.Message("system", SYSTEM));
        return llmClient.sendRequest(req).getChoices().get(0).getMessage().getContent().trim();
    }
}
