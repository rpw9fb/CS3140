package modelsTests;

import static org.junit.jupiter.api.Assertions.*;

import controller.DatabaseManager;
import models.Agent;
import models.Mission;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

class MissionIntegrationTest {

    private DatabaseManager db;
    private static final String TEST_DB = "mission_integration_test.db";

    @BeforeEach
    void setup() throws Exception {
        File file = new File("data/" + TEST_DB);
        if (file.exists()) {
            file.delete();
        }
        db = new DatabaseManager(TEST_DB);
    }

    @AfterEach
    void tearDown() throws Exception {
        db.closeConnection();
        File file = new File("data/" + TEST_DB);
        if (file.exists()) {
            file.delete();
        }
    }

    // ===========================
    // Helper: seed test data
    // ===========================

    private void seedFacilitiesAndAgents() throws Exception {
        db.addFacility("CIA", "CIA");   // id = 1
        db.addFacility("NSA", "NSA");   // id = 2
        db.addAgent("Alice", "1990-01-01", "", "", 1);  // id = 1, facility 1
        db.addAgent("Bob", "1985-05-05", "", "", 2);    // id = 2, facility 2
        db.addAgent("Charlie", "1992-03-15", "", "", 1); // id = 3, facility 1
    }

    // ===========================
    // Joint Operations Tests
    // ===========================

    @Test
    void testAddMissionWithAgentsFromSameFacility() throws Exception {
        seedFacilitiesAndAgents();

        db.addMission("Op Alpha", "classified", "2026-01-01", 1, List.of(1, 3));

        Mission m = db.getMissionById(1);
        assertNotNull(m);
        assertEquals("Op Alpha", m.getTitle());
        assertEquals(2, m.getAgentIds().size());
    }

    @Test
    void testAddMissionWithAgentsFromDifferentFacilities() throws Exception {
        seedFacilitiesAndAgents();

        // Joint operation: agent 1 (CIA) and agent 2 (NSA) on a CIA mission
        db.addMission("Joint Op", "joint brief", "2026-02-01", 1, List.of(1, 2));

        Mission m = db.getMissionById(1);
        assertNotNull(m);
        assertEquals("Joint Op", m.getTitle());
        assertEquals(2, m.getAgentIds().size());
        assertTrue(m.getAgentIds().contains(1));
        assertTrue(m.getAgentIds().contains(2));

        // Verify agents are from different facilities
        List<Agent> agents = db.getAgentsForMission(1);
        assertEquals(2, agents.size());
        assertNotEquals(agents.get(0).getFacilityId(), agents.get(1).getFacilityId());
    }

    @Test
    void testAddMissionInvalidFacility() throws Exception {
        seedFacilitiesAndAgents();

        assertThrows(IllegalArgumentException.class, () -> {
            db.addMission("Bad Op", "brief", "2026-01-01", 999, List.of(1));
        });
    }

    @Test
    void testAddMissionInvalidAgent() throws Exception {
        seedFacilitiesAndAgents();

        assertThrows(IllegalArgumentException.class, () -> {
            db.addMission("Bad Op", "brief", "2026-01-01", 1, List.of(999));
        });
    }

    @Test
    void testAddMissionEmptyAgents() throws Exception {
        seedFacilitiesAndAgents();

        assertThrows(IllegalArgumentException.class, () -> {
            db.addMission("Bad Op", "brief", "2026-01-01", 1, List.of());
        });
    }

    @Test
    void testAddMissionNullAgents() throws Exception {
        seedFacilitiesAndAgents();

        assertThrows(IllegalArgumentException.class, () -> {
            db.addMission("Bad Op", "brief", "2026-01-01", 1, null);
        });
    }

    // ===========================
    // Get All Missions
    // ===========================

    @Test
    void testGetAllMissionsEmpty() throws Exception {
        List<Mission> missions = db.getAllMissions();
        assertTrue(missions.isEmpty());
    }

    @Test
    void testGetAllMissionsMultiple() throws Exception {
        seedFacilitiesAndAgents();

        db.addMission("Op A", "brief A", "2026-01-01", 1, List.of(1));
        db.addMission("Op B", "brief B", "2026-02-01", 2, List.of(2));

        List<Mission> missions = db.getAllMissions();
        assertEquals(2, missions.size());
        assertEquals("Op A", missions.get(0).getTitle());
        assertEquals("Op B", missions.get(1).getTitle());
    }

    // ===========================
    // Get Mission By ID
    // ===========================

    @Test
    void testGetMissionByIdExists() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Op Alpha", "classified", "2026-01-01", 1, List.of(1, 3));

