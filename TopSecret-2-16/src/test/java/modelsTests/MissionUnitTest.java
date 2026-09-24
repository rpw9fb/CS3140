package modelsTests;

import controller.DatabaseManager;
import models.Agent;
import models.Mission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MissionUnitTest {

    @Mock
    private DatabaseManager mockDb;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ========================
    // Mission Model Tests
    // ========================

    @Test
    public void testMissionConstructorAndGetters() {
        List<Integer> agentIds = Arrays.asList(1, 2, 3);
        Mission mission = new Mission(1, "Op Alpha", "Top secret", "2026-01-01", 1, agentIds);

        assertEquals(1, mission.getId());
        assertEquals("Op Alpha", mission.getTitle());
        assertEquals("Top secret", mission.getBrief());
        assertEquals("2026-01-01", mission.getDate());
        assertEquals(1, mission.getFacilityId());
        assertEquals(3, mission.getAgentIds().size());
    }

    @Test
    public void testMissionSetters() {
        Mission mission = new Mission(1, "Op Alpha", "brief", "2026-01-01", 1, new ArrayList<>());

        mission.setTitle("Op Beta");
        mission.setBrief("new brief");
        mission.setDate("2026-06-15");
        mission.setFacilityId(2);
        mission.setAgentIds(Arrays.asList(5, 6));

        assertEquals("Op Beta", mission.getTitle());
        assertEquals("new brief", mission.getBrief());
        assertEquals("2026-06-15", mission.getDate());
        assertEquals(2, mission.getFacilityId());
        assertEquals(2, mission.getAgentIds().size());
    }

    @Test
    public void testMissionAddAgentId() {
        Mission mission = new Mission(1, "Op", "brief", "2026-01-01", 1, new ArrayList<>());

        mission.addAgentId(1);
        mission.addAgentId(2);
        mission.addAgentId(1); // duplicate, should be ignored

        assertEquals(2, mission.getAgentIds().size());
        assertTrue(mission.getAgentIds().contains(1));
        assertTrue(mission.getAgentIds().contains(2));
    }

    @Test
    public void testMissionRemoveAgentId() {
        Mission mission = new Mission(1, "Op", "brief", "2026-01-01", 1, Arrays.asList(1, 2, 3));

        mission.removeAgentId(2);

        assertEquals(2, mission.getAgentIds().size());
        assertFalse(mission.getAgentIds().contains(2));
    }

    @Test
    public void testMissionRemoveNonExistentAgent() {
        Mission mission = new Mission(1, "Op", "brief", "2026-01-01", 1, Arrays.asList(1, 2));

        mission.removeAgentId(99); // should not throw

        assertEquals(2, mission.getAgentIds().size());
    }

    @Test
    public void testMissionToString() {
        Mission mission = new Mission(1, "Op Alpha", "brief", "2026-01-01", 2, Arrays.asList(1, 2, 3));
        String result = mission.toString();

        assertTrue(result.contains("Op Alpha"));
        assertTrue(result.contains("Facility 2"));
        assertTrue(result.contains("Agents: 3"));
    }

    @Test
    public void testMissionNullAgentIds() {
        Mission mission = new Mission(1, "Op", "brief", "2026-01-01", 1, null);

        assertNotNull(mission.getAgentIds());
        assertEquals(0, mission.getAgentIds().size());
    }

    @Test
    public void testMissionSetAgentIdsNull() {
        Mission mission = new Mission(1, "Op", "brief", "2026-01-01", 1, Arrays.asList(1));

        mission.setAgentIds(null);

        assertNotNull(mission.getAgentIds());
        assertEquals(0, mission.getAgentIds().size());
    }

    // =========================================
    // DatabaseManager Mock Tests - Missions
    // =========================================

    @Test
    public void testAddMissionMocked() throws SQLException {
        List<Integer> agentIds = Arrays.asList(1, 2);
        mockDb.addMission("Op1", "Secret Brief", "2026-01-01", 1, agentIds);
        verify(mockDb).addMission("Op1", "Secret Brief", "2026-01-01", 1, agentIds);
    }

    @Test
    public void testGetAllMissionsMocked() throws SQLException {
        Mission m1 = new Mission(1, "Op Alpha", "brief1", "2026-01-01", 1, Arrays.asList(1));
        Mission m2 = new Mission(2, "Op Beta", "brief2", "2026-02-01", 2, Arrays.asList(2, 3));

        when(mockDb.getAllMissions()).thenReturn(Arrays.asList(m1, m2));

        List<Mission> result = mockDb.getAllMissions();
        assertEquals(2, result.size());
        assertEquals("Op Alpha", result.get(0).getTitle());
        assertEquals("Op Beta", result.get(1).getTitle());
    }

    @Test
    public void testGetMissionByIdMocked() throws SQLException {
        Mission m = new Mission(1, "Op Alpha", "secret", "2026-01-01", 1, Arrays.asList(1, 2));

        when(mockDb.getMissionById(1)).thenReturn(m);
        when(mockDb.getMissionById(99)).thenReturn(null);

        Mission result = mockDb.getMissionById(1);
        assertNotNull(result);
        assertEquals("Op Alpha", result.getTitle());
        assertEquals(2, result.getAgentIds().size());

        assertNull(mockDb.getMissionById(99));
    }

    @Test
    public void testAssignAgentToMissionMocked() throws SQLException {
        mockDb.assignAgentToMission(1, 3);
        verify(mockDb).assignAgentToMission(1, 3);
    }

    @Test
    public void testRemoveAgentFromMissionMocked() throws SQLException {
        mockDb.removeAgentFromMission(1, 2);
        verify(mockDb).removeAgentFromMission(1, 2);
    }

    @Test
    public void testGetAgentsForMissionMocked() throws SQLException {
        Agent a1 = new Agent(1, "Alice", "1990-01-01", null, "Notes", 1);
        Agent a2 = new Agent(2, "Bob", "1985-05-05", null, "Notes", 2);

        when(mockDb.getAgentsForMission(1)).thenReturn(Arrays.asList(a1, a2));

        List<Agent> result = mockDb.getAgentsForMission(1);
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals("Bob", result.get(1).getName());
        // verify joint ops: agents from different facilities
        assertNotEquals(result.get(0).getFacilityId(), result.get(1).getFacilityId());
    }

    @Test
    public void testGetMissionsForAgentMocked() throws SQLException {
        Mission m1 = new Mission(1, "Op1", "b1", "2026-01-01", 1, Arrays.asList(1, 2));
        Mission m2 = new Mission(2, "Op2", "b2", "2026-02-01", 2, Arrays.asList(1));

        when(mockDb.getMissionsForAgent(1)).thenReturn(Arrays.asList(m1, m2));

        List<Mission> result = mockDb.getMissionsForAgent(1);
        assertEquals(2, result.size());
    }

    @Test
    public void testGetMissionsForAgentEmptyMocked() throws SQLException {
        when(mockDb.getMissionsForAgent(99)).thenReturn(new ArrayList<>());

        List<Mission> result = mockDb.getMissionsForAgent(99);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSearchMissionsMocked() throws SQLException {
        Mission m1 = new Mission(1, "Op1", "secret mission", "2026-01-01", 1, Arrays.asList(1));

        when(mockDb.searchMissions("secret")).thenReturn(Arrays.asList(m1));

        List<Mission> result = mockDb.searchMissions("secret");
        assertEquals(1, result.size());
        assertTrue(result.get(0).getBrief().contains("secret"));
    }

    @Test
    public void testSearchMissionsNoResultsMocked() throws SQLException {
        when(mockDb.searchMissions("nonexistent")).thenReturn(new ArrayList<>());

        List<Mission> result = mockDb.searchMissions("nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllMissionsEmptyMocked() throws SQLException {
        when(mockDb.getAllMissions()).thenReturn(new ArrayList<>());

        List<Mission> result = mockDb.getAllMissions();
        assertTrue(result.isEmpty());
    }
}
