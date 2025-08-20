package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.*;

@ApplicationScoped
public class IdentityService {

    @Inject @io.quarkus.agroal.DataSource("unidb")
    DataSource ds;

    /** Liefert student_id/professor_id/username passend zur AppRole der JWT-Identität. */
    public IdentityContext resolveByBenutzerId(Long benutzerId, AppRole role) throws SQLException {
        if (benutzerId == null) return new IdentityContext(null, null, null);
        String sql = """
            SELECT b.benutzer_id, b.nutzername, b.student_id, b.professor_id
              FROM benutzer b
             WHERE b.benutzer_id = ?
        """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, benutzerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return new IdentityContext(benutzerId, null, null);
                Long studentId = rs.getObject("student_id") == null ? null : rs.getLong("student_id");
                Long professorId = rs.getObject("professor_id") == null ? null : rs.getLong("professor_id");
                String username = rs.getString("nutzername");
                return new IdentityContext(benutzerId, studentId, professorId, username, role);
            }
        }
    }

    // einfache DTO
    public static final class IdentityContext {
        public final Long benutzerId;
        public final Long studentId;
        public final Long professorId;
        public final String username;
        public final AppRole role;

        public IdentityContext(Long benutzerId, Long studentId, Long professorId) {
            this(benutzerId, studentId, professorId, null, null);
        }
        public IdentityContext(Long benutzerId, Long studentId, Long professorId, String username, AppRole role) {
            this.benutzerId = benutzerId;
            this.studentId = studentId;
            this.professorId = professorId;
            this.username = username;
            this.role = role;
        }
    }
}
