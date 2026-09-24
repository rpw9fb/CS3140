package controllerTests;

import com.sun.net.httpserver.HttpExchange;
import controller.ProjectControl;
import controller.SessionManager;
import controller.WebController;
import models.*;
import org.junit.jupiter.api.*;
import view.HTMLView;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WebControllerTest {

    @Test
    void rootContextResponse_notLoggedIn() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;

        when(view.loginPage(null)).thenReturn("login page");
        when(sm.checkSession(exchange, controller.sessions)).thenReturn(false);

        // Need to mock getResponseHeaders and getResponseBody for sendResponse
        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));
        when(exchange.getResponseBody()).thenReturn(new java.io.ByteArrayOutputStream());

        assertDoesNotThrow(() -> controller.handleRoot(exchange));
    }


    @Test
    void rootContextResponse_loggedInAdmin() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;

        when(view.mainMenu(true)).thenReturn("admin menu");
        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(sm.isSessionAdmin(exchange, controller.adminSessions)).thenReturn(true);

        String result = controller.rootContextResponse(exchange);
        assertEquals("admin menu", result);
    }


    @Test
    void rootContextResponse_loggedIn() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;

        when(view.mainMenu(false)).thenReturn("non-admin menu");
        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(sm.isSessionAdmin(exchange, controller.adminSessions)).thenReturn(false);

        String result = controller.rootContextResponse(exchange);
        assertEquals("non-admin menu", result);
    }


    @Test
    void briefsResponse_invalidSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(false);
        when(view.loginPage(null)).thenReturn("login page");

        String result = controller.briefsResponse(exchange);
        assertEquals("login page", result);
    }


    @Test
    void briefsResponse_briefsPath() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);
        List<String> titles = new ArrayList<String>();
        URI uri = mock(URI.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(pc.listMissionTitles()).thenReturn(titles);
        when(view.missionList(titles)).thenReturn("missions");
        when(exchange.getRequestURI()).thenReturn(uri);
        when(uri.getPath()).thenReturn("/briefs");

        String result = controller.briefsResponse(exchange);
        assertEquals(null, result);
    }


    @Test
    void briefsResponse_specificBriefPath() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);
        URI uri = mock(URI.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(exchange.getRequestURI()).thenReturn(uri);
        when(uri.getPath()).thenReturn("/briefs/1");

        Mission mission = new Mission(1, "mission info", "brief", "2026-01-01", 1, List.of(1));
        Facility facility = new Facility(1, "HQ", "H");
        List<Agent> agents = List.of(new Agent(1, "Bond", "", "", "", 1));
        try {
            when(pc.getMissionById(1)).thenReturn(mission);
            when(pc.getAgentsForMission(1)).thenReturn(agents);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        when(pc.retrieveFacility(1)).thenReturn(facility);
        when(view.missionDetail(mission, facility, agents)).thenReturn("html mission info");

        String result = controller.briefsResponse(exchange);
        assertEquals(null, result);
    }


    @Test
    void agentResponse_invalidSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(false);
        when(view.loginPage(null)).thenReturn("login page");

        String result = controller.agentsResponse(exchange);
        assertEquals("login page", result);
    }


    @Test
    void agentsResponse_agentsPath() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);
        List<Agent> agents = new ArrayList<Agent>();
        URI uri = mock(URI.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        try {
            when(pc.listAgents()).thenReturn(agents);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        when(view.agentList(agents)).thenReturn("agents");
        when(exchange.getRequestURI()).thenReturn(uri);
        when(uri.getPath()).thenReturn("/agents");

        String result = controller.agentsResponse(exchange);
        assertEquals(null, result);
    }


    @Test
    void agentsResponse_specificAgentPath() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);
        URI uri = mock(URI.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(exchange.getRequestURI()).thenReturn(uri);
        when(uri.getPath()).thenReturn("/agents/1");

        Agent agent = new Agent(-1, "", "", "", "", -1);
        when(pc.retrieveAgent(1)).thenReturn(agent);
        when(pc.retrieveFacility(-1)).thenReturn(new Facility(-1, "Unknown", "UNK"));
        try {
            when(pc.getMissionsForAgent(1)).thenReturn(List.of());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        when(view.agentDetail(eq(agent), any(), any())).thenReturn("html agent info");

        String result = controller.agentsResponse(exchange);
        assertEquals(null, result);
    }


    @Test
    void facilityResponse_invalidSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(false);
        when(view.loginPage(null)).thenReturn("login page");

        String result = controller.facilityResponse(exchange);
        assertEquals("login page", result);
    }


    @Test
    void facilityResponse_facilityPath() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);
        List<Facility> facilities = new ArrayList<Facility>();
        URI uri = mock(URI.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        try {
            when(pc.listFacilities()).thenReturn(facilities);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        when(view.facilityList(facilities)).thenReturn("facilities");
        when(exchange.getRequestURI()).thenReturn(uri);
        when(uri.getPath()).thenReturn("/facilities");

        String result = controller.facilityResponse(exchange);
        assertEquals(null, result);
    }


    @Test
    void facilityResponse_specificFacilityPath() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);
        URI uri = mock(URI.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(exchange.getRequestURI()).thenReturn(uri);
        when(uri.getPath()).thenReturn("/facilities/1");

        Facility facility = new Facility(-1, "", "");
        List<Integer> ids = List.of(1, 2);
        Agent a1 = new Agent(-1, "", "", "", "", -1);
        Agent a2 = new Agent(-1, "", "", "", "", -1);

        try {
            when(pc.retrieveFacility(1)).thenReturn(facility);
            when(pc.getAgentsAtFacility(1)).thenReturn(ids);
            when(pc.retrieveAgent(1)).thenReturn(a1);
            when(pc.retrieveAgent(2)).thenReturn(a2);
            when(pc.getMissionsAtFacility(1)).thenReturn(List.of());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        when(view.facilityDetail(eq(facility), any(), any())).thenReturn("facility detail");

        String result = controller.facilityResponse(exchange);
        assertEquals(null, result);
    }


    @Test
    void auditResponse_invalidSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(false);
        when(view.loginPage(null)).thenReturn("login page");

        String result = controller.auditResponse(exchange);
        assertEquals("login page", result);
    }


    @Test
    void auditResponse_isAdmin() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);
        List<String> auditLines = new ArrayList<String>();

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(sm.isSessionAdmin(exchange, controller.adminSessions)).thenReturn(true);
        when(pc.listAuditLogLines()).thenReturn(auditLines);
        when(view.auditLog(auditLines)).thenReturn("audit log");

        String result = controller.auditResponse(exchange);
        assertEquals("audit log", result);
    }


    @Test
    void searchResponse_invalidSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(false);
        when(view.loginPage(null)).thenReturn("login page");

        String result = controller.searchResponse(exchange);
        assertEquals("login page", result);
    }


    @Test
    void searchResponse_validSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(view.missionSearchPage()).thenReturn("mission search page");

        String result = controller.searchResponse(exchange);
        assertEquals("mission search page", result);
    }


    @Test
    void searchResultsResponse_invalidSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(false);
        when(view.loginPage(null)).thenReturn("login page");

        String result = controller.searchResultsResponse(exchange);
        assertEquals("login page", result);
    }


    @Test
    void searchResultsResponse_validSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);
        URI uri = mock(URI.class);
        List<Mission> missions = new ArrayList<>();
        String query = "mission";

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(exchange.getRequestURI()).thenReturn(uri);
        when(uri.getQuery()).thenReturn("q=" + query);
        when(pc.searchMissions(query)).thenReturn(missions);
        when(view.missionSearchResults(missions, query)).thenReturn("search results");

        String result = controller.searchResultsResponse(exchange);
        assertEquals("search results", result);
    }


    @Test
    void adminPromoteResponse_invalidSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        WebController controller = new WebController(password);

        controller.view = view;
        controller.sm = sm;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(false);
        when(sm.isSessionAdmin(exchange, controller.adminSessions)).thenReturn(false);
        when(view.loginPage(null)).thenReturn("login page");

        String result = controller.adminPromoteResponse(exchange);
        assertEquals("login page", result);
    }


    @Test
    void adminPromoteResponse_validSessionNotAdmin() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        WebController controller = new WebController(password);

        controller.view = view;
        controller.sm = sm;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(sm.isSessionAdmin(exchange, controller.adminSessions)).thenReturn(false);
        when(view.loginPage(null)).thenReturn("login page");

        String result = controller.adminPromoteResponse(exchange);
        assertEquals("login page", result);
    }


    @Test
    void logoutResponse_validSession() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        WebController controller = new WebController(password);

        com.sun.net.httpserver.Headers responseHeaders = mock(com.sun.net.httpserver.Headers.class);
        when(exchange.getRequestHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));
        when(exchange.getRequestHeaders().getFirst("Cookie")).thenReturn("SESSIONID=validSessionId");
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);

        controller.logoutResponse(exchange);
        verify(exchange).getResponseHeaders();
        verify(exchange).sendResponseHeaders(302, -1);
        assertEquals(0, controller.sessions.size());
    }


    @Test
    void adminUserResponse_invalidSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(false);
        when(view.loginPage(null)).thenReturn("login page");

        String result = controller.adminUserResponse(exchange);
        assertEquals("login page", result);
    }


    @Test
    void adminUserResponse_notAdmin() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(sm.isSessionAdmin(exchange, controller.adminSessions)).thenReturn(false);
        when(view.loginPage(null)).thenReturn("login page");

        String result = controller.adminUserResponse(exchange);
        assertEquals("login page", result);
    }


    @Test
    void adminUserResponse_validAdmin() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;

        List<java.util.Map.Entry<String, String>> users = new ArrayList<>();

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(sm.isSessionAdmin(exchange, controller.adminSessions)).thenReturn(true);
        when(password.getAllUsers()).thenReturn(users);
        when(view.userManagement(users, null)).thenReturn("users");

        String result = controller.adminUserResponse(exchange);
        assertEquals("users", result);
    }


    @Test
    void loginResponse_getRequest() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);

        WebController controller = new WebController(password);
        controller.view = view;

        when(exchange.getRequestMethod()).thenReturn("GET");
        when(view.loginPage(null)).thenReturn("login");

        String result = controller.loginResponse(exchange);
        assertEquals("login", result);
    }


    @Test
    void loginResponse_postSuccess() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        Password password = mock(Password.class);

        WebController controller = new WebController(password);

        when(exchange.getRequestMethod()).thenReturn("POST");

        String body = "username=user&password=pass";
        when(exchange.getRequestBody()).thenReturn(
                new java.io.ByteArrayInputStream(body.getBytes())
        );

        when(password.webAuth("user", "pass")).thenReturn(true);

        com.sun.net.httpserver.Headers headers = mock(com.sun.net.httpserver.Headers.class);
        when(exchange.getResponseHeaders()).thenReturn(headers);

        String result = controller.loginResponse(exchange);

        verify(exchange).sendResponseHeaders(302, -1);
        assertEquals("", result);
        assertEquals(1, controller.sessions.size());
    }


    @Test
    void loginResponse_postFailure() {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);

        WebController controller = new WebController(password);
        controller.view = view;

        when(exchange.getRequestMethod()).thenReturn("POST");

        String body = "username=user&password=wrong";
        when(exchange.getRequestBody()).thenReturn(
                new java.io.ByteArrayInputStream(body.getBytes())
        );

        when(password.webAuth("user", "wrong")).thenReturn(false);
        when(view.error(any())).thenReturn("error");

        String result = controller.loginResponse(exchange);
        assertEquals("error", result);
    }


    @Test
    void adminPromoteResponse_getRequest() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);

        WebController controller = new WebController(password);
        controller.sm = sm;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(sm.isSessionAdmin(exchange, controller.adminSessions)).thenReturn(true);
        when(exchange.getRequestMethod()).thenReturn("GET");

        com.sun.net.httpserver.Headers headers = mock(com.sun.net.httpserver.Headers.class);
        when(exchange.getResponseHeaders()).thenReturn(headers);

        String result = controller.adminPromoteResponse(exchange);

        verify(exchange).sendResponseHeaders(302, -1);
        assertEquals("", result);
    }


    @Test
    void adminPromoteResponse_postSuccess() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(sm.isSessionAdmin(exchange, controller.adminSessions)).thenReturn(true);
        when(exchange.getRequestMethod()).thenReturn("POST");

        String body = "username=testUser";
        when(exchange.getRequestBody()).thenReturn(
                new java.io.ByteArrayInputStream(body.getBytes())
        );

        when(password.promoteToAdmin("testUser")).thenReturn(true);
        when(password.getAllUsers()).thenReturn(new ArrayList<>());
        when(view.userManagement(any(), contains("promoted"))).thenReturn("success");

        com.sun.net.httpserver.Headers reqHeaders = mock(com.sun.net.httpserver.Headers.class);
        when(exchange.getRequestHeaders()).thenReturn(reqHeaders);
        when(reqHeaders.getFirst("Cookie")).thenReturn("SESSIONID=abc");
        controller.sessions.put("abc", "adminUser");

        String result = controller.adminPromoteResponse(exchange);
        assertEquals("success", result);
    }


    @Test
    void sendResponse_writesResponse() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);

        java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();

        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));
        when(exchange.getResponseBody()).thenReturn(os);

        WebController controller = new WebController(mock(Password.class));

        controller.sendResponse(exchange, "hello");

        verify(exchange).sendResponseHeaders(200, 5);
        assertEquals("hello", os.toString());
    }


    @Test
    void checkIfAdmin_true() {
        Password password = mock(Password.class);
        WebController controller = new WebController(password);

        when(password.isAdminUser("user")).thenReturn(true);

        boolean result = controller.checkIfAdmin("user");
        assertEquals(true, result);
    }


    @Test
    void checkIfAdmin_exceptionReturnsFalse() {
        Password password = mock(Password.class);
        WebController controller = new WebController(password);

        when(password.isAdminUser("user")).thenThrow(new RuntimeException());

        boolean result = controller.checkIfAdmin("user");
        assertEquals(false, result);
    }


    @Test
    void messagesResponse_valid() throws SQLException {
        HttpExchange exchange = mock(HttpExchange.class);
        HTMLView view = mock(HTMLView.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);
        com.sun.net.httpserver.Headers reqHeaders = mock(com.sun.net.httpserver.Headers.class);

        WebController controller = new WebController(password);
        controller.view = view;
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(exchange.getRequestHeaders()).thenReturn(reqHeaders);
        when(reqHeaders.getFirst("Cookie")).thenReturn("SESSIONID=abc");
        controller.sessions.put("abc", "user1");

        List<Message> inbox = new ArrayList<>();
        when(pc.getAgentMessagesForCurrentUser()).thenReturn(inbox);
        when(view.agentInbox(inbox, "user1")).thenReturn("inbox html");

        String result = controller.messagesResponse(exchange);
        assertEquals("inbox html", result);
    }

    @Test
    void sendMessageResponse_post() throws IOException, SQLException {
        HttpExchange exchange = mock(HttpExchange.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);

        WebController controller = new WebController(password);
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(exchange.getRequestMethod()).thenReturn("POST");
        String body = "recipientId=1&body=Hello";
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(body.getBytes()));
        
        com.sun.net.httpserver.Headers reqHeaders = mock(com.sun.net.httpserver.Headers.class);
        when(exchange.getRequestHeaders()).thenReturn(reqHeaders);
        when(reqHeaders.getFirst("Cookie")).thenReturn("SESSIONID=abc");
        controller.sessions.put("abc", "senderUser");

        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));

        String result = controller.sendMessageResponse(exchange);
        verify(pc).addAgentMessage(1, "Hello", "senderUser");
        assertEquals("", result);
    }

    @Test
    void briefsResponse_createPost() throws IOException, SQLException {
        HttpExchange exchange = mock(HttpExchange.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);
        URI uri = mock(URI.class);

        WebController controller = new WebController(password);
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(sm.isSessionAdmin(exchange, controller.adminSessions)).thenReturn(true);
        when(exchange.getRequestURI()).thenReturn(uri);
        when(uri.getPath()).thenReturn("/briefs/create");
        when(exchange.getRequestMethod()).thenReturn("POST");
        
        String body = "title=NewOp&date=2026&brief=text&facilityId=1&agentIds=1&agentIds=2";
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(body.getBytes()));
        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));

        String result = controller.briefsResponse(exchange);
        verify(pc).addMission(eq("NewOp"), anyString(), anyString(), eq(1), anyList());
        assertEquals("", result);
    }

    @Test
    void agentsResponse_createPost() throws IOException, SQLException {
        HttpExchange exchange = mock(HttpExchange.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);
        URI uri = mock(URI.class);

        WebController controller = new WebController(password);
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(sm.isSessionAdmin(exchange, controller.adminSessions)).thenReturn(true);
        when(exchange.getRequestURI()).thenReturn(uri);
        when(uri.getPath()).thenReturn("/agents/create");
        when(exchange.getRequestMethod()).thenReturn("POST");

        String body = "name=Bond&dob=1920&dod=&notes=none&facilityId=1";
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(body.getBytes()));
        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));

        String result = controller.agentsResponse(exchange);
        verify(pc).addAgent("Bond", "1920", "", "none", 1);
        assertEquals("", result);
    }

    @Test
    void facilityResponse_createPost() throws IOException, SQLException {
        HttpExchange exchange = mock(HttpExchange.class);
        Password password = mock(Password.class);
        SessionManager sm = mock(SessionManager.class);
        ProjectControl pc = mock(ProjectControl.class);
        URI uri = mock(URI.class);

        WebController controller = new WebController(password);
        controller.sm = sm;
        controller.projectControl = pc;

        when(sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(sm.isSessionAdmin(exchange, controller.adminSessions)).thenReturn(true);
        when(exchange.getRequestURI()).thenReturn(uri);
        when(uri.getPath()).thenReturn("/facilities/create");
        when(exchange.getRequestMethod()).thenReturn("POST");

        String body = "name=HQ&abbreviation=H&countryId=1";
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(body.getBytes()));
        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));

        String result = controller.facilityResponse(exchange);
        verify(pc).addFacility("HQ", "H", 1);
        assertEquals("", result);
    }

    @Test
    void viewMessageResponse_valid() throws SQLException {
        HttpExchange exchange = mock(HttpExchange.class);
        Password password = mock(Password.class);
        WebController controller = new WebController(password);
        controller.sm = mock(SessionManager.class);
        controller.view = mock(HTMLView.class);
        controller.projectControl = mock(ProjectControl.class);

        when(controller.sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(exchange.getRequestURI()).thenReturn(URI.create("http://localhost/view?id=1"));
        
        Message msg = new Message(1, 1, "hello", "admin", false);
        when(controller.projectControl.getMessageById(1)).thenReturn(msg);
        when(controller.projectControl.listAgents()).thenReturn(List.of(new Agent(1, "user1", "", "", "", 1)));
        
        com.sun.net.httpserver.Headers reqHeaders = mock(com.sun.net.httpserver.Headers.class);
        when(exchange.getRequestHeaders()).thenReturn(reqHeaders);
        when(reqHeaders.getFirst("Cookie")).thenReturn("SESSIONID=abc");
        controller.sessions.put("abc", "user1");

        when(controller.view.messageDetail(any())).thenReturn("msg html");

        String result = controller.viewMessageResponse(exchange);
        assertEquals("msg html", result);
    }

    @Test
    void sendMessageResponse_get() throws SQLException {
        HttpExchange exchange = mock(HttpExchange.class);
        Password password = mock(Password.class);
        WebController controller = new WebController(password);
        controller.sm = mock(SessionManager.class);
        controller.view = mock(HTMLView.class);
        controller.projectControl = mock(ProjectControl.class);

        when(controller.sm.checkSession(exchange, controller.sessions)).thenReturn(true);
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(controller.projectControl.listAgents()).thenReturn(List.of());
        when(controller.view.sendMessagePage(any())).thenReturn("send html");

        String result = controller.sendMessageResponse(exchange);
        assertEquals("send html", result);
    }

    @Test
    void handleAllMethods() throws IOException, SQLException {
        HttpExchange exchange = mock(HttpExchange.class);
        Password password = mock(Password.class);
        WebController controller = new WebController(password);
        controller.sm = mock(SessionManager.class);
        controller.view = mock(HTMLView.class);
        controller.projectControl = mock(ProjectControl.class);

        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));
        when(exchange.getResponseBody()).thenReturn(new java.io.ByteArrayOutputStream());
        when(exchange.getRequestURI()).thenReturn(URI.create("http://localhost/"));
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));

        controller.handleLogin(exchange);
        controller.handleBriefs(exchange);
        controller.handleAgents(exchange);
        controller.handleFacilities(exchange);
        controller.handleAudit(exchange);
        controller.handleSearch(exchange);
        controller.handleSearchResults(exchange);
        controller.handleAdminUsers(exchange);
        controller.handleAdminPromote(exchange);
        controller.handleMessages(exchange);
        controller.handleViewMessage(exchange);
        controller.handleSendMessage(exchange);
    }

    @Test
    void run_startsServer() {
        WebController controller = new WebController(mock(Password.class));
        assertDoesNotThrow(controller::run);
    }
}