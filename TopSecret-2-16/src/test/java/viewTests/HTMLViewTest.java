package viewTests;

import models.Agent;
import models.Facility;
import models.Mission;
import org.junit.jupiter.api.Test;
import view.HTMLView;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HTMLViewTest {

    private final HTMLView view = new HTMLView();

    @Test
    void testLoginPageWithError() {
        String html = view.loginPage("Invalid");

        assertTrue(html.contains("Invalid"));
        assertTrue(html.contains("<form"));
    }

    @Test
    void testLoginPageNoError() {
        String html = view.loginPage(null);

        assertFalse(html.contains("color:red"));
    }

    @Test
    void testMainMenuAdmin() {
        String html = view.mainMenu(true);

        assertTrue(html.contains("Manage Users"));
        assertTrue(html.contains("Review Audit Log"));
    }

    @Test
    void testMainMenuNonAdmin() {
        String html = view.mainMenu(false);

        assertFalse(html.contains("Manage Users"));
    }

    @Test
    void testMissionListEmpty() {
        String html = view.missionList(new ArrayList<>());

        assertTrue(html.contains("No missions available"));
    }

    @Test
    void testMissionListNull() {
        String html = view.missionList(null);

        assertTrue(html.contains("No missions available"));
    }

    @Test
    void testMissionListWithData() {
        List<String> titles = List.of("Mission A", "Mission B");

        String html = view.missionList(titles);

        assertTrue(html.contains("Mission A"));
        assertTrue(html.contains("/briefs/1"));
        assertTrue(html.contains("/briefs/2"));
    }

    @Test
    void testMissionDetailNull() {
        String html = view.missionDetail(null);

        assertTrue(html.contains("Mission not found"));
    }

    @Test
    void testMissionDetailEmpty() {
        String html = view.missionDetail("");

        assertTrue(html.contains("Mission not found"));
    }

    @Test
    void testMissionDetailValid() {
        String html = view.missionDetail("Secret Mission");

        assertTrue(html.contains("Secret Mission"));
        assertTrue(html.contains("<pre>"));
    }

    @Test
    void testMissionSearchPage() {
        String html = view.missionSearchPage();

        assertTrue(html.contains("Search Missions"));
        assertTrue(html.contains("form"));
    }

    @Test
    void testSearchResultsEmpty() {
        String html = view.missionSearchResults(new ArrayList<>(), "test");

        assertTrue(html.contains("No matches found"));
    }

    @Test
    void testSearchResultsNull() {
        String html = view.missionSearchResults(null, "test");

        assertTrue(html.contains("No matches found"));
    }

    @Test
    void testSearchResultsWithData() {
        Mission m = new Mission(1, "Title", "Brief", "2020", 1, List.of());

        String html = view.missionSearchResults(List.of(m), "Title");

        assertTrue(html.contains("Title"));
        assertTrue(html.contains("2020"));
        assertTrue(html.contains("/briefs/1"));
    }

    @Test
    void testAgentListEmpty() {
        String html = view.agentList(new ArrayList<>());

        assertTrue(html.contains("No agents available"));
    }

    @Test
    void testAgentListNull() {
        String html = view.agentList(null);

        assertTrue(html.contains("No agents available"));
    }

    @Test
    void testAgentListWithData() {
        Agent a = new Agent(1, "Bond", "", "", "", 1);

        String html = view.agentList(List.of(a));

        assertTrue(html.contains("Bond"));
        assertTrue(html.contains("/agents/1"));
    }

    @Test
    void testAgentDetailNull() {
        String html = view.agentDetail(null);

        assertTrue(html.contains("Agent not found"));
    }

    @Test
    void testAgentDetailValid() {
        Agent a = new Agent(1, "Bond", "1990", "", "note", 1);

        String html = view.agentDetail(a);

        assertTrue(html.contains("Bond"));
        assertTrue(html.contains("1990"));
        assertTrue(html.contains("Facility ID"));
    }

    @Test
    void testAgentDetailWithRelationships() {
        Agent a = new Agent(1, "Bond", "1990", "", "note", 1);
        Facility f = new Facility(1, "HQ", "H");
        Mission m = new Mission(2, "Nightfall", "brief", "2025-01-01", 1, List.of(1));

        String html = view.agentDetail(a, f, List.of(m));

        assertTrue(html.contains("/facilities/1"));
        assertTrue(html.contains("/briefs/2"));
        assertTrue(html.contains("Assigned Missions"));
    }

    @Test
    void testFacilityListEmpty() {
        String html = view.facilityList(new ArrayList<>());

        assertTrue(html.contains("No facilities available"));
    }

    @Test
    void testFacilityListWithData() {
        Facility f = new Facility(1, "HQ", "H");

        String html = view.facilityList(List.of(f));

        assertTrue(html.contains("HQ"));
        assertTrue(html.contains("(H)"));
    }

    @Test
    void testFacilityDetailNull() {
        String html = view.facilityDetail(null, null);

        assertTrue(html.contains("Facility not found"));
    }

    @Test
    void testFacilityDetailNoAgents() {
        Facility f = new Facility(1, "HQ", "H");

        String html = view.facilityDetail(f, new ArrayList<>());

        assertFalse(html.contains("Agents at this Facility"));
    }

    @Test
    void testFacilityDetailWithAgents() {
        Facility f = new Facility(1, "HQ", "H");
        Agent a = new Agent(1, "Bond", "", "", "", 1);
        Mission m = new Mission(3, "Skyhook", "brief", "2026-01-01", 1, List.of(1));

        String html = view.facilityDetail(f, List.of(a), List.of(m));

        assertTrue(html.contains("Agents at this Facility"));
        assertTrue(html.contains("Bond"));
        assertTrue(html.contains("/agents/1"));
        assertTrue(html.contains("/briefs/3"));
        assertTrue(html.contains("Missions at this Facility"));
    }

    @Test
    void testAuditLogEmpty() {
        String html = view.auditLog(new ArrayList<>());

        assertTrue(html.contains("No audit entries"));
    }

    @Test
    void testAuditLogWithData() {
        String html = view.auditLog(List.of("entry1", "entry2"));

        assertTrue(html.contains("entry1"));
        assertTrue(html.contains("entry2"));
    }

    @Test
    void testUserManagementEmpty() {
        String html = view.userManagement(new ArrayList<>(), null);

        assertTrue(html.contains("No users found"));
    }

    @Test
    void testUserManagementWithMessage() {
        String html = view.userManagement(new ArrayList<>(), "Success");

        assertTrue(html.contains("Success"));
    }

    @Test
    void testUserManagementNormalUser() {
        Map<String, String> map = new HashMap<>();
        map.put("user", "user");

        String html = view.userManagement(map.entrySet().stream().toList(), null);

        assertTrue(html.contains("Promote to Admin"));
    }

    @Test
    void testUserManagementAdminUser() {
        Map<String, String> map = new HashMap<>();
        map.put("admin", "admin");

        String html = view.userManagement(map.entrySet().stream().toList(), null);

        assertTrue(html.contains("Admin"));
    }

    @Test
    void testErrorPage() {
        String html = view.error("Something went wrong");

        assertTrue(html.contains("Something went wrong"));
        assertTrue(html.contains("Error"));
    }

    @Test
    void testMissionListAdminView() {
        Mission m = new Mission(1, "Op", "brief", "2024", 1, List.of());

        String html = view.missionList(List.of(m), true);

        assertTrue(html.contains("Create New Mission"));
        assertTrue(html.contains("[Edit]"));
    }

    @Test
    void testMissionListNonAdminView() {
        Mission m = new Mission(1, "Op", "brief", "2024", 1, List.of());

        String html = view.missionList(List.of(m), false);

        assertFalse(html.contains("[Edit]"));
    }

    @Test
    void testMissionDetailObjectNull() {
        String html = view.missionDetail((Mission) null, null, null, false);

        assertTrue(html.contains("Mission not found"));
    }

    @Test
    void testMissionDetailObjectWithAdmin() {
        Mission m = new Mission(1, "Op", "brief", "2024", 1, List.of());
        Facility f = new Facility(1, "HQ", "H");
        Agent a = new Agent(1, "Bond", "", "", "", 1);

        String html = view.missionDetail(m, f, List.of(a), true);

        assertTrue(html.contains("[Edit Mission]"));
        assertTrue(html.contains("/facilities/1"));
        assertTrue(html.contains("/agents/1"));
    }

    @Test
    void testMissionFormCreate() {
        String html = view.missionForm(null, new ArrayList<>(), new ArrayList<>(), false);

        assertTrue(html.contains("Create Mission"));
        assertTrue(html.contains("action='/briefs/create'"));
    }

    @Test
    void testMissionFormEdit() {
        Mission m = new Mission(1, "Op", "brief", "2024", 1, List.of(1));
        Facility f = new Facility(1, "HQ", "H");
        Agent a = new Agent(1, "Bond", "", "", "", 1);

        String html = view.missionForm(m, List.of(f), List.of(a), true);

        assertTrue(html.contains("Edit Mission"));
        assertTrue(html.contains("value='Op'"));
        assertTrue(html.contains("selected"));
    }

    @Test
    void testAgentFormCreate() {
        String html = view.agentForm(null, new ArrayList<>(), false);

        assertTrue(html.contains("Create Agent"));
        assertTrue(html.contains("/agents/create"));
    }

    @Test
    void testAgentFormEdit() {
        Agent a = new Agent(1, "Bond", "1990", "", "note", 1);
        Facility f = new Facility(1, "HQ", "H");

        String html = view.agentForm(a, List.of(f), true);

        assertTrue(html.contains("Edit Agent"));
        assertTrue(html.contains("Bond"));
        assertTrue(html.contains("selected"));
    }

    @Test
    void testFacilityFormCreate() {
        String html = view.facilityForm(null, new ArrayList<>(), false);

        assertTrue(html.contains("Create Facility"));
        assertTrue(html.contains("/facilities/create"));
    }

    @Test
    void testFacilityFormEdit() {
        Facility f = new Facility(1, "HQ", "H");

        String html = view.facilityForm(f, new ArrayList<>(), true);

        assertTrue(html.contains("Edit Facility"));
        assertTrue(html.contains("HQ"));
    }

    @Test
    void testAgentInboxEmpty() {
        String html = view.agentInbox(new ArrayList<>(), "user");

        assertTrue(html.contains("No messages found"));
    }

    @Test
    void testAgentInboxWithMessages() {
        models.Message msg = new models.Message(1, 99, "This is a test message body", "sender", false);

        String html = view.agentInbox(List.of(msg), "user");

        assertTrue(html.contains("NEW"));
        assertTrue(html.contains("sender"));
        assertTrue(html.contains("Open"));
    }

    @Test
    void testAgentInboxReadMessage() {
        models.Message msg = new models.Message(1, 99, "Read message", "sender", true);

        String html = view.agentInbox(List.of(msg), "user");

        assertTrue(html.contains("Read"));
        assertFalse(html.contains("NEW"));
    }

    @Test
    void testSendMessagePage() {
        Agent a = new Agent(1, "Bond", "", "", "", 1);

        String html = view.sendMessagePage(List.of(a));

        assertTrue(html.contains("Send a New Message"));
        assertTrue(html.contains("Bond"));
    }

    @Test
    void testMessageDetailNull() {
        String html = view.messageDetail(null);

        assertTrue(html.contains("Message not found"));
    }

    @Test
    void testMessageDetailValid() {
        models.Message msg = new models.Message(1, 99, "Hello world", "sender", true);

        String html = view.messageDetail(msg);

        assertTrue(html.contains("sender"));
        assertTrue(html.contains("Hello world"));
    }

    @Test
    void missionDetail_threeArg_callsFourArgVersion() {
        Mission mission = mock(Mission.class);
        Facility facility = mock(Facility.class);
        List<Agent> agents = new ArrayList<Agent>();
        agents.add(mock(Agent.class));
        String result = view.missionDetail(mission, facility, agents);
        assertEquals(result.charAt(0), '<');
    }
}