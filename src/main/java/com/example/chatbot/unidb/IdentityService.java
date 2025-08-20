package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;

@ApplicationScoped
public class IdentityService {

    public static final class IdentityContext {
        public final Long benutzerId;   // aus benutzer.benutzer_id (JWT userId)
        public final Long studentId;    // Mapping, falls Student
        public final Long professorId;  // Mapping, falls Professor
        public final String username;
        public final String roleName;   // 'Student' / 'Professor' / 'Admin'

        public IdentityContext(Long benutzerId, Long studentId, Long professorId,
                               String username, String roleName) {
            this.benutzerId = benutzerId;
            this.studentId = studentId;
            this.professorId = professorId;
            this.username = username;
            this.roleName = roleName;
        }
    }

    @Inject @io.quarkus.agroal.DataSource("unidb")
    DataSource ds;

    /** Holt student_id/professor_id zu einer benutzer_id; null-sicher. */
    public IdentityContext resolveByBenutzerId(Long benutzerId, AppRole appRole) {
        if (benutzerId == null) return new IdentityContext(null, null, null, null, null);
        String sql = """
            SELECT b.benutzer_id, b.nutzername, r.rollenname,
                   b.student_id, b.professor_id
            FROM benutzer b
            LEFT JOIN rollen r ON r.rolle_id = b.rolle_id
            WHERE b.benutzer_id = ?
        """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, benutzerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return new IdentityContext(benutzerId, null, null, null, null);
                return new IdentityContext(
                        rs.getLong("benutzer_id"),
                        (Long) rs.getObject("student_id"),
                        (Long) rs.getObject("professor_id"),
                        rs.getString("nutzername"),
                        rs.getString("rollenname")
                );
            }
        } catch (SQLException e) {
            // defensiv: lieber ein leerer Kontext als harter Crash
            return new IdentityContext(benutzerId, null, null, null, null);
        }
    }
}
