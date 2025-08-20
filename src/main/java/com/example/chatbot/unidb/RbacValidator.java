package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import java.util.regex.*;

@ApplicationScoped
public class RbacValidator {
    @Inject RbacPolicy policy;

    private static final Pattern TBL_COL = Pattern.compile(
            "(?i)\\b(from|join)\\s+([a-z_][a-z0-9_\\.]*)(?:\\s+as\\s+[a-z_][a-z0-9_]*)?|\\bselect\\s+(.*?)\\bfrom\\b",
            Pattern.DOTALL);

    /** sehr einfache Extraktion (Tabelle → genutzte Spalten, nur Heuristik) */
    public Map<String, Set<String>> extract(String sql) {
        Map<String, Set<String>> map = new HashMap<>();
        if (sql == null) return map;
        String s = sql.trim();

        // Tabellen sammeln
        Matcher m = Pattern.compile("(?i)\\b(from|join)\\s+([a-z_][a-z0-9_]*)").matcher(s);
        while (m.find()) map.putIfAbsent(m.group(2), new HashSet<>());

        // (Optional) Projektion parsen, falls du Spalten prüfen willst – hier weggelassen für Robustheit
        return map;
    }

    /** String-API bleibt: prüft Tabellen/Spalten gegen Rollen-Matrix. Admin => alles ok (policy.allowedFor("Admin") == null). */
    public RbacCheckResult check(String role, String sql) {
        Map<String, Set<String>> allow = policy.allowedFor(role);
        if (allow == null) return RbacCheckResult.ok(); // Admin = alles
        Map<String, Set<String>> used = extract(sql);
        List<String> violations = new ArrayList<>();
        for (String t : used.keySet()) {
            if (!allow.containsKey(t)) violations.add("TABLE:" + t);
            // Spaltenprüfung könntest du hier ergänzen
        }
        return violations.isEmpty() ? RbacCheckResult.ok() : RbacCheckResult.fail(violations);
    }

    // ---- NEU: AppRole-Overload (delegiert auf String-API) ----
    public RbacCheckResult check(AppRole role, String sql) {
        return check(mapToRoleString(role), sql);
    }

    private static String mapToRoleString(AppRole role) {
        if (role == null) return "Student";
        return switch (role) {
            case ADMIN -> "Admin";
            case ADVANCED_USER -> "Professor";
            case BASIC_USER -> "Student";
        };
    }

    public static class RbacCheckResult {
        public final boolean ok;
        public final List<String> violations;
        private RbacCheckResult(boolean ok, List<String> v){ this.ok=ok; this.violations=v; }
        public static RbacCheckResult ok(){ return new RbacCheckResult(true, List.of()); }
        public static RbacCheckResult fail(List<String> v){ return new RbacCheckResult(false, v); }
    }
}
