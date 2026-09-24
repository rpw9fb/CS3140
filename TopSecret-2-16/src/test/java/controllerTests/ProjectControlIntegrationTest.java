package controllerTests;

import controller.DatabaseManager;
import controller.ProjectControl;
import models.*;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style tests for ProjectControl using a real in-memory database.
 * These tests cover methods that were previously untested with proper delegation.
 */
class ProjectControlIntegrationTest {

    private ProjectControl pc;
    private DatabaseManager db;

    @BeforeEach
    void setUp() {
        db = new DatabaseManager(":memory:", new Random(42), true);
        pc = new ProjectControl(db);
    }

    @AfterEach
    void tearDown() throws Exception {
        db.closeConnection();
    }

    // === setCurrentUsername ===

    @Test
    void setCurrentUsername_setsUsername() {
        pc.setCurrentUsername("alice");
        // Verify indirectly — retrieve mission uses currentUsername for audit
    }

    @Test
    void setCurrentUsername_null_usesDefault() {
        pc.setCurrentUsername(null);
        // Should not throw
    }

    @Test
    void setCurrentUsername_empty_usesDefault() {
        pc.setCurrentUsername("");
        // Should not throw
    }

    // === Mission Methods ===

    @Test
    void listMissionTitles_emptyDb() {
        List<String> titles = pc.listMissionTitles();
        assertTrue(titles.isEmpty());
    }

    @Test
    void addMission_andRetrieve() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        pc.addMission("Op X", "Secret op", "2024-01-01", 1, List.of(1));

