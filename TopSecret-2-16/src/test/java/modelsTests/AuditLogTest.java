package modelsTests;

import models.AuditLog;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditLogTest {

    @Test
    void constructor_initializesSchema() throws Exception {
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);

        new AuditLog(connection);

        verify(statement, times(1)).executeUpdate(anyString());
    }

    @Test
    void record_withExplicitTimestamp_bindsAndExecutesInsert() throws Exception {
        Connection connection = mock(Connection.class);
        Statement schemaStatement = mock(Statement.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.createStatement()).thenReturn(schemaStatement);
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        AuditLog log = new AuditLog(connection);
        log.record("READ", "BRIEF", "1", "alice", "2026-01-01T00:00:00Z");

        verify(ps).setString(1, "READ");
        verify(ps).setString(2, "BRIEF");
        verify(ps).setString(3, "1");
        verify(ps).setString(4, "alice");
        verify(ps).setString(5, "2026-01-01T00:00:00Z");
        verify(ps).executeUpdate();
    }

    @Test
    void recordRead_delegatesToReadEventType() throws Exception {
        Connection connection = mock(Connection.class);
        Statement schemaStatement = mock(Statement.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.createStatement()).thenReturn(schemaStatement);
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        AuditLog log = new AuditLog(connection);
        log.recordRead(AuditLog.TYPE_AGENT, "7", "carol");

        verify(ps).setString(1, AuditLog.EVENT_READ);
        verify(ps).setString(2, AuditLog.TYPE_AGENT);
        verify(ps).setString(3, "7");
        verify(ps).setString(4, "carol");
        verify(ps).executeUpdate();
    }

    @Test
    void recordCreate_delegatesToCreateEventType() throws Exception {
        Connection connection = mock(Connection.class);
        Statement schemaStatement = mock(Statement.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.createStatement()).thenReturn(schemaStatement);
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        AuditLog log = new AuditLog(connection);
        log.recordCreate(AuditLog.TYPE_USER_ACCOUNT, "new_user", "admin");

        verify(ps).setString(1, AuditLog.EVENT_CREATE);
        verify(ps).setString(2, AuditLog.TYPE_USER_ACCOUNT);
        verify(ps).setString(3, "new_user");
        verify(ps).setString(4, "admin");
        verify(ps).executeUpdate();
    }

    @Test
    void readAllLines_formatsRowsIncludingNullContentId() throws Exception {
        Connection connection = mock(Connection.class);
        Statement schemaStatement = mock(Statement.class);
        Statement queryStatement = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(schemaStatement, queryStatement);
        when(queryStatement.executeQuery(anyString())).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("event_type")).thenReturn("READ", "CREATE");
        when(rs.getString("content_type")).thenReturn("BRIEF", "USER_ACCOUNT");
        when(rs.getString("content_id")).thenReturn("1", null);
        when(rs.getString("username")).thenReturn("alice", "admin");
        when(rs.getString("occurred_at")).thenReturn("t1", "t2");

        AuditLog log = new AuditLog(connection);
        List<String> lines = log.readAllLines();

        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("READ | BRIEF | 1 | user=alice | t1"));
        assertTrue(lines.get(1).contains("CREATE | USER_ACCOUNT | - | user=admin | t2"));
    }
}

