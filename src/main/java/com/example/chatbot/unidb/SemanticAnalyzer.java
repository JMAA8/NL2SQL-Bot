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
        String nl = (nlInput == null) ? "" : nlInput.toLowerCase();

        // 1) Policy / PII
        if (s.contains("email") || s.matches("(?s).*\\b(passwort|password|hash)\\b.*")) {
            out.add("PII_REQUEST");
        }

        // 2) Nicht-ReadOnly
        if (s.matches("(?s).*\\b(insert|update|delete|create|alter|drop|grant|revoke|truncate)\\b.*")) {
            out.add("NON_READONLY");
        }

        // 3) Semantikregeln
        if (nl.contains("durchschnitt") && !s.contains("avg(")) {
            out.add("WRONG_AGGREGATION");
        }
        if (nl.contains("zwischen") && !s.contains(" between ")) {
            out.add("WRONG_DATE_RANGE");
        }
        if (nl.contains("top-") && !(s.contains("order by") && s.contains("limit"))) {
            out.add("MISSING_ORDER_OR_LIMIT");
        }
        if (nl.matches("(?s).*\\b(wie viele|anzahl|count)\\b.*") && !s.contains("count(")) {
            out.add("MISSING_COUNT");
        }
        if (nl.matches("(?s).*\\b(distinct|einzigartig|ohne dopp)\\b.*")
                && !(s.contains("distinct") || s.contains("group by"))) {
            out.add("MISSING_DISTINCT_OR_GROUPBY");
        }
        return out;
    }
}
