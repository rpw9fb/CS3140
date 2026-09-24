package viewTests;

import models.Agent;
import models.Facility;
import models.Message;
import models.Mission;
import org.junit.jupiter.api.Test;
import view.HTMLView;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HTMLViewIntegrationTest {

    private final HTMLView view = new HTMLView();

    @Test
    void loginPage_with_and_without_error() {
        String html = view.loginPage("Invalid");

        assertTrue(html.contains("<form"));
        assertTrue(html.contains("Invalid"));
    }

    @Test
    void mainMenu_admin_vs_user() {
        String admin = view.mainMenu(true);
        String user = view.mainMenu(false);

        assertTrue(admin.contains("Manage Users"));
        assertFalse(user.contains("Manage Users"));
    }

    @Test
    void missionList_links() {
        String html = view.missionList(List.of("1. Alpha", "2. Bravo"));

        assertTrue(html.contains("/briefs/1"));
        assertTrue(html.contains("Alpha"));
    }

    @Test
    void missionDetail_null_and_valid() {
        assertTrue(view.missionDetail(null).contains("not found"));

        String html = view.missionDetail("Mission Text");
        assertTrue(html.contains("Mission Text"));
    }

    @Test
    void missionSearchResults() {
        Mission m = new Mission(1, "Alpha", "Secret", "2020", 1, List.of(1));

        String html = view.missionSearchResults(List.of(m), "alpha");

        assertTrue(html.contains("Alpha"));
        assertTrue(html.contains("2020"));
    }

    @Test
    void agent_and_facility_pages() {
        Agent a = new Agent(1, "Bond", "1990", "", "notes", 1);
        Facility f = new Facility(1, "CIA", "CIA");
        Mission m = new Mission(7, "Alpha", "brief", "2020", 1, List.of(1));

        assertTrue(view.agentDetail(a, f, List.of(m)).contains("/briefs/7"));
        assertTrue(view.facilityDetail(f, List.of(a), List.of(m)).contains("Missions at this Facility"));
    }

    @Test
    void search_empty() {
        String html = view.missionSearchResults(List.of(), "x");

        assertTrue(html.contains("No matches found"));
    }

    @Test
    void userManagement_promote_button() {
        List<Map.Entry<String, String>> users = List.of(
                Map.entry("bob", "user"),
                Map.entry("admin", "admin")
        );

        String html = view.userManagement(users, "OK");

        assertTrue(html.contains("Promote to Admin"));
        assertTrue(html.contains("admin"));
    }

    @Test
    void error_page() {
        String html = view.error("Something broke");

        assertTrue(html.contains("Something broke"));
    }

    @Test
    void missionDetail_full_flow_with_admin() {
        Mission m = new Mission(5, "Delta", "Deep cover", "2025", 1, List.of(1));
        Facility f = new Facility(1, "HQ", "H");
        Agent a = new Agent(1, "Bond", "", "", "", 1);

        String html = view.missionDetail(m, f, List.of(a), true);

        assertTrue(html.contains("Delta"));
        assertTrue(html.contains("[Edit Mission]"));
        assertTrue(html.contains("/facilities/1"));
        assertTrue(html.contains("/agents/1"));
    }

    @Test
    void missionForm_create_and_edit_flow() {
        String createHtml = view.missionForm(null, List.of(), List.of(), false);
        assertTrue(createHtml.contains("/briefs/create"));

        Mission m = new Mission(2, "Echo", "brief", "2024", 1, List.of(1));
        Facility f = new Facility(1, "HQ", "H");
        Agent a = new Agent(1, "Bond", "", "", "", 1);

        String editHtml = view.missionForm(m, List.of(f), List.of(a), true);

        assertTrue(editHtml.contains("/briefs/edit/2"));
        assertTrue(editHtml.contains("Echo"));
    }

    @Test
    void agentForm_create_and_edit_flow() {
        String createHtml = view.agentForm(null, List.of(), false);
        assertTrue(createHtml.contains("/agents/create"));

        Agent a = new Agent(3, "Bond", "1990", "", "note", 1);
        Facility f = new Facility(1, "HQ", "H");

        String editHtml = view.agentForm(a, List.of(f), true);

        assertTrue(editHtml.contains("/agents/edit/3"));
        assertTrue(editHtml.contains("Bond"));
    }

    @Test
    void facilityForm_create_and_edit_flow() {
        String createHtml = view.facilityForm(null, List.of(), false);
        assertTrue(createHtml.contains("/facilities/create"));

        Facility f = new Facility(4, "NSA", "N");

        String editHtml = view.facilityForm(f, List.of(), true);

        assertTrue(editHtml.contains("/facilities/edit/4"));
        assertTrue(editHtml.contains("NSA"));
    }

    @Test
    void messaging_full_flow() {
        Message unread = new Message(1, 99, "Unread message body here", "alice", false);
        Message read = new Message(2, 99, "Read message body here", "bob", true);

        String inbox = view.agentInbox(List.of(unread, read), "user");

        assertTrue(inbox.contains("Messaging Center"));
        assertTrue(inbox.contains("alice"));
        assertTrue(inbox.contains("bob"));
        assertTrue(inbox.contains("NEW"));
        assertTrue(inbox.contains("Read"));

        String detail = view.messageDetail(unread);

        assertTrue(detail.contains("alice"));
        assertTrue(detail.contains("Unread message body here"));
    }

    @Test
    void sendMessagePage_flow() {
        Agent a1 = new Agent(1, "Bond", "", "", "", 1);
        Agent a2 = new Agent(2, "Ethan", "", "", "", 1);

        String html = view.sendMessagePage(List.of(a1, a2));

        assertTrue(html.contains("Send a New Message"));
        assertTrue(html.contains("Bond"));
        assertTrue(html.contains("Ethan"));
    }

    @Test
    void facilityDetail_with_no_related_data() {
        Facility f = new Facility(10, "EmptyBase", "EB");

        String html = view.facilityDetail(f, List.of(), List.of());

        assertTrue(html.contains("EmptyBase"));
        assertFalse(html.contains("Agents at this Facility"));
        assertFalse(html.contains("Missions at this Facility"));
    }

    @Test
    void agentDetail_admin_view() {
        Agent a = new Agent(8, "Jane", "1985", "", "notes", 1);

        String html = view.agentDetail(a, null, List.of(), true);

        assertTrue(html.contains("[Edit Agent]"));
        assertTrue(html.contains("Jane"));
    }
}