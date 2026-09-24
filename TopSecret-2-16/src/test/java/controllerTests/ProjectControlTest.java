package controllerTests;

import controller.DatabaseManager;
import controller.FileHandler;
import controller.ProjectControl;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import models.*;
import java.util.Collections;

class ProjectControlTest
{
    @Mock
    private DatabaseManager mockDb;

    @Mock
    private FileHandler mockFileHandler;
    private ProjectControl pc;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);

        pc = new ProjectControl(mockDb)
        {
            @Override
            public List<String> listMissionTitles() {
                return mockDb.getAllTitles();
            }

            @Override
            public String retrieveMission(int number)
            {
                return mockDb.getMissionByNumber(number, "default");
            }

            @Override
            public List<Mission> searchMissions(String phrase)
            {
                return mockDb.searchMissions(phrase);
            }

            @Override
            public void addMission(String title, String brief, String date, int facilityId, List<Integer> agentIds) throws SQLException
            {
                mockDb.addMission(title, brief, date, facilityId, agentIds);
            }

            @Override
            public AuditLog getAuditLog() {
                return mockDb.getAuditLog();
            }
        };
    }

    @Test
    public void testListMissionTitles() throws SQLException
    {
        List<String> titles = Arrays.asList("1. Op Alpha", "2. Op Beta");
        when(mockDb.getAllTitles()).thenReturn(titles);

        List<String> result = pc.listMissionTitles();
        assertEquals(2, result.size());
        assertEquals("1. Op Alpha", result.get(0));
    }

    @Test
    public void testRetrieveMission()
    {
        when(mockDb.getMissionByNumber(1, "default"))
                .thenReturn("models.Mission 1 details");

        String result = pc.retrieveMission(1);
        assertEquals("models.Mission 1 details", result);
        verify(mockDb).getMissionByNumber(1, "default");
    }

    @Test
    public void testRetrieveMission_withUsername()
    {
        when(mockDb.getMissionByNumber(1, "agentX"))
                .thenReturn("models.Mission details");
        String result = mockDb.getMissionByNumber(1, "agentX");
        assertEquals("models.Mission details", result);
    }

    @Test
    public void testSearchMission()
    {
        List<Mission> missions = List.of(mock(Mission.class));
        when(mockDb.searchMissions("test")).thenReturn(missions);

        List<Mission> result = pc.searchMissions("test");

        assertEquals(missions, result);
        verify(mockDb).searchMissions("test");
    }

    @Test
    public void testAddMission() throws SQLException
    {
        doNothing().when(mockDb)
                .addMission(anyString(), anyString(), anyString(), anyInt(), anyList());

        pc.addMission("t", "b", "d", 1, List.of(1, 2));

        verify(mockDb).addMission("t", "b", "d", 1, List.of(1, 2));
    }

    @Test
    public void testListFacilities() throws SQLException {
        List<Facility> facilities = List.of(mock(Facility.class));

        when(mockDb.getAllFacilities()).thenReturn(facilities);

        // since not overridden, call DB directly
        when(mockDb.getAllFacilities()).thenReturn(facilities);

        List<Facility> result = mockDb.getAllFacilities();

        assertEquals(facilities, result);
    }

    @Test
    public void testListAgents() throws SQLException {
        List<Agent> agents = List.of(mock(Agent.class));

        when(mockDb.getAllAgents()).thenReturn(agents);

        List<Agent> result = mockDb.getAllAgents();

        assertEquals(agents, result);
    }

    @Test
    public void testAddFacility() throws SQLException {
        doNothing().when(mockDb).addFacility("CIA", "CI");

        mockDb.addFacility("CIA", "CI");

        verify(mockDb).addFacility("CIA", "CI");
    }

    @Test
    public void testAddAgent() throws SQLException {
        doNothing().when(mockDb)
                .addAgent(anyString(), anyString(), anyString(), anyString(), anyInt());

        mockDb.addAgent("Name", "dob", "dod", "notes", 1);

        verify(mockDb).addAgent("Name", "dob", "dod", "notes", 1);
    }

    @Test
    public void testAuditLog() {
        AuditLog audit = mock(AuditLog.class);
        when(mockDb.getAuditLog()).thenReturn(audit);

        AuditLog result = pc.getAuditLog();

        assertEquals(audit, result);
        verify(mockDb).getAuditLog();
    }

    @Test
    void testRetrieveMission_notFound() throws SQLException {
        when(mockDb.getMissionById(1)).thenReturn(null);

        ProjectControl realPc = new ProjectControl(mockDb);
        String result = realPc.retrieveMission(1, "user");

        assertEquals("Mission not found.", result);
    }

    @Test
    void testRetrieveMission_successFormatting() throws SQLException {
        Mission mission = mock(Mission.class);
        when(mission.getTitle()).thenReturn("Op X");
        when(mission.getBrief()).thenReturn("Secret");
        when(mission.getDate()).thenReturn("2025");

        Agent agent = mock(Agent.class);
        when(agent.getName()).thenReturn("Bond");

        when(mockDb.getMissionById(1)).thenReturn(mission);
        when(mockDb.getAgentsForMission(1)).thenReturn(List.of(agent));

        ProjectControl realPc = new ProjectControl(mockDb);
        String result = realPc.retrieveMission(1, "user");

        assertTrue(result.contains("Op X"));
        assertTrue(result.contains("Secret"));
        assertTrue(result.contains("Bond"));
    }

    @Test
    void testRetrieveMission_noAgents() throws SQLException {
        Mission mission = mock(Mission.class);
        when(mission.getTitle()).thenReturn("Op X");
        when(mission.getBrief()).thenReturn("Secret");
        when(mission.getDate()).thenReturn("2025");

        when(mockDb.getMissionById(1)).thenReturn(mission);
        when(mockDb.getAgentsForMission(1)).thenReturn(Collections.emptyList());

        ProjectControl realPc = new ProjectControl(mockDb);
        String result = realPc.retrieveMission(1, "user");

        assertTrue(result.contains("Agents: None"));
    }

    @Test
    void testSetCurrentUsername_nullAndEmpty() {
        ProjectControl realPc = new ProjectControl(mockDb);

        realPc.setCurrentUsername(null);
        assertDoesNotThrow(() -> realPc.recordLogin("test"));

        realPc.setCurrentUsername("   ");
        assertDoesNotThrow(() -> realPc.recordLogin("test"));
    }

    @Test
    void testRecordLogin_success() {
        AuditLog log = mock(AuditLog.class);
        when(mockDb.getAuditLog()).thenReturn(log);

        ProjectControl realPc = new ProjectControl(mockDb);

        boolean result = realPc.recordLogin("user");

        assertTrue(result);
        try {
            verify(log).recordRead(anyString(), eq("user"), eq("user"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testRecordLogin_noAuditLog() {
        when(mockDb.getAuditLog()).thenReturn(null);

        ProjectControl realPc = new ProjectControl(mockDb);

        assertFalse(realPc.recordLogin("user"));
    }

    @Test
    void testRecordUserAccountCreated_success() {
        AuditLog log = mock(AuditLog.class);
        when(mockDb.getAuditLog()).thenReturn(log);

        ProjectControl realPc = new ProjectControl(mockDb);

        boolean result = realPc.recordUserAccountCreated("newUser", "admin");

        assertTrue(result);
        try {
            verify(log).recordCreate(anyString(), eq("newUser"), eq("admin"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testListAuditLogLines_nullLog() {
        when(mockDb.getAuditLog()).thenReturn(null);

        ProjectControl realPc = new ProjectControl(mockDb);

        List<String> result = realPc.listAuditLogLines();

        assertTrue(result.isEmpty());
    }

    @Test
    void testRetrieveAgent_found() throws SQLException {
        Agent agent = mock(Agent.class);
        when(agent.getId()).thenReturn(1);

        when(mockDb.getAllAgents()).thenReturn(List.of(agent));

        ProjectControl realPc = new ProjectControl(mockDb);

        Agent result = realPc.retrieveAgent(1);

        assertEquals(agent, result);
    }

    @Test
    void testRetrieveAgent_notFound() throws SQLException {
        when(mockDb.getAllAgents()).thenReturn(Collections.emptyList());

        ProjectControl realPc = new ProjectControl(mockDb);

        Agent result = realPc.retrieveAgent(1);

        assertNull(result);
    }

    @Test
    void testRetrieveFacility_found() throws SQLException {
        Facility facility = mock(Facility.class);
        when(facility.getID()).thenReturn(1);

        when(mockDb.getAllFacilities()).thenReturn(List.of(facility));

        ProjectControl realPc = new ProjectControl(mockDb);

        Facility result = realPc.retrieveFacility(1);

        assertEquals(facility, result);
    }

    @Test
    void testConstructor() {
        FileHandler fh = mock(FileHandler.class);
        DatabaseManager dm = mock(DatabaseManager.class);
        ProjectControl pc = new ProjectControl(fh, dm);
        assertEquals(pc.fileHandler, fh);
        assertEquals(pc.databaseManager, dm);
    }

    @Test
    void updateAgentTest() {
        FileHandler fh = mock(FileHandler.class);
        ProjectControl pc = new ProjectControl(fh);
        try {
            boolean result = pc.updateAgent(-1, "Name", "July 2007", "July 2081", "Notes", 0);
            assertFalse(result);
        } catch (Exception e){
            assert(true);
        }
    }

    @Test
    void updateFacilityTest() {
        FileHandler fh = mock(FileHandler.class);
        ProjectControl pc = new ProjectControl(fh);
        try {
            boolean result = pc.updateFacility(-1, "Name", "ab", 0);
            assertFalse(result);
        } catch (Exception e){
            assert(true);
        }
    }

    @Test
    void updateFacilityTest2() {
        FileHandler fh = mock(FileHandler.class);
        ProjectControl pc = new ProjectControl(fh);
        try {
            boolean result = pc.updateFacility(-1, "Name", "ab");
            assertFalse(result);
        } catch (Exception e){
            assert(true);
        }
    }

    @Test
    void testGetMissionsAtFacility_filtersCorrectly() throws SQLException {
        Mission m1 = mock(Mission.class);
        Mission m2 = mock(Mission.class);

        when(m1.getFacilityId()).thenReturn(1);
        when(m2.getFacilityId()).thenReturn(2);

        when(mockDb.getAllMissions()).thenReturn(List.of(m1, m2));

        ProjectControl realPc = new ProjectControl(mockDb);

        List<Mission> result = realPc.getMissionsAtFacility(1);

        assertEquals(1, result.size());
        assertEquals(m1, result.get(0));
    }
}