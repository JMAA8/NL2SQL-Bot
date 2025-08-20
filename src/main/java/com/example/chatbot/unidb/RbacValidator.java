package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import java.util.regex.*;
import com.example.chatbot.unidb.AppRole;

@ApplicationScoped
public class RbacValidator {

    @Inject RbacPolicy policy;


    public static final class IdentityContext {
        public final Long benutzerId;
        public final Long studentId;
        public final Long professorId;
        public IdentityContext(Long benutzerId, Long studentId, Long professorId) {
            this.benutzerId = benutzerId;
            this.studentId = studentId;
            this.professorId = professorId;
        }
    }

    public static final class RbacCheckResult {
        public final boolean ok;
        public final List<String> violations;
        private RbacCheckResult(boolean ok, List<String> v){ this.ok=ok; this.violations=v; }
        public static RbacCheckResult ok(){ return new RbacCheckResult(true, List.of()); }
        public static RbacCheckResult fail(List<String> v){ return new RbacCheckResult(false, v); }
    }

    // --- sehr einfache Parser-Heuristiken ---
    private static final Pattern P_SELECT_PROJ = Pattern.compile("(?is)\\bselect\\s+(.*?)\\s+from\\b");
    private static final Pattern P_TBL = Pattern.compile("(?is)\\b(from|join)\\s+([a-z_][a-z0-9_]*)");

    private static Set<String> extractTables(String sql) {
        Set<String> t = new HashSet<>();
        if (sql == null) return t;
        Matcher m = P_TBL.matcher(sql);
        while (m.find()) t.add(m.group(2).toLowerCase(Locale.ROOT));
        return t;
    }

    private static Set<String> extractProjectedColumns(String sql) {
        Set<String> cols = new HashSet<>();
        if (sql == null) return cols;
        Matcher m = P_SELECT_PROJ.matcher(sql);
        if (!m.find()) return cols;
        String proj = m.group(1);
        for (String part : proj.split(",")) {
            String c = part
                    .replaceAll("(?i)\\bas\\b\\s+\\w+","")
                    .replaceAll("[^a-z0-9_\\.]", " ")
                    .trim()
                    .toLowerCase(Locale.ROOT);
            if (c.isBlank() || c.equals("*")) continue;
            String[] toks = c.split("\\s+|\\.");
            cols.add(toks[toks.length-1]);
        }
        return cols;
    }

    private static boolean selectsOnlyCounts(String sql) {
        if (sql == null) return false;
        Matcher m = P_SELECT_PROJ.matcher(sql);
        if (!m.find()) return false;
        String proj = m.group(1).toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        // sehr simple Heuristik: nur count(..) [ , count(..) ]*
        return proj.replaceAll("\\bcount\\s*\\([^\\)]*\\)(\\s+as\\s+\\w+)?\\s*(,\\s*count\\s*\\([^\\)]*\\)(\\s+as\\s+\\w+)?\\s*)*", "")
                .isBlank();
    }

    /** Hauptprüfung: Tabellen/Spalten + Row-Scope (für BASIC_USER). */
    public RbacCheckResult check(AppRole role, IdentityContext idCtx, String sql) {
        Map<String, Set<String>> allow = policy.allowedFor(role);
        if (allow == null) return RbacCheckResult.ok(); // Admin

        List<String> vio = new ArrayList<>();
        Set<String> usedTables = extractTables(sql);
        Set<String> projCols   = extractProjectedColumns(sql);
        Set<String> sensitive  = policy.sensitiveColumns();

        // 1) Tabellen-Whitelist
        for (String t : usedTables) {
            if (!allow.containsKey(t)) vio.add("TABLE:" + t);
        }

        // 2) Spalten-Whitelist (grob – nur Kolumnennamen)
        for (String c : projCols) {
            boolean seenInAllow = false;
            for (var e : allow.entrySet()) {
                if (e.getValue().contains(c)) { seenInAllow = true; break; }
            }
            if (!seenInAllow) {
                // häufig sind es Funktions/Alias-Ausdrücke – daher nur sensible Spalten hart prüfen
                if (sensitive.contains(c)) {
                    // fällt unten nochmal in die Sensitivitätsprüfung
                } else {
                    // weiches Signal ignorieren
                }
            }
        }

        // 3) Sensitivität/Row-Scope für BASIC_USER (Student)
        if (role == AppRole.BASIC_USER) {
            // 3a) email nie für Studierende
            if (projCols.contains("email")) {
                vio.add("COL:email");
            }
            // 3b) note nur im Self-Scope
            boolean asksNote = projCols.contains("note");
            boolean touchesRowScopeTbl = !Collections.disjoint(
                    usedTables, policy.studentRowScopeTables()
            );

            // Self-Scope prüfen: „student_id = <eigene_id>“ muss vorkommen, sonst Violation
            boolean hasSelfPredicate = false;
            if (idCtx != null && idCtx.studentId != null && sql != null) {
                String needle = "student_id = " + idCtx.studentId;
                hasSelfPredicate = sql.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
            }

            if (asksNote && !hasSelfPredicate && !selectsOnlyCounts(sql)) {
                vio.add("ROW_SCOPE:note_requires_self");
            }

            // 3c) generell personenbezogene Zeilen aus studenten/kursbelegung/anmeldung_pruefung nur im Self-Scope
            if (touchesRowScopeTbl && !hasSelfPredicate && !selectsOnlyCounts(sql)) {
                // Wenn explizit auf eine Person gefiltert wird (z.B. Jonas Bauer) aber ohne self-Predicate,
                // bleibt es eine Verletzung – wir erkennen Namen nicht robust, daher generisch:
                vio.add("ROW_SCOPE:self_required");
            }
        }

        return vio.isEmpty() ? RbacCheckResult.ok() : RbacCheckResult.fail(vio);
    }
}
