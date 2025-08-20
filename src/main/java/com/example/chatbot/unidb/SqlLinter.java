package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.regex.*;

@ApplicationScoped
public class SqlLinter {

    // Minimales Katalogwissen (Spalten je Tabelle)
    private static final Map<String, Set<String>> COLS = Map.of(
            "studenten", Set.of("student_id","matrikelnummer","vorname","nachname","geburtsdatum","email","studiengang","einschreibedatum","status"),
            "professoren", Set.of("professor_id","vorname","nachname","email","fachbereich","raum"),
            "kurse", Set.of("kurs_id","kursname","beschreibung","ects","semester","professor_id"),
            "kursbelegung", Set.of("student_id","kurs_id","belegungsdatum"),
            "pruefungen", Set.of("pruefung_id","kurs_id","pruefungsdatum","pruefungsart","raum"),
            "anmeldung_pruefung", Set.of("student_id","pruefung_id","anmeldedatum","note"),
            "rollen", Set.of("rolle_id","rollenname"),
            "benutzer", Set.of("benutzer_id","nutzername","passwort_hash","email","rolle_id","student_id","professor_id")
    );

    // Numerische ID-/Integer-Spalten (für Typprüfung)
    private static final Set<String> NUMERIC_COLS = Set.of(
            "student_id","professor_id","kurs_id","pruefung_id","ects","rolle_id","benutzer_id"
    );

    // --- Patterns ---
    private static final Pattern P_SEMICOLON_ANY = Pattern.compile(";");
    private static final Pattern P_NON_SELECT = Pattern.compile("(?is)^\\s*select\\b");
    private static final Pattern P_FROM = Pattern.compile("(?is)\\bfrom\\b");
    private static final Pattern P_BANNED = Pattern.compile("(?is)\\b(insert|update|delete|create|alter|drop|grant|revoke|truncate|comment|merge|call|execute|vacuum|analyze|explain|index)\\b");

    private static final Pattern P_ALIAS_DECL = Pattern.compile("(?is)\\b(from|join)\\s+([a-z_][a-z0-9_]*)(?:\\s+(?:as\\s+)?([a-z][a-z0-9_]*))?");
    private static final Pattern P_ALIAS_USE  = Pattern.compile("(?is)\\b([a-z][a-z0-9_]*)\\.");
    private static final Pattern P_TBL        = Pattern.compile("(?i)\\b(from|join)\\s+([a-z_][a-z0-9_]*)");
    private static final Pattern P_QUAL_COL   = Pattern.compile("(?i)\\b([a-z_][a-z0-9_]*)\\.([a-z_][a-z0-9_]*)\\b");
    private static final Pattern P_COMP_EQ    = Pattern.compile("(?is)\\b([a-z_][a-z0-9_]*)(?:\\.([a-z_][a-z0-9_]*))?\\s*=\\s*'([^']+)'");
    private static final Pattern P_NEED_ALIAS = Pattern.compile("(?is)(?<!\\.)\\b(student_id|kurs_id|professor_id|pruefung_id)\\b");

    /** Entfernt nur Abschluss-Semikolons/Whitespace; bewahrt den Rest 1:1. */
    public String stripTrailingSemicolons(String sql) {
        if (sql == null) return null;
        return sql.replaceAll("[;\\s]+$", "").trim();
    }

    /** Whitespace normalisieren (einzeilig, stabilere Checks). */
    public String normalizeWhitespace(String sql) {
        if (sql == null) return null;
        return sql.replaceAll("[\\r\\n]+", " ").replaceAll("\\s+", " ").trim();
    }

    // ------------------------------------------------------------
    // NEW: Postprocessing-/Hilfsfunktionen
    // ------------------------------------------------------------

    /** Nur die erste echte SELECT-Zeile aus einer evtl. gemischten LLM-Antwort extrahieren. */
    public String extractFirstSelect(String out) {
        if (out == null) return null;
        String s = out.replace("```sql", " ").replace("```", " ").trim();
        int i = indexOfIgnoreCase(s, "select");
        if (i < 0) return normalizeWhitespace(stripTrailingSemicolons(s)); // keine SELECT gefunden
        s = s.substring(i).trim();
        int sc = s.indexOf(';');
        if (sc >= 0) s = s.substring(0, sc);
        return normalizeWhitespace(stripTrailingSemicolons(s));
    }

