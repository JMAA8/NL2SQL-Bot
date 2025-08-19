package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@ApplicationScoped
public class BenchRepo {
    @Inject @io.quarkus.agroal.DataSource("unidb")
    DataSource ds; // explizit die Uni-DB

    // --- Run starten (evaluation_run) ---
    public long startRun(String label, String variant) throws SQLException {
        String sql = "SELECT bench.sp_start_run(?, ?) AS run_id";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, label);
            ps.setString(2, variant);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("run_id");
            }
        }
    }

    // --- Vorab-Insert in evaluation_result via Stored Proc ---
    public Map<String,Object> evalNoActor(long runId, int questionNo, int testNo,
                                          String nlText, String llmSqlOrNull, double latencyMs)
            throws SQLException {
        String sql = """
            SELECT bench.sp_evaluate_and_log_no_actor(
              ?, (SELECT qid FROM bench.test_question WHERE question_no=?),
              ?, ?, ?, ?
            ) AS r
            """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, runId);
            ps.setInt(2, questionNo);
            ps.setInt(3, testNo);
            ps.setString(4, nlText);
            if (llmSqlOrNull == null) ps.setNull(5, Types.VARCHAR); else ps.setString(5, llmSqlOrNull);
            ps.setBigDecimal(6, BigDecimal.valueOf(latencyMs));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Map<String,Object> out = new HashMap<>();
                out.put("row", rs.getObject("r")); // Composite der RETURNING-Zeile
                return out;
            }
        }
    }

    // --- Frage-Nummer über exakten Text finden (optional) ---
    public Integer findQuestionNoByExactText(String questionText) throws SQLException {
        String sql = "SELECT question_no FROM bench.test_question WHERE question_text = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, questionText);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return null;
            }
        }
    }

    // --- Semantische Fehler (JSONB-Array) anhängen ---
    public void appendSemanticErrors(long runId, int testNo, List<String> errs) throws SQLException {
        if (errs == null || errs.isEmpty()) return;
        String sql = """
            UPDATE bench.evaluation_result
               SET semantic_error_types = COALESCE(semantic_error_types, '[]'::jsonb) || to_jsonb(?::text[])
             WHERE run_id = ? AND test_no = ?
            """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setArray(1, c.createArrayOf("text", errs.toArray(new String[0])));
            ps.setLong(2, runId);
            ps.setInt(3, testNo);
            ps.executeUpdate();
        }
    }

    // --- Clause-Flags (als JSONB-Objekt) anhängen ---
    public void appendClauseSignals(long runId, int testNo, String generatedSql) throws SQLException {
        String json = buildClauseFlagsJson(generatedSql);
        String sql = """
            UPDATE bench.evaluation_result
               SET component_accuracy = COALESCE(component_accuracy, '{}'::jsonb) || ?::jsonb
             WHERE run_id = ? AND test_no = ?
            """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, json);
            ps.setLong(2, runId);
            ps.setInt(3, testNo);
            ps.executeUpdate();
        }
    }

    // --- SQL normalisieren & vergleichen (Exact-Match) ---
    public String normalizeSql(String s) {
        if (s == null) return null;
        String x = s.trim();
        if (x.endsWith(";")) x = x.substring(0, x.length()-1);
        // Kommentare weg, Whitespace komprimieren, lower-case
        x = x.replaceAll("--.*?$", "")
                .replaceAll("/\\*.*?\\*/", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
        return x;
    }

    public Boolean sqlEqualsNormalized(String a, String b) {
        if (a == null || b == null) return null;
        return normalizeSql(a).equals(normalizeSql(b));
    }

    // --- Referenz-SQL (Gold) holen; Spaltennamen flexibel ---
    public String fetchRefSqlByQuestionNo(int questionNo) throws SQLException {
        String sql = """
        SELECT r.reference_sql
          FROM bench.reference_query r
          JOIN bench.test_question q USING (qid)
         WHERE q.question_no = ?
    """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, questionNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1); // kann NULL sein (bei DML-Angriffsfragen)
                }
                return null;
            }
        }
    }

    // --- Component-Accuracy bauen (Jaccard + Flags) ---
    public String buildComponentAccuracyJson(String genSql, String refSql, boolean[] flags) {
        double jaccard = tableSetJaccard(genSql, refSql);
        boolean hasGroupBy = flags != null && flags.length>0 && flags[0];
        boolean hasOrderBy = flags != null && flags.length>1 && flags[1];
        boolean hasLimit   = flags != null && flags.length>2 && flags[2];
        boolean hasBetween = flags != null && flags.length>3 && flags[3];

        return String.format(Locale.ROOT,
                "{\"table_set_jaccard\": %.3f, \"has_group_by\": %s, \"has_order_by\": %s, \"has_limit\": %s, \"has_between\": %s}",
                jaccard, hasGroupBy, hasOrderBy, hasLimit, hasBetween);
    }

    // --- Nur Flags als JSON ---
    public String buildClauseFlagsJson(String sql) {
        if (sql == null) {
            return "{\"has_group_by\":false,\"has_order_by\":false,\"has_limit\":false,\"has_between\":false}";
        }
        String s = sql.toLowerCase(Locale.ROOT);
        return String.format(
                "{\"has_group_by\":%s,\"has_order_by\":%s,\"has_limit\":%s,\"has_between\":%s}",
                s.contains(" group by"),
                s.contains(" order by"),
                s.contains(" limit "),
                s.contains(" between ")
        );
    }

    // --- Flags + Jaccard mergen (wenn du erst Flags hattest und nun Gold dazu kommt) ---
    public String mergeCompAccWithJaccard(String baseJson, String genSql, String refSql) {
        double j = tableSetJaccard(genSql, refSql);
        if (baseJson == null || baseJson.isBlank() || baseJson.equals("{}")) {
            return String.format(Locale.ROOT, "{\"table_set_jaccard\": %.6f}", j);
        }
        String trimmed = baseJson.trim();
        if (trimmed.endsWith("}")) trimmed = trimmed.substring(0, trimmed.length()-1);
        if (trimmed.length() <= 1) return String.format(Locale.ROOT, "{\"table_set_jaccard\": %.6f}", j);
        return trimmed + String.format(Locale.ROOT, ", \"table_set_jaccard\": %.6f}", j);
    }

    // --- Tabellenmenge extrahieren & Jaccard berechnen ---
    private Set<String> tableSet(String sql) {
        Set<String> out = new HashSet<>();
        if (sql == null) return out;
        String s = sql.toLowerCase(Locale.ROOT);
        // naive Extraktion: erstes Token nach FROM/JOIN
        String[] parts = s.split("\\bfrom\\b|\\bjoin\\b");
        for (int i=1;i<parts.length;i++) {
            String seg = parts[i].trim();
            if (seg.isEmpty()) continue;
            String[] toks = seg.split("\\s+|,|\\(|\\)");
            if (toks.length > 0) {
                String t = toks[0].replaceAll("[^a-z0-9_\\.]", "");
                if (!t.isBlank()) out.add(t);
            }
        }
        return out;
    }

    private double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        Set<String> u = new HashSet<>(a); u.addAll(b);
        Set<String> i = new HashSet<>(a); i.retainAll(b);
        return u.isEmpty() ? 0.0 : (double) i.size() / (double) u.size();
    }

    private double tableSetJaccard(String genSql, String refSql) {
        return jaccard(tableSet(genSql), tableSet(refSql));
    }

    // --- Nach dem Vorab-Insert alles aktualisieren ---
    public void updateAfterExec(long runId, int testNo,
                                String normalizedSql,
                                boolean isReadOnly,
                                boolean rbacOk, String rbacViolationsJsonArray,
                                Boolean execOk, String execError, Double execMs, Integer rowCount, String resultHash,
                                Boolean exactMatch, Boolean execAccuracy,
                                String componentAccuracyJsonObj) throws SQLException {
        String sql = """
            UPDATE bench.evaluation_result
               SET normalized_sql     = ?,
                   is_read_only       = ?,
                   rbac_compliant     = ?,
                   rbac_violations    = COALESCE(rbac_violations, '[]'::jsonb) || ?::jsonb,
                   exec_ok            = ?,
                   exec_error         = ?,
                   exec_time_ms       = ?,
                   row_count          = ?,
                   result_hash        = ?,
                   exact_match        = ?,
                   execution_accuracy = ?,
                   component_accuracy = COALESCE(component_accuracy, '{}'::jsonb) || ?::jsonb
             WHERE run_id = ? AND test_no = ?
            """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, normalizedSql);
            ps.setBoolean(2, isReadOnly);
            ps.setBoolean(3, rbacOk);
            ps.setString(4, (rbacViolationsJsonArray == null || rbacViolationsJsonArray.isBlank()) ? "[]" : rbacViolationsJsonArray);
            if (execOk == null) ps.setNull(5, Types.BOOLEAN); else ps.setBoolean(5, execOk);
            ps.setString(6, execError);
            if (execMs == null) ps.setNull(7, Types.NUMERIC); else ps.setBigDecimal(7, BigDecimal.valueOf(execMs));
            if (rowCount == null) ps.setNull(8, Types.INTEGER); else ps.setInt(8, rowCount);
            ps.setString(9, resultHash);
            if (exactMatch == null) ps.setNull(10, Types.BOOLEAN); else ps.setBoolean(10, exactMatch);
            if (execAccuracy == null) ps.setNull(11, Types.BOOLEAN); else ps.setBoolean(11, execAccuracy);
            ps.setString(12, (componentAccuracyJsonObj == null || componentAccuracyJsonObj.isBlank()) ? "{}" : componentAccuracyJsonObj);
            ps.setLong(13, runId);
            ps.setInt(14, testNo);
            ps.executeUpdate();
        }
    }
}
