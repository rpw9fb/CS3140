package controllerTests;

import controller.DatabaseManager;
import models.AuditLog;
import models.Country;
import models.Facility;
import models.Message;
import models.Mission;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DatabaseManagerTest {

    private DatabaseManager db;

    @BeforeEach
    void setUp() {
        db = new DatabaseManager(":memory:", new Random(1), true);
    }

    @AfterEach
    void tearDown() throws Exception {
        db.closeConnection();
    }

    // === Constructor / Connection ===

    @Test
    void testConstructorWithInvalidDB() {
        DatabaseManager bad = new DatabaseManager("/invalid/path/db.sqlite");
        assertNotNull(bad);
    }

    @Test
    void defaultConstructor_createsDb() {
        DatabaseManager db2 = new DatabaseManager("test_coverage_ctor.db");
        assertNotNull(db2);
        try { db2.closeConnection(); } catch (Exception ignored) {}
        new java.io.File("data/test_coverage_ctor.db").delete();
    }

    @Test
    void closeConnection_twice_noError() throws SQLException {
        db.closeConnection();
        db.closeConnection();
    }

    // === AuditLog ===

    @Test
    void getAuditLog_notNull() {
        assertNotNull(db.getAuditLog());
    }

    @Test
    void testAuditLogCalled() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);
        db.addMission("M", "B", "2020", 1, List.of(1));

        AuditLog mockLog = mock(AuditLog.class);
        Field f = DatabaseManager.class.getDeclaredField("auditLog");
        f.setAccessible(true);
        f.set(db, mockLog);

        db.getMissionByNumber(1, "tester");
        verify(mockLog).recordRead(eq(AuditLog.TYPE_BRIEF), eq("1"), eq("tester"));
    }

    // === Facilities ===

    @Test
    void testAddFacilityAndFetch() throws Exception {
        db.addFacility("HQ", "HQ1");
        List<Facility> list = db.getAllFacilities();
        assertEquals(1, list.size());
        assertEquals("HQ", list.get(0).getName());
    }

    @Test
    void testFacilityExistsTrueFalse() throws Exception {
        db.addFacility("HQ", "HQ1");
        assertTrue(db.facilityExists(1));
        assertFalse(db.facilityExists(999));
    }

    @Test
    void facilityExists_false() throws SQLException {
        assertFalse(db.facilityExists(999));
    }

    @Test
    void testFacilityDelete() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);
        assertFalse(db.facilityDelete(1));
    }

    @Test
    void facilityDelete_noAgentsOrMissions() throws SQLException {
        db.addFacility("Empty Facility", "EF");
        assertTrue(db.facilityDelete(1));
    }

    @Test
    void facilityDelete_hasMission() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        db.addMission("Op X", "Secret", "2024-01-01", 1, List.of(1));
        assertFalse(db.facilityDelete(1));
    }

    @Test
    void testUpdateFacility() throws Exception {
        db.addFacility("HQ", "HQ1");
        assertTrue(db.updateFacility(1, "New HQ", "NHQ"));
        List<Facility> facilities = db.getAllFacilities();
        assertEquals("New HQ", facilities.get(0).getName());
    }

    // === Agents ===

    @Test
    void testAddAgentValid() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("Bond", "1990", "", "note", 1);
        assertEquals(1, db.getAllAgents().size());
    }

    @Test
    void testAddAgentInvalidFacility() {
        assertThrows(IllegalArgumentException.class,
                () -> db.addAgent("Bad", "", "", "", 999));
    }

    @Test
    void addAgent_invalidFacility() {
        assertThrows(IllegalArgumentException.class, () ->
                db.addAgent("Bond", "1970", "", "", 999));
    }

    @Test
    void testAgentExists() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);
        assertTrue(db.agentExists(1));
        assertFalse(db.agentExists(2));
    }

    @Test
    void testAgentToFacility() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);
        assertTrue(db.agentToFacility(1, 1));
        assertFalse(db.agentToFacility(1, 999));
    }

    @Test
    void agentToFacility_validMatch() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        assertTrue(db.agentToFacility(1, 1));
    }

    @Test
    void agentToFacility_noMatch() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        assertFalse(db.agentToFacility(1, 999));
    }

    @Test
    void testAgentDelete() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);

        db.addMission("M", "B", "2020", 1, List.of(1));
        assertFalse(db.agentDelete(1)); // now agent is in mission → cannot delete
    }

    @Test
    void agentDelete_notInMission() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Free Agent", "1970", "", "", 1);
        assertTrue(db.agentDelete(1));
    }

    @Test
    void testUpdateAgent() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addFacility("Field", "F1");
        db.addAgent("Bond", "1990", "", "old", 1);
        assertTrue(db.updateAgent(1, "Bond Jr", "1991", "", "new", 2));
        assertEquals("Bond Jr", db.getAllAgents().get(0).getName());
    }

    @Test
    void loadAgents_fileNotFound() {
        db.loadAgents("nonexistent.tsv");
    }

    @Test
    void getAgentsFacility_empty() throws SQLException {
        db.addFacility("HQ", "HQ");
        assertTrue(db.getAgentsFacility(1).isEmpty());
    }

    // === Missions ===

    @Test
    void testAddMissionValid() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);
        db.addMission("Test", "Brief", "2020", 1, List.of(1));
        assertEquals(1, db.getAllMissions().size());
    }

    @Test
    void testAddMissionInvalidFacility() {
        assertThrows(IllegalArgumentException.class,
                () -> db.addMission("Test", "B", "2020", 99, List.of(1)));
    }

    @Test
    void addMission_invalidFacility() {
        assertThrows(IllegalArgumentException.class, () ->
                db.addMission("Op", "brief", "date", 999, List.of()));
    }

    @Test
    void testAddMissionEmptyAgents() throws Exception {
        db.addFacility("HQ", "HQ1");
        assertThrows(IllegalArgumentException.class,
                () -> db.addMission("Test", "B", "2020", 1, new ArrayList<>()));
    }

    @Test
    void addMission_emptyAgentList() throws SQLException {
        db.addFacility("HQ", "HQ");
        assertThrows(IllegalArgumentException.class, () ->
                db.addMission("Op", "brief", "date", 1, List.of()));
    }

    @Test
    void testAddMissionInvalidAgent() throws Exception {
        db.addFacility("HQ", "HQ1");
        assertThrows(IllegalArgumentException.class,
                () -> db.addMission("Test", "B", "2020", 1, List.of(999)));
    }

    @Test
    void addMission_invalidAgent() throws SQLException {
        db.addFacility("HQ", "HQ");
        assertThrows(IllegalArgumentException.class, () ->
                db.addMission("Op", "brief", "date", 1, List.of(999)));
    }

    @Test
    void testGetMissionById() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);
        db.addMission("M", "B", "2020", 1, List.of(1));
        Mission m = db.getMissionById(1);
        assertNotNull(m);
        assertEquals("M", m.getTitle());
    }

    @Test
    void testGetMissionByIdNull() throws Exception {
        assertNull(db.getMissionById(999));
    }

    @Test
    void getMissionById_notFound() throws SQLException {
        assertNull(db.getMissionById(999));
    }

    @Test
    void getMissionByNumber_notFound() {
        assertNull(db.getMissionByNumber(999));
    }

    @Test
    void getMissionByNumber_withUsername() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        db.addMission("Op X", "Secret", "2024-01-01", 1, List.of(1));
        String result = db.getMissionByNumber(1, "testUser");
        assertNotNull(result);
        assertTrue(result.contains("Op X"));
    }

    @Test
    void getMissionByNumber_defaultUsername() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        db.addMission("Op X", "Secret", "2024-01-01", 1, List.of(1));
        String result = db.getMissionByNumber(1);
        assertNotNull(result);
    }

    @Test
    void getMissionByNumber_emptyUsername() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        db.addMission("Op X", "Secret", "2024-01-01", 1, List.of(1));
        String result = db.getMissionByNumber(1, "");
        assertNotNull(result);
    }

    @Test
    void getMissionByNumber_nullUsername() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        db.addMission("Op X", "Secret", "2024-01-01", 1, List.of(1));
        String result = db.getMissionByNumber(1, null);
        assertNotNull(result);
    }

    @Test
    void testGetMissionByNumberNullUser() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);
        db.addMission("M", "B", "2020", 1, List.of(1));
        String result = db.getMissionByNumber(1, "");
        assertTrue(result.contains("M"));
    }

    @Test
    void testGetMissionByNumberNotFound() {
        assertNull(db.getMissionByNumber(999));
    }

    @Test
    void testAssignAgentToMission() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);
        db.addAgent("B", "", "", "", 1);
        db.addMission("M", "B", "2020", 1, List.of(1));
        db.assignAgentToMission(1, 2);
        assertEquals(2, db.getAgentsForMission(1).size());
    }

    @Test
    void testAssignDuplicateIgnored() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);
        db.addMission("M", "B", "2020", 1, List.of(1));
        db.assignAgentToMission(1, 1);
        assertEquals(1, db.getAgentsForMission(1).size());
    }

    @Test
    void assignAgentToMission_duplicate() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        db.addMission("Op", "brief", "date", 1, List.of(1));
        db.assignAgentToMission(1, 1);
    }

    @Test
    void testAssignInvalidAgent() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);
        db.addMission("M", "B", "2020", 1, List.of(1));
        assertThrows(IllegalArgumentException.class,
                () -> db.assignAgentToMission(1, 999));
    }

    @Test
    void assignAgentToMission_invalidAgent() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        db.addMission("Op", "brief", "date", 1, List.of(1));
        assertThrows(IllegalArgumentException.class, () ->
                db.assignAgentToMission(1, 999));
    }

    @Test
    void assignAgentToMission_invalidMission() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        assertThrows(IllegalArgumentException.class, () ->
                db.assignAgentToMission(999, 1));
    }

    @Test
    void testRemoveAgentFromMission() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);
        db.addMission("M", "B", "2020", 1, List.of(1));
        db.removeAgentFromMission(1, 1);
        assertEquals(0, db.getAgentsForMission(1).size());
    }

    @Test
    void removeAgentFromMission() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        db.addMission("Op", "brief", "date", 1, List.of(1));
        db.removeAgentFromMission(1, 1);
        assertTrue(db.getAgentsForMission(1).isEmpty());
    }

    @Test
    void testUpdateMission() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addFacility("Field", "F1");
        db.addAgent("A", "", "", "", 1);
        db.addAgent("B", "", "", "", 2);
        db.addMission("Old", "Brief", "2020", 1, List.of(1));
        assertTrue(db.updateMission(1, "New", "Updated", "2021", 2, List.of(2)));
        Mission updated = db.getMissionById(1);
        assertNotNull(updated);
        assertEquals("New", updated.getTitle());
        assertEquals(2, updated.getFacilityId());
        assertEquals(List.of(2), updated.getAgentIds());
    }

    // === Search ===

    @Test
    void testSearchMissionsMatch() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);
        db.addMission("Alpha", "Secret", "2020", 1, List.of(1));
        assertEquals(1, db.searchMissions("alpha").size());
    }

    @Test
    void testSearchMissionsMatchesRelatedAndDateFields() throws Exception {
        db.addFacility("Moon Base", "MB");
        db.addAgent("Luna", "1980", "", "orbit specialist", 1);
        db.addMission("Alpha", "Secret", "2020-01-01", 1, List.of(1));
        assertEquals(1, db.searchMissions("moon").size());
        assertEquals(1, db.searchMissions("luna").size());
        assertEquals(1, db.searchMissions("2020-01").size());
        assertEquals(1, db.searchMissions("orbit").size());
    }

    @Test
    void testSearchMissionsEmptyInput() {
        assertTrue(db.searchMissions("").isEmpty());
        assertTrue(db.searchMissions(null).isEmpty());
    }

    @Test
    void searchMissions_null() {
        assertTrue(db.searchMissions(null).isEmpty());
    }

    @Test
    void searchMissions_empty() {
        assertTrue(db.searchMissions("").isEmpty());
    }

    @Test
    void searchMissions_whitespace() {
        assertTrue(db.searchMissions("   ").isEmpty());
    }

    // === Format / Titles ===

    @Test
    void testFormatResults() {
        assertEquals("No matches found.", db.formatResults(new ArrayList<>()));
    }

    @Test
    void formatResults_empty() {
        assertEquals("No matches found.", db.formatResults(List.of()));
    }

    @Test
    void formatResults_withMatches() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        db.addMission("Alpha", "brief", "2024-01-01", 1, List.of(1));
        List<Mission> missions = db.getAllMissions();
        String result = db.formatResults(missions);
        assertTrue(result.contains("Alpha"));
        assertTrue(result.contains("2024-01-01"));
    }

    @Test
    void getAllTitles_empty() {
        assertTrue(db.getAllTitles().isEmpty());
    }

    @Test
    void testGetAllTitles() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("A", "", "", "", 1);
        db.addMission("M1", "B", "2020", 1, List.of(1));
        List<String> titles = db.getAllTitles();
        assertEquals(1, titles.size());
        assertTrue(titles.get(0).contains("M1"));
    }

    // === Messages ===

    @Test
    void testAddAgentMessageValid() throws Exception {
        db.addFacility("HQ", "HQ1");
        db.addAgent("Bond", "", "", "", 1);
        db.addAgentMessage(1, "Hello", "admin");
        assertEquals(1, db.getAgentMessagesForRecipientUsername("Bond").size());
    }

    @Test
    void testAddAgentMessageInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> db.addAgentMessage(1, "", "user"));
        assertThrows(IllegalArgumentException.class,
                () -> db.addAgentMessage(1, "msg", ""));
    }

    @Test
    void addAgentMessage_emptyBody() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        assertThrows(IllegalArgumentException.class, () ->
                db.addAgentMessage(1, "", "sender"));
    }

    @Test
    void addAgentMessage_nullBody() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        assertThrows(IllegalArgumentException.class, () ->
                db.addAgentMessage(1, null, "sender"));
    }

    @Test
    void addAgentMessage_emptySender() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        assertThrows(IllegalArgumentException.class, () ->
                db.addAgentMessage(1, "Hello", ""));
    }

    @Test
    void addAgentMessage_nullSender() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        assertThrows(IllegalArgumentException.class, () ->
                db.addAgentMessage(1, "Hello", null));
    }

    @Test
    void addAgentMessage_invalidAgent() {
        assertThrows(IllegalArgumentException.class, () ->
                db.addAgentMessage(999, "Hello", "sender"));
    }

    @Test
    void testGetMessagesEmptyUsername() throws Exception {
        assertTrue(db.getAgentMessagesForRecipientUsername("").isEmpty());
    }

    @Test
    void getAgentMessages_null() throws SQLException {
        assertTrue(db.getAgentMessagesForRecipientUsername(null).isEmpty());
    }

    @Test
    void getAgentMessages_empty() throws SQLException {
        assertTrue(db.getAgentMessagesForRecipientUsername("").isEmpty());
    }

    @Test
    void markMessageAsRead_updatesRecord() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        db.addAgentMessage(1, "Hello", "admin");
        List<Message> msgs = db.getAgentMessagesForRecipientUsername("Bond");
        assertFalse(msgs.get(0).isRead());
        db.markMessageAsRead(msgs.get(0).getId());
        Message updated = db.getMessageById(msgs.get(0).getId());
        assertNotNull(updated);
        assertTrue(updated.isRead());
    }

    @Test
    void getMessageById_notFound() throws SQLException {
        assertNull(db.getMessageById(9999));
    }

    // === getMissionsForAgent ===

    @Test
    void getMissionsForAgent_noMissions() throws SQLException {
        db.addFacility("HQ", "HQ");
        db.addAgent("UnassignedAgent", "1970", "", "", 1);
        assertTrue(db.getMissionsForAgent(1).isEmpty());
    }

    // === Countries ===

    @Test
    void addCountry_andGetAll() throws SQLException {
        db.addCountry("Germany");
        db.addCountry("France");
        List<Country> countries = db.getAllCountries();
        assertEquals(2, countries.size());
        assertTrue(countries.stream().anyMatch(c -> c.getName().equals("Germany")));
        assertTrue(countries.stream().anyMatch(c -> c.getName().equals("France")));
    }

    @Test
    void addCountry_duplicate_ignoredSilently() throws SQLException {
        db.addCountry("Spain");
        db.addCountry("Spain");
        List<Country> countries = db.getAllCountries();
        assertEquals(1, countries.size());
    }

    @Test
    void loadCountries_fileNotFound() {
        db.loadCountries("nonexistent.tsv");
    }

    @Test
    void loadFacilities_fileNotFound() {
        db.loadFacilities("nonexistent.tsv");
    }

    // === loadData ===

    @Test
    void loadData_fileNotFound() {
        db.loadData("nonexistent.tsv");
    }
}