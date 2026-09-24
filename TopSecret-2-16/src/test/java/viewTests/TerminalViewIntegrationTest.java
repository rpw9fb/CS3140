package viewTests;

import models.*;
import org.junit.jupiter.api.Test;
import view.TerminalView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TerminalViewIntegrationTest {

    private final TerminalView view = new TerminalView();

    @Test
    void mainMenu_admin_vs_user() {
        String admin = view.mainMenu(true);
        String user = view.mainMenu(false);

        assertTrue(admin.contains("R - Review Audit Log"));
        assertFalse(user.contains("R - Review Audit Log"));
    }

    @Test
    void missionList_empty_and_valid() {
        assertEquals("No missions available.", view.missionList(List.of()));

        String out = view.missionList(List.of("1. Alpha", "2. Bravo"));
        assertTrue(out.contains("Alpha"));
        assertTrue(out.contains("Bravo"));
    }

    @Test
    void missionDetail_null_and_valid() {
        assertEquals("Invalid mission number.", view.missionDetail(null));

        assertTrue(view.missionDetail("Secret Mission").contains("Secret Mission"));
    }

    @Test
    void missionSearchResults() {
        assertEquals("No matches found.",
                view.missionSearchResults(List.of(), "x"));

        Mission m = new Mission(1, "Alpha", "Secret", "2020", 1, List.of(1));
        String out = view.missionSearchResults(List.of(m), "alpha");

        assertTrue(out.contains("Alpha"));
    }

    @Test
    void agentList_and_detail() {
        assertEquals("No agents available.", view.agentList(List.of()));

        Agent a = new Agent(1, "Bond", "1990", "", "notes", 1);
        assertTrue(view.agentDetail(a).contains("Bond"));
    }

    @Test
    void facilityList_and_detail() {
        Facility f = new Facility(1, "CIA", "CIA");

        assertTrue(view.facilityList(List.of(f)).contains("CIA"));
        assertTrue(view.facilityDetail(f).contains("Abbreviation"));
    }

    @Test
    void inbox_messages() {
        Message msg = new Message(1, 1, "Hello", "admin");

        String out = view.agentInbox(List.of(msg));

        assertTrue(out.contains("Hello"));
        assertTrue(out.contains("admin"));
    }

    @Test
    void audit_log_empty() {
        assertEquals("\nAudit log is empty.", view.auditLog(List.of()));
    }

    @Test
    void error_and_unknown() {
        assertEquals("Error: fail", view.error("fail"));
        assertTrue(view.unknownOption("X").contains("Unknown option"));
    }

    @Test
    void mainMenu_admin_shows_all_options() {
        String admin = view.mainMenu(true);

        assertTrue(admin.contains("Promote User to Admin"));
        assertTrue(admin.contains("Review Audit Log"));
        assertTrue(admin.contains("Choice:"));
    }

    @Test
    void missionsMenu_full_output() {
        String out = view.missionsMenu();

        assertTrue(out.contains("Mission Briefs"));
        assertTrue(out.contains("L - List all Briefs"));
        assertTrue(out.contains("S - Search Briefs"));
        assertTrue(out.contains("# - Enter Number"));
    }

    @Test
    void missionList_null_and_mixed_content() {
        assertEquals("No missions available.", view.missionList(null));

        String out = view.missionList(List.of("1. Alpha", "2. Bravo"));
        assertTrue(out.contains("Alpha"));
        assertTrue(out.contains("Bravo"));
    }

    @Test
    void missionAdded_flow() {
        String out = view.missionAdded(List.of("1. Apollo"));

        assertTrue(out.contains("Mission added Successfully"));
        assertTrue(out.contains("Apollo"));
    }

    @Test
    void agentsMenu_full_output() {
        String out = view.agentsMenu();

        assertTrue(out.contains("Agents"));
        assertTrue(out.contains("L - List all Agents"));
        assertTrue(out.contains("# - Enter ID"));
    }

    @Test
    void agentAdded_flow() {
        Agent a = new Agent(1, "Bond", "1990", "", "notes", 1);

        String out = view.agentAdded(List.of(a));

        assertTrue(out.contains("Agent added successfully"));
        assertTrue(out.contains("Bond"));
    }

    @Test
    void facilitiesMenu_full_output() {
        String out = view.facilitiesMenu();

        assertTrue(out.contains("Facilities"));
        assertTrue(out.contains("E - Edit Facility"));
        assertTrue(out.contains("# - Enter ID"));
    }

    @Test
    void countryList_output() {
        Country c1 = new Country(1, "USA");
        Country c2 = new Country(2, "UK");

        String out = view.countryList(List.of(c1, c2));

        assertTrue(out.contains("USA"));
        assertTrue(out.contains("UK"));
        assertTrue(out.contains("1. USA"));
        assertTrue(out.contains("2. UK"));
    }

    @Test
    void facilityAdded_flow() {
        Facility f = new Facility(1, "CIA", "CIA");

        String out = view.facilityAdded(List.of(f));

        assertTrue(out.contains("Facility added successfully"));
        assertTrue(out.contains("CIA"));
    }

    @Test
    void messagesMenu_full_output() {
        String out = view.messagesMenu();

        assertTrue(out.contains("Messages"));
        assertTrue(out.contains("Send message to agent"));
        assertTrue(out.contains("View my inbox"));
    }

    @Test
    void inbox_null_and_multiple_messages() {
        assertEquals("No messages for your account.", view.agentInbox(null));

        Message m1 = new Message(1, 1, "Hello", "admin");
        Message m2 = new Message(2, 1, "World", "user");

        String out = view.agentInbox(List.of(m1, m2));

        assertTrue(out.contains("#1"));
        assertTrue(out.contains("#2"));
        assertTrue(out.contains("Hello"));
        assertTrue(out.contains("World"));
    }

    @Test
    void audit_log_null_and_multiple() {
        assertEquals("\nAudit log is empty.", view.auditLog(null));

        String out = view.auditLog(List.of("Line1", "Line2"));

        assertTrue(out.contains("Line1"));
        assertTrue(out.contains("Line2"));
    }

    @Test
    void error_and_unknownOption_full_validation() {
        assertEquals("Error: fail", view.error("fail"));

        String out = view.unknownOption("Z");
        assertTrue(out.contains("Unknown option"));
        assertTrue(out.contains("Z"));
    }

    @Test
    void welcome_and_goodbye_messages() {
        assertEquals("Welcome to the Mission Brief System.", view.welcome());
        assertEquals("\nExiting Mission Brief System.", view.goodbye());
    }
}