    /** Häufige Platzhalter für die Student-ID durch die echte ID (ohne Quotes) ersetzen. */
    public String substituteStudentIdPlaceholders(String sql, Long studentId) {
        if (sql == null || studentId == null) return sql;
        String s = sql;

        String[] phWords = new String[]{
                "ihre studenten-id hier","ihre studenten-id","eigene id","eigene studenten-id",
                "your student id here","your student id","your_student_id","own student id"
        };

        for (String ph : phWords) {
            s = s.replaceAll("(?is)=\\s*'\\s*" + Pattern.quote(ph) + "\\s*'", "= " + studentId);
            s = s.replaceAll("(?is)=\\s*\"\\s*" + Pattern.quote(ph) + "\\s*\"", "= " + studentId);
        }
        // generischer Fänger: = '<...student...id...>'
        s = s.replaceAll("(?is)=\\s*'[^']*(student[^']*id)[^']*'", "= " + studentId);
        s = s.replaceAll("(?is)=\\s*\"[^\"]*(student[^\"]*id)[^\"]*\"", "= " + studentId);

        // Zahl in Quotes → ohne Quotes
        s = s.replaceAll("(?is)=\\s*'\\s*(\\d+)\\s*'", "= $1");

        return s;
    }

    /** Self-Scope-Bedingung (student_id = <eigene_id>) programmatisch injizieren. */
    public String injectSelfPredicateIfNeeded(String sql, Long studentId) {
        if (sql == null || studentId == null) return sql;
        String s = sql;
        String sl = s.toLowerCase(Locale.ROOT);

        if (sl.contains("student_id = " + studentId)) return s; // schon vorhanden

        String aliasKb = findAlias(sl, "kursbelegung");
        String aliasSt = findAlias(sl, "studenten");

        String lhs;
        if (aliasKb != null) lhs = aliasKb + ".student_id";
        else if (aliasSt != null) lhs = aliasSt + ".student_id";
        else lhs = "studenten.student_id"; // Fallback

        String cond = lhs + " = " + studentId;

        Pattern endKw = Pattern.compile("(?is)\\b(group\\s+by|order\\s+by|limit)\\b");
        Matcher m = endKw.matcher(s);
        int cut = m.find() ? m.start() : s.length();

        int whereIdx = indexOfRegex(sl, "\\bwhere\\b");
        if (whereIdx < 0 || whereIdx > cut) {
            return s.substring(0, cut) + " WHERE " + cond + " " + s.substring(cut);
        } else {
            String head = s.substring(0, cut).replaceAll("(?is)\\s+\\bwhere\\b\\s*$", " WHERE ");
            String tail = s.substring(cut);
            return head + " AND " + cond + " " + tail;
        }
    }

    private int indexOfIgnoreCase(String hay, String needle) {
        if (hay == null || needle == null) return -1;
        return hay.toLowerCase(Locale.ROOT).indexOf(needle.toLowerCase(Locale.ROOT));
    }

    private int indexOfRegex(String sLower, String regex) {
        Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(sLower);
        return m.find() ? m.start() : -1;
    }

    private String findAlias(String sl, String baseTable) {
        Pattern p = Pattern.compile("(?is)\\b(from|join)\\s+" + Pattern.quote(baseTable) + "(?:\\s+(?:as\\s+)?([a-z][a-z0-9_]*))?");
        Matcher m = p.matcher(sl);
        if (m.find()) {
            String alias = m.group(2);
            if (alias != null && !alias.isBlank()) return alias.toLowerCase(Locale.ROOT);
            return baseTable;
        }
        return null;
    }

    // ------------------------------------------------------------
    // Linting (mit Alias-Resolution & gezielten Hinweisen)
    // ------------------------------------------------------------

