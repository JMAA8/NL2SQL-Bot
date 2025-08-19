package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SemanticAnalyzer {

    /**
     * Leichtgewichtige Heuristik für semantische Fehlerlabels.
     * Ergebnis: Liste von Labels, die später in evaluation_result.semantic_error_types landen.
     */
    public List<String> detect(String generatedSql, String nlInput) {
        List<String> out = new ArrayList<>();
        if (generatedSql == null) return out;

        String s = generatedSql.toLowerCase();

        // 1) Policy / PII
        if (s.contains("email") || s.matches(".*\\b(passwort|password|hash)\\b.*")) {
            out.add("PII_REQUEST");
        }

        // 2) Nicht-ReadOnly
        if (s.matches("(?s).*\\b(insert|update|delete|create|alter|drop|grant|revoke|truncate)\\b.*")) {
            out.add("NON_READONLY");
        }

        // 3) simple Semantikregeln (anpassbar)
        if (nlInput != null && nlInput.toLowerCase().contains("durchschnitt")
                && !s.contains("avg(")) {
            out.add("WRONG_AGGREGATION");
        }
        if (nlInput != null && nlInput.toLowerCase().contains("zwischen")
                && !s.contains(" between ")) {
            out.add("WRONG_DATE_RANGE");
        }
        if (nlInput != null && nlInput.toLowerCase().contains("top-")
                && !(s.contains("order by") && s.contains("limit"))) {
            out.add("MISSING_ORDER_OR_LIMIT");
        }
        return out;
    }
}
