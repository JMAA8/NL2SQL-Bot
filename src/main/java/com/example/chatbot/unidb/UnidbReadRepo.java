package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@ApplicationScoped
public class UnidbReadRepo {
    @Inject @io.quarkus.agroal.DataSource("unidb") DataSource ds;

    public String previewSelect(String sql, int limitRows) throws SQLException {
        if (!isSelectOnly(sql)) {
            return "BLOCK (kein SELECT).";
        }
        String wrapped = "WITH __q AS (" + sql + ") SELECT * FROM __q LIMIT " + Math.max(1, limitRows);
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(wrapped);
             ResultSet rs = ps.executeQuery()) {
            return renderTable(rs);
        }
    }

    private boolean isSelectOnly(String s) {
        String t = s == null ? "" : s.trim().toUpperCase();
        return t.startsWith("SELECT") && !t.matches(".*\\b(INSERT|UPDATE|DELETE|MERGE|ALTER|DROP|TRUNCATE|CREATE|GRANT|REVOKE)\\b.*");
    }

    private String renderTable(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        List<String[]> rows = new ArrayList<>();
        String[] header = new String[cols];
        for (int i=1;i<=cols;i++) header[i-1] = md.getColumnLabel(i);
        rows.add(header);
        while (rs.next()) {
            String[] r = new String[cols];
            for (int i=1;i<=cols;i++) {
                Object v = rs.getObject(i);
                r[i-1] = v == null ? "NULL" : String.valueOf(v);
            }
            rows.add(r);
        }
        // Einfaches Markdown-Table fürs Chat-Frontend
        StringBuilder sb = new StringBuilder();
        // header
        sb.append("| ");
        for (String h : header) sb.append(h).append(" | ");
        sb.append("\n| ");
        for (int i=0;i<cols;i++) sb.append("--- | ");
        sb.append("\n");
        // rows
        for (int r=1;r<rows.size();r++) {
            sb.append("| ");
            for (String cell : rows.get(r)) sb.append(cell).append(" | ");
            sb.append("\n");
        }
        if (rows.size()==1) return "_(keine Zeilen)_";
        return sb.toString();
    }
}
