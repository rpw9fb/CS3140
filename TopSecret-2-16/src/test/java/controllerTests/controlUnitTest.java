package controllerTests;

import controller.DatabaseManager;
import controller.FileHandler;
import controller.ProjectControl;
import models.Agent;
import models.Facility;
import models.Mission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class controlUnitTest {

    private DatabaseManager dbMock;
    private FileHandler fhMock;
    private ProjectControl control;

    @BeforeEach
    void setUp() {
        dbMock = mock(DatabaseManager.class);
        fhMock = mock(FileHandler.class);
        control = new ProjectControl(dbMock);
    }

    @Test
    void testAddAgent() throws SQLException {
        control.addAgent("Bond", "1920-11-11", "", "Notes", 1);
        verify(dbMock).addAgent("Bond", "1920-11-11", "", "Notes", 1);
    }

    @Test
    void testUpdateAgent() throws SQLException {
        when(dbMock.updateAgent(anyInt(), anyString(), anyString(), anyString(), anyString(), anyInt())).thenReturn(true);
        boolean result = control.updateAgent(1, "Bond", "1920-11-11", "", "New Notes", 1);
        assertTrue(result);
        verify(dbMock).updateAgent(1, "Bond", "1920-11-11", "", "New Notes", 1);
    }

    @Test
    void testAddFacility() throws SQLException {
        control.addFacility("HQ", "MI6", 1);
        verify(dbMock).addFacility("HQ", "MI6", 1);
    }

    @Test
    void testUpdateFacility() throws SQLException {
        when(dbMock.updateFacility(anyInt(), anyString(), anyString(), anyInt())).thenReturn(true);
        boolean result = control.updateFacility(1, "HQ New", "MI6N", 2);
        assertTrue(result);
        verify(dbMock).updateFacility(1, "HQ New", "MI6N", 2);
    }

    @Test
    void testAddMission() throws SQLException {
        List<Integer> agents = List.of(1, 2);
        control.addMission("Skyfall", "Brief", "2012", 1, agents);
        verify(dbMock).addMission("Skyfall", "Brief", "2012", 1, agents);
    }

    @Test
    void testUpdateMission() throws SQLException {
        List<Integer> agents = List.of(1);
        when(dbMock.updateMission(anyInt(), anyString(), anyString(), anyString(), anyInt(), anyList())).thenReturn(true);
        boolean result = control.updateMission(1, "Skyfall 2", "New Brief", "2013", 2, agents);
        assertTrue(result);
        verify(dbMock).updateMission(1, "Skyfall 2", "New Brief", "2013", 2, agents);
    }

    @Test
    void testRetrieveAgent() throws SQLException {
        Agent a = new Agent(1, "Bond", "DOB", "DOD", "Notes", 1);
        when(dbMock.getAllAgents()).thenReturn(List.of(a));
        Agent result = control.retrieveAgent(1);
        assertEquals("Bond", result.getName());
    }

    @Test
    void testRetrieveFacility() throws SQLException {
        Facility f = new Facility(1, "HQ", "MI6", 1, "UK");
        when(dbMock.getAllFacilities()).thenReturn(List.of(f));
        Facility result = control.retrieveFacility(1);
        assertEquals("HQ", result.getName());
    }

    @Test
    void testGetMissionById() throws SQLException {
        Mission m = new Mission(1, "Title", "Brief", "Date", 1, List.of(1));
        when(dbMock.getMissionById(1)).thenReturn(m);
        Mission result = control.getMissionById(1);
        assertEquals("Title", result.getTitle());
    }
}