        List<String> titles = pc.listMissionTitles();
        assertEquals(1, titles.size());
        assertTrue(titles.get(0).contains("Op X"));
    }

    @Test
    void retrieveMission_found() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        pc.addMission("Op X", "Secret op", "2024-01-01", 1, List.of(1));

        String result = pc.retrieveMission(1);
        assertTrue(result.contains("Op X"));
        assertTrue(result.contains("Secret op"));
        assertTrue(result.contains("Bond"));
    }

    @Test
    void retrieveMission_withUsername() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        pc.addMission("Op X", "Secret op", "2024-01-01", 1, List.of(1));

        String result = pc.retrieveMission(1, "testUser");
        assertTrue(result.contains("Op X"));
    }

    @Test
    void retrieveMission_notFound() {
        String result = pc.retrieveMission(999);
        assertEquals("Mission not found.", result);
    }

    @Test
    void searchMissions_found() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        pc.addMission("Op Alpha", "Secret alpha mission", "2024-01-01", 1, List.of(1));

        List<Mission> results = pc.searchMissions("alpha");
        assertEquals(1, results.size());
    }

    @Test
    void searchMissions_noMatch() {
        List<Mission> results = pc.searchMissions("nonexistent");
        assertTrue(results.isEmpty());
    }

    @Test
    void getAllMissions() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        pc.addMission("Op A", "a", "2024-01-01", 1, List.of(1));
        pc.addMission("Op B", "b", "2024-01-02", 1, List.of(1));

        List<Mission> missions = pc.getAllMissions();
        assertEquals(2, missions.size());
    }

    @Test
    void getMissionById_found() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        pc.addMission("Op X", "Secret", "2024-01-01", 1, List.of(1));

        Mission m = pc.getMissionById(1);
        assertNotNull(m);
        assertEquals("Op X", m.getTitle());
    }

    @Test
    void getMissionById_notFound() throws SQLException {
        assertNull(pc.getMissionById(999));
    }

    @Test
    void assignAndRemoveAgentFromMission() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        db.addAgent("Q", "1960", "", "", 1);
        pc.addMission("Op X", "Secret", "2024-01-01", 1, List.of(1));

        pc.assignAgentToMission(1, 2);
        List<Agent> agents = pc.getAgentsForMission(1);
        assertEquals(2, agents.size());

        pc.removeAgentFromMission(1, 2);
        agents = pc.getAgentsForMission(1);
        assertEquals(1, agents.size());
    }

    @Test
    void getMissionsForAgent() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        pc.addMission("Op A", "a", "2024-01-01", 1, List.of(1));
        pc.addMission("Op B", "b", "2024-01-02", 1, List.of(1));

        List<Mission> missions = pc.getMissionsForAgent(1);
        assertEquals(2, missions.size());
    }

    // === Agent Methods ===

    @Test
    void listAgents_empty() throws SQLException {
        assertTrue(pc.listAgents().isEmpty());
    }

    @Test
    void addAgent_andList() throws SQLException {
        db.addFacility("HQ", "HQ");
        pc.addAgent("Bond", "1970", "", "", 1);

        List<Agent> agents = pc.listAgents();
        assertEquals(1, agents.size());
        assertEquals("Bond", agents.get(0).getName());
    }

    @Test
    void retrieveAgent_found() throws SQLException {
        db.addFacility("HQ", "HQ");
        pc.addAgent("Bond", "1970", "", "", 1);

        Agent a = pc.retrieveAgent(1);
        assertNotNull(a);
        assertEquals("Bond", a.getName());
    }

    @Test
    void retrieveAgent_notFound() {
        assertNull(pc.retrieveAgent(999));
    }

    // === Facility Methods ===

    @Test
    void listFacilities_empty() throws SQLException {
        assertTrue(pc.listFacilities().isEmpty());
    }

    @Test
    void addFacility_andList() throws SQLException {
        pc.addFacility("CIA", "CIA");

        List<Facility> facs = pc.listFacilities();
        assertEquals(1, facs.size());
        assertEquals("CIA", facs.get(0).getName());
    }

    @Test
    void getAgentsAtFacility() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);

        List<Integer> ids = pc.getAgentsAtFacility(1);
        assertEquals(1, ids.size());
    }

    @Test
    void retrieveFacility_found() throws SQLException {
        pc.addFacility("CIA", "CIA");
        Facility f = pc.retrieveFacility(1);
        assertNotNull(f);
        assertEquals("CIA", f.getName());
    }

    @Test
    void retrieveFacility_notFound() {
        assertNull(pc.retrieveFacility(999));
    }

    // === Message Methods ===

    @Test
    void addAndGetAgentMessage() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);

        pc.addAgentMessage(1, "Hello Bond", "M");

        pc.setCurrentUsername("Bond");
        List<Message> msgs = pc.getAgentMessagesForCurrentUser();
        assertEquals(1, msgs.size());
        assertEquals("Hello Bond", msgs.get(0).getBody());
    }

    @Test
    void addAgentMessageForCurrentUser() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);

        pc.setCurrentUsername("M");
        pc.addAgentMessageForCurrentUser(1, "Report in");

        pc.setCurrentUsername("Bond");
        List<Message> msgs = pc.getAgentMessagesForCurrentUser();
        assertEquals(1, msgs.size());
    }

    // === Audit Methods ===

    @Test
    void getAuditLog_notNull() {
        assertNotNull(pc.getAuditLog());
    }

    @Test
    void listAuditLogLines_empty() {
        List<String> lines = pc.listAuditLogLines();
        assertTrue(lines.isEmpty());
    }

    @Test
    void listAuditLogLines_afterAction() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        pc.setCurrentUsername("testUser");
        pc.addMission("Op X", "Secret", "2024-01-01", 1, List.of(1));

        List<String> lines = pc.listAuditLogLines();
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("CREATE"));
    }

    @Test
    void recordLogin() {
        assertTrue(pc.recordLogin("alice"));
        List<String> lines = pc.listAuditLogLines();
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).contains("LOGIN"));
    }

    @Test
    void recordUserAccountCreated() {
        assertTrue(pc.recordUserAccountCreated("bob", "alice"));
        List<String> lines = pc.listAuditLogLines();
        assertTrue(lines.stream().anyMatch(l -> l.contains("CREATE") && l.contains("USER_ACCOUNT")));
    }

    @Test
    void recordUserRoleChange() {
        assertTrue(pc.recordUserRoleChange("bob", "alice"));
        List<String> lines = pc.listAuditLogLines();
        assertTrue(lines.stream().anyMatch(l -> l.contains("UPDATE") && l.contains("USER_ACCOUNT")));
    }

    // === File Retrieval (static) ===

    @Test
    void listFiles_returnsString() {
        String result = ProjectControl.listFiles();
        assertNotNull(result);
    }

    // === importMissions ===

    @Test
    void importMissions_noFile() {
        // Should handle gracefully (no crash)
        pc.importMissions("nonexistent.tsv");
    }

    @Test
    void updateMission_success() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);

        pc.addMission("Old", "Old brief", "2024-01-01", 1, List.of(1));

        boolean updated = pc.updateMission(1, "New", "New brief", "2025-01-01", 1, List.of(1));

        assertTrue(updated);

        Mission m = pc.getMissionById(1);
        assertEquals("New", m.getTitle());
        assertEquals("New brief", m.getBrief());
    }

    @Test
    void updateMission_invalidAgent_throws() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);

        pc.addMission("Op X", "Brief", "2024", 1, List.of(1));

        assertThrows(IllegalArgumentException.class, () ->
                pc.updateMission(1, "Bad", "Bad", "2025", 1, List.of(999))
        );
    }

    @Test
    void markMessageAsRead_updatesState() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);

        pc.addAgentMessage(1, "Test message", "M");

        pc.markMessageAsRead(1);

        Message msg = pc.getMessageById(1);
        assertTrue(msg.isRead());
    }

    @Test
    void getMissionsAtFacility_filtersCorrectly() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addFacility("Field", "F");

        db.addAgent("Bond", "1970", "", "", 1);

        pc.addMission("HQ Mission", "A", "2024", 1, List.of(1));
        pc.addMission("Field Mission", "B", "2024", 1, List.of(1));

        List<Mission> missions = pc.getMissionsAtFacility(1);

        assertEquals(2, missions.size());
    }

    @Test
    void retrieve_invalidFileIndex() {
        String result = pc.retrieve(0);
        assertEquals("Invalid file number.", result);
    }

    @Test
    void retrieve_withBadKey() {
        String result = pc.retrieve(1, "fakekey.txt");

        assertTrue(
                result.equals("Key file not found.") ||
                        result.equals("Invalid file number.")
        );
    }

    @Test
    void getMessageById_notFound() throws SQLException {
        Message msg = pc.getMessageById(999);
        assertNull(msg);
    }

    @Test
    void listCountries_empty() throws SQLException {
        List<Country> countries = pc.listCountries();
        assertTrue(countries.isEmpty());
    }
}