        Mission m = db.getMissionById(1);
        assertNotNull(m);
        assertEquals("Op Alpha", m.getTitle());
        assertEquals("classified", m.getBrief());
        assertEquals("2026-01-01", m.getDate());
        assertEquals(1, m.getFacilityId());
        assertEquals(2, m.getAgentIds().size());
    }

    @Test
    void testGetMissionByIdNotFound() throws Exception {
        Mission m = db.getMissionById(999);
        assertNull(m);
    }

    // ===========================
    // Assign Agent to Mission
    // ===========================

    @Test
    void testAssignAgentToMission() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Op Alpha", "classified", "2026-01-01", 1, List.of(1));

        // Assign agent 2 (from NSA) to CIA mission - joint operation
        db.assignAgentToMission(1, 2);

        Mission m = db.getMissionById(1);
        assertEquals(2, m.getAgentIds().size());
        assertTrue(m.getAgentIds().contains(2));
    }

    @Test
    void testAssignAgentDuplicateIsIgnored() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Op Alpha", "classified", "2026-01-01", 1, List.of(1));

        // Assign agent 1 again - should be silently ignored
        db.assignAgentToMission(1, 1);

        Mission m = db.getMissionById(1);
        assertEquals(1, m.getAgentIds().size());
    }

    @Test
    void testAssignNonExistentAgent() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Op Alpha", "classified", "2026-01-01", 1, List.of(1));

        assertThrows(IllegalArgumentException.class, () -> {
            db.assignAgentToMission(1, 999);
        });
    }

    @Test
    void testAssignToNonExistentMission() throws Exception {
        seedFacilitiesAndAgents();

        assertThrows(IllegalArgumentException.class, () -> {
            db.assignAgentToMission(999, 1);
        });
    }

    // ===========================
    // Remove Agent from Mission
    // ===========================

    @Test
    void testRemoveAgentFromMission() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Op Alpha", "classified", "2026-01-01", 1, List.of(1, 3));

        db.removeAgentFromMission(1, 3);

        Mission m = db.getMissionById(1);
        assertEquals(1, m.getAgentIds().size());
        assertFalse(m.getAgentIds().contains(3));
    }

    @Test
    void testRemoveNonAssignedAgent() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Op Alpha", "classified", "2026-01-01", 1, List.of(1));

        // Remove agent 2 who is not assigned - should not throw
        db.removeAgentFromMission(1, 2);

        Mission m = db.getMissionById(1);
        assertEquals(1, m.getAgentIds().size());
    }

    // ===========================
    // Get Agents For Mission
    // ===========================

    @Test
    void testGetAgentsForMission() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Joint Op", "brief", "2026-01-01", 1, List.of(1, 2, 3));

        List<Agent> agents = db.getAgentsForMission(1);
        assertEquals(3, agents.size());
        assertEquals("Alice", agents.get(0).getName());
        assertEquals("Bob", agents.get(1).getName());
        assertEquals("Charlie", agents.get(2).getName());
    }

    @Test
    void testGetAgentsForMissionEmpty() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Op", "brief", "2026-01-01", 1, List.of(1));
        db.removeAgentFromMission(1, 1);

        List<Agent> agents = db.getAgentsForMission(1);
        assertTrue(agents.isEmpty());
    }

    // ===========================
    // Get Missions For Agent
    // ===========================

    @Test
    void testGetMissionsForAgent() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Op A", "brief A", "2026-01-01", 1, List.of(1));
        db.addMission("Op B", "brief B", "2026-02-01", 2, List.of(1, 2));

        List<Mission> missions = db.getMissionsForAgent(1);
        assertEquals(2, missions.size());
        assertEquals("Op A", missions.get(0).getTitle());
        assertEquals("Op B", missions.get(1).getTitle());
    }

    @Test
    void testGetMissionsForAgentNone() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Op A", "brief A", "2026-01-01", 1, List.of(1));

        List<Mission> missions = db.getMissionsForAgent(2);
        assertTrue(missions.isEmpty());
    }

    @Test
    void testAgentOnMultipleMissions() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Op A", "brief A", "2026-01-01", 1, List.of(1));
        db.addMission("Op B", "brief B", "2026-02-01", 1, List.of(1));
        db.addMission("Op C", "brief C", "2026-03-01", 2, List.of(1, 2));

        List<Mission> missions = db.getMissionsForAgent(1);
        assertEquals(3, missions.size());
    }

    // ===========================
    // Search Missions
    // ===========================

    @Test
    void testSearchMissionsFound() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Op Alpha", "secret classified mission", "2026-01-01", 1, List.of(1));
        db.addMission("Op Beta", "public mission", "2026-02-01", 1, List.of(1));

        List<Mission> results = db.searchMissions("secret");
        assertEquals(1, results.size());
        assertEquals("Op Alpha", results.get(0).getTitle());
    }

    @Test
    void testSearchMissionsNotFound() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Op Alpha", "classified", "2026-01-01", 1, List.of(1));

        List<Mission> results = db.searchMissions("nonexistent");
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchMissionsCaseInsensitive() throws Exception {
        seedFacilitiesAndAgents();
        db.addMission("Op Alpha", "Secret Mission", "2026-01-01", 1, List.of(1));

        List<Mission> results = db.searchMissions("SECRET");
        assertEquals(1, results.size());
    }

    // ===========================
    // End-to-End Joint Operation
    // ===========================

    @Test
    void testFullJointOperationWorkflow() throws Exception {
        seedFacilitiesAndAgents();

        // Create a mission run by CIA with only a CIA agent
        db.addMission("Joint Strike", "multi-agency operation", "2026-06-01", 1, List.of(1));

        // Later assign NSA agent (from facility 2) to this CIA mission
        db.assignAgentToMission(1, 2);

        // Also assign another CIA agent
        db.assignAgentToMission(1, 3);

        // Verify mission has 3 agents from 2 different facilities
        Mission m = db.getMissionById(1);
        assertEquals(3, m.getAgentIds().size());

        List<Agent> agents = db.getAgentsForMission(1);
        assertEquals(3, agents.size());

        // Verify agent 1 (CIA) and agent 2 (NSA) are from different facilities
        boolean hasCIA = agents.stream().anyMatch(a -> a.getFacilityId() == 1);
        boolean hasNSA = agents.stream().anyMatch(a -> a.getFacilityId() == 2);
        assertTrue(hasCIA);
        assertTrue(hasNSA);

        // Verify agent 2 can see this mission
        List<Mission> agentMissions = db.getMissionsForAgent(2);
        assertEquals(1, agentMissions.size());
        assertEquals("Joint Strike", agentMissions.get(0).getTitle());

        // Remove agent 2 from the mission
        db.removeAgentFromMission(1, 2);

        agents = db.getAgentsForMission(1);
        assertEquals(2, agents.size());
        assertFalse(agents.stream().anyMatch(a -> a.getId() == 2));
    }
}
