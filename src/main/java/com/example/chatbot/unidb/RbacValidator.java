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
        // crude: SELECT-Spaltenliste (erste SELECT ... FROM)
        int sel = s.toLowerCase().indexOf("select");
        int from = s.toLowerCase().indexOf(" from ");
        if (sel >=0 && from > sel) {
            String proj = s.substring(sel + 6, from).trim();
            // split by comma, remove funcs/aliases
            String[] cols = proj.split(",");
            for (String c : cols) {
                String cc = c.replaceAll("(?i)\\bas\\b.*","")
                        .replaceAll("[^a-z0-9_\\.]", " ").trim();
                if (cc.isBlank() || cc.equals("*")) continue;
                // optional: t.col -> Tabelle unbekannt; wir prüfen gegen jede Tabelle später
            }
        }
        // Tabellen sammeln
        Matcher m = Pattern.compile("(?i)\\b(from|join)\\s+([a-z_][a-z0-9_]*)").matcher(s);
        while (m.find()) map.putIfAbsent(m.group(2), new HashSet<>());
        return map;
    }

    /** Prüft Tabellen/Spalten gegen Rollen-Matrix. Admin => immer ok. */
    public RbacCheckResult check(String role, String sql) {
        Map<String, Set<String>> allow = policy.allowedFor(role);
        if (allow == null) return RbacCheckResult.ok(); // Admin = alles
        Map<String, Set<String>> used = extract(sql);
        List<String> violations = new ArrayList<>();
        for (String t : used.keySet()) {
            if (!allow.containsKey(t)) {
                violations.add("TABLE:" + t);
            }
            // Spaltenprüfung optional verfeinern (hier: nur wenn proj. Spalten explizit geparst würden)
        }
        return violations.isEmpty() ? RbacCheckResult.ok() : RbacCheckResult.fail(violations);
    }

    public static class RbacCheckResult {
        public final boolean ok;
        public final List<String> violations;
        private RbacCheckResult(boolean ok, List<String> v){ this.ok=ok; this.violations=v; }
        public static RbacCheckResult ok(){ return new RbacCheckResult(true, List.of()); }
        public static RbacCheckResult fail(List<String> v){ return new RbacCheckResult(false, v); }
    }
}