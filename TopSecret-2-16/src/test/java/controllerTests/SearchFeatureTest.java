package controllerTests;

import controller.DatabaseManager;
import controller.ProjectControl;
import controller.SearchFeature;
import models.Mission;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class SearchFeatureTest
{
    private DatabaseManager db;
    private ProjectControl projectControl;

    @BeforeEach
    void setup() throws Exception
    {
        db = new DatabaseManager(":memory:", new Random(), true);
        db.addFacility("NASA", "NASA");
        db.addAgent("Neil Armstrong", "1930", "", "", 1);

        List<Integer> agents = List.of(1);
        db.addMission("Apollo Mission", "Moon landing mission", "1969", 1, agents);
        projectControl = new ProjectControl(db);
    }

    @AfterEach
    void tearDown() throws Exception
    {
        db.closeConnection();
    }

    // ===========
    // Unit Tests
    // ===========

    @Test
    void searchFindsMatchingMission()
    {
        List<Mission> results = db.searchMissions("mission");
        assertFalse(results.isEmpty());
    }

    @Test
    void searchIsCaseInsensitive()
    {
        List<Mission> lower = db.searchMissions("apollo");
        List<Mission> upper = db.searchMissions("APOLLO");

        assertEquals(lower.size(), upper.size());
    }

    @Test
    void searchReturnsEmptyWhenNoMatch()
    {
        List<Mission> results = db.searchMissions("lalalala");
        assertTrue(results.isEmpty());
    }

    @Test
    void searchHandlesEmptyInput()
    {
        List<Mission> results = db.searchMissions("");
        assertTrue(results.isEmpty());
    }

    // ==================
    // Integration Tests
    // ==================

    @Test
    void projectControlSearchIntegration()
    {
        List<Mission> results = projectControl.searchMissions("apollo");
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void interfaceSearchIntegration()
    {
        SearchFeature feature = db;
        List<Mission> results = feature.searchMissions("mission");

        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
}