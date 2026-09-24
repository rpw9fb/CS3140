package viewTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.TerminalView;
import models.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalViewTest
{
    private TerminalView view;

    @BeforeEach
    void setup()
    {
        view = new TerminalView();
    }

    // ===========
    // Main Menu
    // ===========

    @Test
    void mainMenuShowsAdminOption()
    {
        String result = view.mainMenu(true);
        assertTrue(result.contains("Review Audit Log"));
    }

    @Test
    void mainMenuHidesAdminOption()
    {
        String result = view.mainMenu(false);
        assertFalse(result.contains("Review Audit Log"));
    }

    @Test
    void mainMenuShowsAllAdminOptions() {
        String result = view.mainMenu(true);

        assertTrue(result.contains("Promote User to Admin"));
        assertTrue(result.contains("Review Audit Log"));
    }

    // ===========
    // Missions
    // ===========

    @Test
    void missionListEmpty()
    {
        assertEquals("No missions available.", view.missionList(List.of()));
    }

    @Test
    void missionListDisplaysTitles()
    {
        List<String> titles = List.of("1. Apollo", "2. Gemini");
        String result = view.missionList(titles);

        assertTrue(result.contains("Apollo"));
        assertTrue(result.contains("Gemini"));
    }

    @Test
    void missionDetailNull()
    {
        assertEquals("Invalid mission number.", view.missionDetail(null));
    }

    @Test
    void missionDetailValid()
    {
        assertEquals("Test Mission", view.missionDetail("Test Mission"));
    }

    @Test
    void missionSearchResultsEmpty()
    {
        assertEquals("No matches found.", view.missionSearchResults(List.of(), "apollo"));
    }

    @Test
    void missionSearchResultsDisplaysMatches()
    {
        Mission m = new Mission(1, "Apollo", "Moon", "1969", 1, List.of(1));
        String result = view.missionSearchResults(List.of(m), "apollo");

        assertTrue(result.contains("Apollo"));
        assertTrue(result.contains("1969"));
    }

    @Test
    void missionsMenuDisplaysOptions() {
        String result = view.missionsMenu();

        assertTrue(result.contains("Mission Briefs"));
        assertTrue(result.contains("L - List all Briefs"));
        assertTrue(result.contains("Choice:"));
    }

    @Test
    void missionListNull() {
        assertEquals("No missions available.", view.missionList(null));
    }

    @Test
    void missionAddedDisplaysUpdatedList() {
        List<String> titles = List.of("1. Apollo");

        String result = view.missionAdded(titles);

        assertTrue(result.contains("Mission added Successfully"));
        assertTrue(result.contains("Apollo"));
    }

    // ===========
    // Agents
    // ===========

    @Test
    void agentListEmpty()
    {
        assertEquals("No agents available.", view.agentList(List.of()));
    }

    @Test
    void agentListDisplaysAgents()
    {
        Agent a = new Agent(1, "Neil", "1930", "", "", 1);
        String result = view.agentList(List.of(a));

        assertTrue(result.contains("Neil"));
    }

    @Test
    void agentDetailNull()
    {
        assertEquals("Agent not found.", view.agentDetail(null));
    }

    @Test
    void agentDetailDisplaysFields()
    {
        Agent a = new Agent(1, "Neil", "1930", "", "Test", 1);
        String result = view.agentDetail(a);

        assertTrue(result.contains("Neil"));
        assertTrue(result.contains("1930"));
        assertTrue(result.contains("Test"));
    }

    @Test
    void agentsMenuDisplaysOptions() {
        String result = view.agentsMenu();

        assertTrue(result.contains("Agents"));
        assertTrue(result.contains("L - List all Agents"));
    }

    @Test
    void agentAddedDisplaysUpdatedList() {
        Agent a = new Agent(1, "Neil", "1930", "", "", 1);

        String result = view.agentAdded(List.of(a));

        assertTrue(result.contains("Agent added successfully"));
        assertTrue(result.contains("Neil"));
    }

    // ===========
    // Facilities
    // ===========

    @Test
    void facilityListEmpty()
    {
        assertEquals("No facilities available.", view.facilityList(List.of()));
    }

    @Test
    void facilityListDisplaysFacilities()
    {
        Facility f = new Facility(1, "NASA", "NS");
        String result = view.facilityList(List.of(f));

        assertTrue(result.contains("NASA"));
    }

    @Test
    void facilityDetailNull()
    {
        assertEquals("Facility not found.", view.facilityDetail(null));
    }

    @Test
    void facilityDetailDisplaysFields()
    {
        Facility f = new Facility(1, "NASA", "NS");
        String result = view.facilityDetail(f);

        assertTrue(result.contains("NASA"));
        assertTrue(result.contains("NS"));
    }

    @Test
    void facilitiesMenuDisplaysOptions() {
        String result = view.facilitiesMenu();

        assertTrue(result.contains("Facilities"));
        assertTrue(result.contains("E - Edit Facility"));
    }

    @Test
    void countryListDisplaysCountries() {
        Country c1 = new Country(1, "USA");
        Country c2 = new Country(2, "UK");

        String result = view.countryList(List.of(c1, c2));

        assertTrue(result.contains("USA"));
        assertTrue(result.contains("UK"));
        assertTrue(result.contains("1."));
        assertTrue(result.contains("2."));
    }

    @Test
    void facilityListNull() {
        assertEquals("No facilities available.", view.facilityList(null));
    }

    @Test
    void facilityDetailShowsNoCountryWhenNull() {
        Facility f = new Facility(1, "NASA", "NS");

        String result = view.facilityDetail(f);

        assertTrue(result.contains("No Country"));
    }

    @Test
    void facilityAddedDisplaysUpdatedList() {
        Facility f = new Facility(1, "NASA", "NS");

        String result = view.facilityAdded(List.of(f));

        assertTrue(result.contains("Facility added successfully"));
        assertTrue(result.contains("NASA"));
    }

    // ===========
    // Messages
    // ===========

    @Test
    void inboxEmpty()
    {
        assertEquals("No messages for your account.", view.agentInbox(List.of()));
    }

    @Test
    void inboxDisplaysMessages()
    {
        Message m = new Message(1, 1, "Hello", "admin");
        String result = view.agentInbox(List.of(m));

        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("admin"));
    }

    @Test
    void messagesMenuDisplaysOptions() {
        String result = view.messagesMenu();

        assertTrue(result.contains("Messages"));
        assertTrue(result.contains("Send message"));
    }

    @Test
    void inboxNull() {
        assertEquals("No messages for your account.", view.agentInbox(null));
    }

    @Test
    void inboxDisplaysMultipleMessages() {
        Message m1 = new Message(1, 1, "Hello", "admin");
        Message m2 = new Message(2, 1, "World", "user");

        String result = view.agentInbox(List.of(m1, m2));

        assertTrue(result.contains("#1"));
        assertTrue(result.contains("#2"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("World"));
    }

    // ===========
    // Audit Log
    // ===========

    @Test
    void auditLogEmpty()
    {
        assertEquals("\nAudit log is empty.", view.auditLog(List.of()));
    }

    @Test
    void auditLogDisplaysLines()
    {
        String result = view.auditLog(List.of("Line1", "Line2"));

        assertTrue(result.contains("Line1"));
        assertTrue(result.contains("Line2"));
    }

    @Test
    void auditLogNull() {
        assertEquals("\nAudit log is empty.", view.auditLog(null));
    }

    // ===========
    // Generic
    // ===========

    @Test
    void errorFormatsMessage()
    {
        assertEquals("Error: fail", view.error("fail"));
    }

    @Test
    void unknownOptionFormatsCorrectly()
    {
        assertEquals("Unknown option 'Z'.\n", view.unknownOption("Z"));
    }

    @Test
    void welcomeMessage()
    {
        assertEquals("Welcome to the Mission Brief System.", view.welcome());
    }

    @Test
    void goodbyeMessage()
    {
        assertEquals("\nExiting Mission Brief System.", view.goodbye());
    }
}