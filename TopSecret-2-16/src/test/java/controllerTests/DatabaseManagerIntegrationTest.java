package controllerTests;

import static org.junit.jupiter.api.Assertions.*;

import controller.DatabaseManager;
import models.Agent;
import models.Country;
import models.Facility;
import models.Message;
import models.Mission;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.ArrayList;

class DatabaseManagerIntegrationTest {

    private DatabaseManager db;

    @BeforeEach
    void setup() throws Exception {
        db = new DatabaseManager(":memory:", new java.util.Random(), true);
    }

    @AfterEach
    void tearDown() throws Exception {
        db.closeConnection();
    }

    // === Facilities ===

    @Test
    void testAddAndGetFacilities() throws Exception {
        db.addFacility("CIA", "CIA");
        db.addFacility("NSA", "NSA");
        List<Facility> facilities = db.getAllFacilities();
        assertEquals(2, facilities.size());
        assertEquals("CIA", facilities.get(0).getName());
    }

    @Test
    void testFacilityExists() throws Exception {
        db.addFacility("CIA", "CIA");
        assertTrue(db.facilityExists(1));
        assertFalse(db.facilityExists(999));
    }

    @Test
    void testFacilityDeleteBlockedByAgent() throws Exception {
        db.addFacility("CIA", "CIA");
        db.addAgent("Agent Smith", "1970", "", "", 1);
        assertFalse(db.facilityDelete(1));
    }

    @Test
    void testFacilityWithCountry() throws Exception {
        db.addCountry("France");
        List<Country> countries = db.getAllCountries();
        assertEquals(1, countries.size());
        db.addFacility("DGSE", "DGS", countries.get(0).getId());
        List<Facility> facilities = db.getAllFacilities();
        assertEquals("France", facilities.get(0).getCountryName());
    }

    // === Agents ===

    @Test
    void testAddAndGetAgents() throws Exception {
        db.addFacility("CIA", "CIA");
        db.addAgent("Agent Smith", "1970", "", "", 1);
        List<Agent> agents = db.getAllAgents();
        assertEquals(1, agents.size());
        assertEquals("Agent Smith", agents.get(0).getName());
    }

    @Test
    void testAgentExists() throws Exception {
        db.addFacility("CIA", "CIA");
        db.addAgent("Agent Smith", "1970", "", "", 1);
        assertTrue(db.agentExists(1));
        assertFalse(db.agentExists(999));
    }

    @Test
    void testAgentToFacility() throws Exception {
        db.addFacility("CIA", "CIA");
        db.addAgent("Agent Smith", "1970", "", "", 1);
        assertTrue(db.agentToFacility(1, 1));
        assertFalse(db.agentToFacility(1, 999));
    }

    @Test
    void testAgentDeleteBlockedByMission() throws Exception {
        db.addFacility("CIA", "CIA");
        db.addAgent("Agent Smith", "1970", "", "", 1);
        List<Integer> agents = List.of(1);
        db.addMission("Mission X", "classified", "2024-01-01", 1, agents);
        assertFalse(db.agentDelete(1));
    }

    // === Missions ===

    @Test
    void testAddMissionAndRetrieve() throws Exception {
        db.addFacility("CIA", "CIA");
        db.addAgent("Agent Smith", "1970", "", "", 1);
        List<Integer> agents = new ArrayList<>();
        agents.add(1);
        db.addMission("Operation Alpha", "Top secret mission", "2024-01-01", 1, agents);
        String mission = db.getMissionByNumber(1);
        assertNotNull(mission);
        assertTrue(mission.contains("Operation Alpha"));
    }

    @Test
    void testGetAllTitles() throws Exception {
        db.addFacility("CIA", "CIA");
        db.addAgent("Agent Smith", "1970", "", "", 1);
        List<Integer> agents = List.of(1);
        db.addMission("Op A", "brief A", "2024-01-01", 1, agents);
        db.addMission("Op B", "brief B", "2024-01-02", 1, agents);
        List<String> titles = db.getAllTitles();
        assertEquals(2, titles.size());
        assertEquals("1. Op A", titles.get(0));
        assertEquals("2. Op B", titles.get(1));
    }

    @Test
    void testSearchMissions() throws Exception {
        db.addFacility("CIA", "CIA");
        db.addAgent("Agent Smith", "1970", "", "", 1);
        List<Integer> agents = List.of(1);
        db.addMission("Alpha Mission", "secret test mission", "2024-01-01", 1, agents);
        List<Mission> results = db.searchMissions("secret");
        assertEquals(1, results.size());
        assertTrue(results.get(0).getBrief().contains("secret"));
    }

    // === Audit Log ===

    @Test
    void testAuditLogOnMissionRead() throws Exception {
        db.addFacility("CIA", "CIA");
        db.addAgent("Agent Smith", "1970", "", "", 1);
        List<Integer> agents = List.of(1);
        db.addMission("Mission X", "classified", "2024-01-01", 1, agents);
        db.getMissionByNumber(1, "testUser");
        List<String> logs = db.getAuditLog().readAllLines();
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).contains("READ"));
        assertTrue(logs.get(0).contains("testUser"));
    }

    // === Messages Integration ===

    @Test
    void addAndRetrieveMessage_fullRoundTrip() throws Exception {
        db.addFacility("HQ", "HQ");
        db.addAgent("Spy", "1980", "", "", 1);
        db.addAgentMessage(1, "Secret orders", "commander");
        List<Message> msgs = db.getAgentMessagesForRecipientUsername("Spy");
        assertEquals(1, msgs.size());
        assertEquals("Secret orders", msgs.get(0).getBody());
        assertEquals("commander", msgs.get(0).getSenderUsername());
        assertFalse(msgs.get(0).isRead());

        db.markMessageAsRead(msgs.get(0).getId());
        Message read = db.getMessageById(msgs.get(0).getId());
        assertNotNull(read);
        assertTrue(read.isRead());
    }

    @Test
    void getAgentMessagesForRecipientUsername_caseInsensitive() throws Exception {
        db.addFacility("HQ", "HQ");
        db.addAgent("Bond", "1970", "", "", 1);
        db.addAgentMessage(1, "Hello", "admin");
        assertEquals(1, db.getAgentMessagesForRecipientUsername("bond").size());
        assertEquals(1, db.getAgentMessagesForRecipientUsername("BOND").size());
    }

    // === Countries ===

    @Test
    void addMultipleCountries_getAllCountries() throws Exception {
        db.addCountry("Italy");
        db.addCountry("Japan");
        db.addCountry("Brazil");
        List<Country> countries = db.getAllCountries();
        assertEquals(3, countries.size());
    }

    // === Full facility-with-country flow ===

    @Test
    void facilityWithCountry_fullFlow() throws Exception {
        db.addCountry("Germany");
        List<Country> countries = db.getAllCountries();
        db.addFacility("BND", "BND", countries.get(0).getId());
        db.addAgent("Hans", "1975", "", "", 1);
        db.addMission("Op Rhine", "Brief", "2026-01-01", 1, List.of(1));

        Mission m = db.getMissionById(1);
        assertNotNull(m);
        assertEquals("Op Rhine", m.getTitle());

        List<Facility> facs = db.getAllFacilities();
        assertEquals("Germany", facs.get(0).getCountryName());
    }
}