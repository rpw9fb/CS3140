package controllerTests;

import com.sun.net.httpserver.HttpExchange;
import controller.DatabaseManager;
import controller.ProjectControl;
import controller.SessionManager;
import controller.WebController;
import models.Agent;
import models.Facility;
import models.Mission;
import models.Password;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class controlIntegrationTest {

    private WebController controller;
    private ProjectControl projectControl;
    private DatabaseManager db;
    private Password password;
    private SessionManager sm;
    private HttpExchange exchange;

    @BeforeEach
    void setUp() throws SQLException {
        db = new DatabaseManager(":memory:", new java.util.Random(), true);
        projectControl = new ProjectControl(db);
        password = mock(Password.class);
        sm = mock(SessionManager.class);
        
        controller = new WebController(password);
        controller.projectControl = projectControl;
        controller.sm = sm;
        
        exchange = mock(HttpExchange.class);
        when(sm.checkSession(any(), any())).thenReturn(true);
        when(sm.isSessionAdmin(any(), any())).thenReturn(true);
        
        // Add a default facility for testing
        db.addFacility("HQ", "HQ", null);
        // Add a default agent
        db.addAgent("Bond", "1920", "", "", 1);
    }

    @Test
    void testCreateAgentIntegration() throws IOException, SQLException {
        when(exchange.getRequestURI()).thenReturn(URI.create("/agents/create"));
        when(exchange.getRequestMethod()).thenReturn("POST");
        String body = "name=NewAgent&dob=2000-01-01&dod=&notes=SomeNotes&facilityId=1";
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));

        controller.agentsResponse(exchange);

        List<Agent> agents = db.getAllAgents();
        assertTrue(agents.stream().anyMatch(a -> a.getName().equals("NewAgent")));
        verify(exchange).sendResponseHeaders(302, -1);
    }

    @Test
    void testEditAgentIntegration() throws IOException, SQLException {
        int agentId = db.getAllAgents().get(0).getId();
        when(exchange.getRequestURI()).thenReturn(URI.create("/agents/edit/" + agentId));
        when(exchange.getRequestMethod()).thenReturn("POST");
        String body = "name=BondUpdated&dob=1920&dod=2026&notes=Retired&facilityId=1";
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));

        controller.agentsResponse(exchange);

        Agent updated = db.getAllAgents().stream().filter(a -> a.getId() == agentId).findFirst().get();
        assertEquals("BondUpdated", updated.getName());
        assertEquals("2026", updated.getDateOfDeath());
        verify(exchange).sendResponseHeaders(302, -1);
    }

    @Test
    void testCreateFacilityIntegration() throws IOException, SQLException {
        when(exchange.getRequestURI()).thenReturn(URI.create("/facilities/create"));
        when(exchange.getRequestMethod()).thenReturn("POST");
        String body = "name=NewFacility&abbreviation=NF&countryId=";
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));

        controller.facilityResponse(exchange);

        List<Facility> facilities = db.getAllFacilities();
        assertTrue(facilities.stream().anyMatch(f -> f.getName().equals("NewFacility")));
    }

    @Test
    void testEditFacilityIntegration() throws IOException, SQLException {
        int facilityId = db.getAllFacilities().get(0).getID();
        when(exchange.getRequestURI()).thenReturn(URI.create("/facilities/edit/" + facilityId));
        when(exchange.getRequestMethod()).thenReturn("POST");
        String body = "name=HQUpdated&abbreviation=HQU&countryId=";
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));

        controller.facilityResponse(exchange);

        Facility updated = db.getAllFacilities().stream().filter(f -> f.getID() == facilityId).findFirst().get();
        assertEquals("HQUpdated", updated.getName());
        assertEquals("HQU", updated.getAbbreviation());
    }

    @Test
    void testCreateMissionIntegration() throws IOException, SQLException {
        int agentId = db.getAllAgents().get(0).getId();
        when(exchange.getRequestURI()).thenReturn(URI.create("/briefs/create"));
        when(exchange.getRequestMethod()).thenReturn("POST");
        String body = "title=NewMission&date=2026&brief=TestBrief&facilityId=1&agentIds=" + agentId;
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));

        controller.briefsResponse(exchange);

        List<Mission> missions = db.getAllMissions();
        assertTrue(missions.stream().anyMatch(m -> m.getTitle().equals("NewMission")));
    }

    @Test
    void testEditMissionIntegration() throws IOException, SQLException {
        int agentId = db.getAllAgents().get(0).getId();
        db.addMission("OldMission", "OldBrief", "2025", 1, List.of(agentId));
        int missionId = db.getAllMissions().get(0).getId();
        
        when(exchange.getRequestURI()).thenReturn(URI.create("/briefs/edit/" + missionId));
        when(exchange.getRequestMethod()).thenReturn("POST");
        String body = "title=MissionUpdated&date=2027&brief=NewBrief&facilityId=1&agentIds=" + agentId;
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        when(exchange.getResponseHeaders()).thenReturn(mock(com.sun.net.httpserver.Headers.class));

        controller.briefsResponse(exchange);

        Mission updated = db.getMissionById(missionId);
        assertEquals("MissionUpdated", updated.getTitle());
        assertEquals("2027", updated.getDate());
    }

    @Test
    void testNonAdminCannotCreate() throws IOException, SQLException {
        when(sm.isSessionAdmin(any(), any())).thenReturn(false);
        when(exchange.getRequestURI()).thenReturn(URI.create("/agents/create"));
        
        String result = controller.agentsResponse(exchange);
        assertTrue(result.contains("Admin permissions required"));
    }
}
