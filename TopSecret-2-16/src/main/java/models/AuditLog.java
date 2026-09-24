package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class AuditLog {

    private final Connection connection;

    public static final String EVENT_READ = "READ";
    public static final String EVENT_CREATE = "CREATE";
    public static final String EVENT_UPDATE = "UPDATE";

    public static final String TYPE_BRIEF = "BRIEF";
    public static final String TYPE_AGENT = "AGENT";
    public static final String TYPE_FACILITY = "FACILITY";
    public static final String TYPE_MISSION = "MISSION";
    public static final String TYPE_USER_ACCOUNT = "USER_ACCOUNT";
    public static final String TYPE_LOGIN = "LOGIN";
    public static final String TYPE_MESSAGE = "MESSAGE";

    public AuditLog(Connection connection) throws SQLException {
        this.connection = connection;
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS audit_log (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        event_type TEXT NOT NULL,
                        content_type TEXT NOT NULL,
                        content_id TEXT,
                        username TEXT NOT NULL,
                        occurred_at TEXT NOT NULL
                    );
                    """);
        }
    }

    public void record(
            String eventType,
            String contentType,
            String contentId,
            String username
    ) throws SQLException {
        record(eventType, contentType, contentId, username, Instant.now().toString());
    }

    public void recordRead(String contentType, String contentId, String username) throws SQLException {
        record(EVENT_READ, contentType, contentId, username);
    }

    public void recordCreate(String contentType, String contentId, String username) throws SQLException {
        record(EVENT_CREATE, contentType, contentId, username);
    }

    public void record(
            String eventType,
            String contentType,
            String contentId,
            String username,
            String occurredAt
    ) throws SQLException {
        String sql = """
                INSERT INTO audit_log (event_type, content_type, content_id, username, occurred_at)
                VALUES (?, ?, ?, ?, ?);
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, eventType);
            ps.setString(2, contentType);
            ps.setString(3, contentId);
            ps.setString(4, username);
            ps.setString(5, occurredAt);
            ps.executeUpdate();
        }
    }

    public List<String> readAllLines() throws SQLException {
        List<String> lines = new ArrayList<>();
        String sql = """
                SELECT event_type, content_type, content_id, username, occurred_at
                FROM audit_log
                ORDER BY occurred_at ASC, id ASC;
                """;
        try (
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)
        ) {
            while (rs.next()) {
                String contentId = rs.getString("content_id");
                String idPart = contentId == null ? "-" : contentId;
                lines.add(String.format(
                        "%s | %s | %s | user=%s | %s",
                        rs.getString("event_type"),
                        rs.getString("content_type"),
                        idPart,
                        rs.getString("username"),
                        rs.getString("occurred_at")
                ));
            }
        }
        return lines;
    }
}
