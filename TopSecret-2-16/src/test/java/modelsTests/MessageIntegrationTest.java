package modelsTests;

import controller.DatabaseManager;
import controller.ProjectControl;
import models.Agent;
import models.Facility;
import models.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for agent messages: SQLite + {@link ProjectControl}.
 */
class MessageIntegrationTest {

    private ProjectControl pc;
    private DatabaseManager db;

    @BeforeEach
    void setup() throws Exception {
        db = new DatabaseManager(":memory:", new Random(), true);
        pc = new ProjectControl(db);
    }

    @AfterEach
    void tearDown() throws Exception {
        db.closeConnection();
    }

    @Test
    void senderStoresMessageRecipientReadsCaseInsensitive() throws SQLException {
        pc.addFacility("HQ", "HQ");
        List<Facility> facs = pc.listFacilities();
        int facilityId = facs.get(facs.size() - 1).getID();
        pc.addAgent("Ghost", "1980-01-01", "", "", facilityId);
        List<Agent> agents = pc.listAgents();
        int ghostId = agents.get(agents.size() - 1).getId();

        pc.setCurrentUsername("director");
        pc.addAgentMessageForCurrentUser(ghostId, "Rendezvous at bridge.");

        pc.setCurrentUsername("ghost");
        List<Message> inbox = pc.getAgentMessagesForCurrentUser();
        assertEquals(1, inbox.size());
        assertEquals("Rendezvous at bridge.", inbox.get(0).getBody());
        assertEquals("director", inbox.get(0).getSenderUsername());
        assertEquals(ghostId, inbox.get(0).getRecipientAgentId());

        pc.setCurrentUsername("GHOST");
        assertEquals(1, pc.getAgentMessagesForCurrentUser().size());
    }

    @Test
    void wrongUsernameSeesEmptyInbox() throws SQLException {
        pc.addFacility("HQ", "HQ");
        int facilityId = pc.listFacilities().get(pc.listFacilities().size() - 1).getID();
        pc.addAgent("Alpha", "1970", "", "", facilityId);
        pc.addAgent("Beta", "1971", "", "", facilityId);
        List<Agent> agents = pc.listAgents();
        int alphaId = agents.stream().filter(a -> a.getName().equals("Alpha")).findFirst().orElseThrow().getId();

        pc.setCurrentUsername("user1");
        pc.addAgentMessageForCurrentUser(alphaId, "For Alpha only");

        pc.setCurrentUsername("beta");
        assertTrue(pc.getAgentMessagesForCurrentUser().isEmpty());

        pc.setCurrentUsername("alpha");
        assertEquals(1, pc.getAgentMessagesForCurrentUser().size());
    }

    @Test
    void addAgentMessageForUnknownAgentThrows() throws SQLException {
        pc.addFacility("HQ", "HQ");
        pc.setCurrentUsername("sender");
        assertThrows(IllegalArgumentException.class, () ->
                pc.addAgentMessageForCurrentUser(9999, "Nobody"));
    }
}
