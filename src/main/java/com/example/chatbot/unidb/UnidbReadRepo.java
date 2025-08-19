package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class UnidbReadRepo {
    @Inject @io.quarkus.agroal.DataSource("unidb") DataSource ds;

    public static final class QueryRun {
        public final boolean execOk;
        public final String execError;
        public final double execMs;
        public final int rowCount;
        public final String resultHash;
        public final String preview;
        public final boolean hasGroupBy, hasOrderBy, hasLimit, hasBetween;

        public QueryRun(boolean ok, String err, double ms, int rows, String hash, String prev,
                        boolean g, boolean o, boolean l, boolean b) {
            this.execOk = ok; this.execError = err; this.execMs = ms;
            this.rowCount = rows; this.resultHash = hash; this.preview = prev;
            this.hasGroupBy = g; this.hasOrderBy = o; this.hasLimit = l; this.hasBetween = b;
        }
    }

    /** Öffentlich – wird im ChatService benötigt. */
    public boolean isSelectOnly(String s) {
        String t = s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
        return t.startsWith("SELECT")
                && !t.matches("(?s).*\\b(INSERT|UPDATE|DELETE|MERGE|ALTER|DROP|TRUNCATE|CREATE|GRANT|REVOKE)\\b.*");
    }

    /** Nur Hash & RowCount (für Referenz-SQL). */
    public QueryRun hashOnly(String sql) throws SQLException {
        if (!isSelectOnly(sql)) {
            return new QueryRun(false, "NON-SELECT", 0.0, 0, null, null, has(sql,"group by"),has(sql,"order by"),has(sql,"limit"),has(sql," between "));
        }
        long t0 = System.nanoTime();
        int rows = 0;
        String hash = null;
        String err = null;
        boolean ok = false;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM (" + sql + ") __x")) {
            try (ResultSet rs = ps.executeQuery()) {
                hash = digestResultSet(rs);
                rows = lastRowCount; // set by digestResultSet
                ok = true;
            }
        } catch (SQLException e) {
            err = e.getMessage();
            ok = false;
        }
        double ms = (System.nanoTime() - t0) / 1_000_000.0;
        return new QueryRun(ok, err, ms, rows, hash, null,
                has(sql,"group by"),has(sql,"order by"),has(sql,"limit"),has(sql," between "));
    }

    /** Ausführen + Hash + RowCount + hübsche Vorschau (LIMIT n). */
    public QueryRun runAndHashSelect(String sql, int limitRows) throws SQLException {
        if (!isSelectOnly(sql)) {
            return new QueryRun(false, "NON-SELECT", 0.0, 0, null, "BLOCK (kein SELECT).",
                    has(sql,"group by"),has(sql,"order by"),has(sql,"limit"),has(sql," between "));
        }

        // 1) Preview (LIMIT)
        String preview;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("WITH __q AS (" + sql + ") SELECT * FROM __q LIMIT " + Math.max(1, limitRows));
             ResultSet rs = ps.executeQuery()) {
            preview = renderMarkdown(rs);
        }

        // 2) Vollständiger Hash + RowCount
        long t0 = System.nanoTime();
        boolean ok = false; String err = null; String hash = null; int rows = 0;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM (" + sql + ") __x");
             ResultSet rs = ps.executeQuery()) {
            hash = digestResultSet(rs);
            rows = lastRowCount;
            ok = true;
        } catch (SQLException e) {
            err = e.getMessage();
            ok = false;
        }
        double ms = (System.nanoTime() - t0) / 1_000_000.0;

        return new QueryRun(ok, err, ms, rows, hash, preview,
                has(sql,"group by"),has(sql,"order by"),has(sql,"limit"),has(sql," between "));
    }

    // -------- helpers --------

    private static boolean has(String s, String needle) {
        return s != null && s.toLowerCase(Locale.ROOT).contains(needle);
    }

    private int lastRowCount = 0;

    private String digestResultSet(ResultSet rs) throws SQLException {
        MessageDigest md;
        try { md = MessageDigest.getInstance("MD5"); }
        catch (Exception e) { throw new SQLException("MD5 not available", e); }

        ResultSetMetaData mdta = rs.getMetaData();
        int cols = mdta.getColumnCount();
        lastRowCount = 0;

        while (rs.next()) {
            lastRowCount++;
            // eine stabile Zeilenrepräsentation bauen
            StringBuilder sb = new StringBuilder();
            for (int i=1;i<=cols;i++) {
                Object v = rs.getObject(i);
                sb.append(v == null ? "NULL" : String.valueOf(v));
                if (i < cols) sb.append('|');
            }
            sb.append('\n');
            md.update(sb.toString().getBytes(StandardCharsets.UTF_8));
        }
        byte[] digest = md.digest();
        return toHex(digest);
    }

    private static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length*2);
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    private String renderMarkdown(ResultSet rs) throws SQLException {
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
        if (rows.size() == 1) return "_(keine Zeilen)_";

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
        return sb.toString();
    }
}
