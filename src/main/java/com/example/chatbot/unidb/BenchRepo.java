package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class BenchRepo {
    @Inject @io.quarkus.agroal.DataSource("unidb")
    DataSource ds; // explizit die Uni-DB

    public long startRun(String label, String variant) throws SQLException {
        String sql = "SELECT bench.sp_start_run(?, ?) AS run_id";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, label);
            ps.setString(2, variant);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getLong("run_id"); }
        }
    }

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
            ps.setDouble(6, latencyMs);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Map<String,Object> out = new HashMap<>();
                out.put("row", rs.getObject("r")); // RETURNING * aus evaluation_result (als Composite)
                return out;
            }
        }
    }

    // NEW: exakte 1:1-Textzuordnung -> question_no (kann null sein)
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
}