    /** Liefert kurze, gezielte Issue-Texte für ein Repair-Prompt (mit Alias-Resolution). */
    public List<String> lint(String sql){
        List<String> issues = new ArrayList<>();
        if (sql == null || sql.isBlank()) {
            issues.add("EMPTY: Leere Ausgabe – erzeuge eine einzelne SELECT-Zeile.");
            return issues;
        }

        String s = sql.trim();

        // Grundcheck: genau EINE SELECT-Zeile, WITH/CTE ok, aber keine DDL/DML
        if (P_SEMICOLON_ANY.matcher(s).find()) {
            issues.add("SEMICOLON: Gib genau eine SELECT-Anweisung ohne ';'.");
        }
        if (!P_NON_SELECT.matcher(s).find()) {
            issues.add("NON_SELECT: Nur READ-ONLY SELECT erlaubt.");
        }
        if (!P_FROM.matcher(s).find()) {
            issues.add("NO_FROM: SELECT benötigt eine FROM-Klausel.");
        }
        if (P_BANNED.matcher(s).find()) {
            issues.add("BANNED_VERB: Keine DDL/DML (INSERT/UPDATE/DELETE/CREATE/ALTER/...).");
        }

        // Aliasse erfassen: alias -> basistabelle
        Map<String,String> aliasToTable = new HashMap<>();
        Set<String> declaredAliases = new HashSet<>();
        Matcher mDecl = P_ALIAS_DECL.matcher(s);
        while (mDecl.find()) {
            String base = mDecl.group(2);
            String alias = mDecl.group(3);
            if (alias != null && !alias.isBlank()) {
                String a = alias.toLowerCase(Locale.ROOT);
                declaredAliases.add(a);
                aliasToTable.put(a, base.toLowerCase(Locale.ROOT));
            }
        }

        // Alias-Verwendung prüfen
        Matcher mUse = P_ALIAS_USE.matcher(s);
        while (mUse.find()) {
            String used = mUse.group(1).toLowerCase(Locale.ROOT);
            if (!declaredAliases.contains(used) && !COLS.containsKey(used)) {
                issues.add("ALIAS_UNKNOWN: Alias/Tabelle '"+used+"' wird verwendet, aber nicht deklariert (FROM/JOIN).");
                break;
            }
        }

        // Qualifizierte Spalten prüfen (alias oder tabellenname)
        String sl = s.toLowerCase(Locale.ROOT);
        Set<String> usedTables = new HashSet<>();
        Matcher mt = P_TBL.matcher(sl);
        while (mt.find()) usedTables.add(mt.group(2));

        Matcher mc = P_QUAL_COL.matcher(sl);
        while (mc.find()) {
            String left = mc.group(1).toLowerCase(Locale.ROOT);   // alias oder tabellenname
            String col  = mc.group(2).toLowerCase(Locale.ROOT);

            String baseTable = COLS.containsKey(left) ? left : aliasToTable.get(left);
            if (baseTable == null) {
                continue; // unbekannter alias wurde oben schon gemeldet
            }
            Set<String> allowedCols = COLS.get(baseTable);
            if (allowedCols == null || !allowedCols.contains(col)) {
                issues.add("UNKNOWN_COL: '"+left+"."+col+"' existiert nicht (Basis: "+baseTable+"). Erlaubt: "+COLS.getOrDefault(baseTable, Set.of()));
            }

            // Spezieller 'name'-Guard für studenten/professoren
            if (col.equals("name") && (baseTable.equals("studenten") || baseTable.equals("professoren"))) {
                issues.add("NO_NAME_COL: Es gibt keine Spalte 'name'. Verwende 'vorname'/'nachname' oder CONCAT(vorname,' ',nachname) AS name.");
            }
        }

        // Offensichtliche ID = 'String'
        Matcher me = P_COMP_EQ.matcher(sl);
        while (me.find()) {
            String qualifier = me.group(1); // alias oder tabelle
            String col = (me.group(2) != null) ? me.group(2).toLowerCase(Locale.ROOT)
                    : qualifier.toLowerCase(Locale.ROOT);
            String lit = me.group(3);
            if (NUMERIC_COLS.contains(col)) {
                issues.add("TYPE_MISMATCH: Numerische Spalte '"+col+"' nicht mit Text vergleichen ('"+lit+"').");
            }
        }

        // Bei mehreren Tabellen: aliaslose *_id vermeiden
        if (!aliasToTable.isEmpty() && usedTables.size() >= 2) {
            Matcher need = P_NEED_ALIAS.matcher(sl);
            if (need.find()) {
                issues.add("NEED_ALIAS: Verwende für *_id IMMER einen Tabellenalias (z. B. kb.student_id).");
            }
        }

        // Bekannte Fehlmuster als Guidance
        if (sl.contains("anmeldung_pruefung.kurs_id")) {
            issues.add("JOIN_GUIDE: Nutze a.pruefung_id -> pruefungen p -> kurse k (k.kurs_id).");
        }
        if (sl.contains("belegungsdatum") && !sl.contains("kursbelegung")) {
            issues.add("COLUMN_CONFUSION: 'belegungsdatum' gehört zu kursbelegung, nicht zu studenten.");
        }

        // Optionaler Intent-Hinweis (harmlos, wird nur appended)
        if (sl.contains("welche kurse") && !sl.contains("k.kursname")) {
            issues.add("INTENT_HINT: Projiziere nur k.kursname (DISTINCT), nicht Personendaten.");
        }

        return issues;
    }

    public String buildHint(List<String> issues){
        if (issues == null || issues.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (String i : issues) sb.append("- ").append(i).append("\n");
        sb.append("Repariere strikt: eine einzige READ-ONLY SELECT-Zeile, korrekte Aliasse (z. B. s=studenten, kb=kursbelegung, k=kurse, p=pruefungen, a=anmeldung_pruefung), ")
                .append("IDs nie mit Text vergleichen, keine Spalte 'name' in personen-Tabellen (nutze vorname/nachname oder CONCAT), ")
                .append("bei 'Welche Kurse ...' nur k.kursname (DISTINCT) projizieren und für Studierende kb.student_id = <eigene_id> filtern.");
        return sb.toString();
    }
}
