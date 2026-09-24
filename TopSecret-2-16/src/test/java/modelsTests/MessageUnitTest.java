package modelsTests;

import controller.DatabaseManager;
import models.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Message} and mocked {@link DatabaseManager} message APIs.
 */
class MessageUnitTest {

    @org.mockito.Mock
    private DatabaseManager mockDb;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void messageExposesStoredFields() {
        Message m = new Message(42, 7, "body text", "alice");
        assertEquals(42, m.getId());
        assertEquals(7, m.getRecipientAgentId());
        assertEquals("body text", m.getBody());
        assertEquals("alice", m.getSenderUsername());
    }

    @Test
    void addAgentMessageCanBeVerifiedOnMock() throws SQLException {
        mockDb.addAgentMessage(1, "Hello", "sender");
        verify(mockDb).addAgentMessage(1, "Hello", "sender");
    }

    @Test
    void getAgentMessagesForRecipientUsernameCanBeStubbed() throws SQLException {
        Message msg = new Message(1, 2, "Brief text", "director");
        when(mockDb.getAgentMessagesForRecipientUsername("ghost")).thenReturn(Arrays.asList(msg));

        List<Message> result = mockDb.getAgentMessagesForRecipientUsername("ghost");
        assertEquals(1, result.size());
        assertEquals("Brief text", result.get(0).getBody());
        assertEquals("director", result.get(0).getSenderUsername());
        assertEquals(2, result.get(0).getRecipientAgentId());
    }
}
