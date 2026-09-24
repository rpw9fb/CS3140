package viewTests;

import models.Agent;
import models.Facility;
import models.Mission;
import view.View;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for View interface default methods.
 */
class ViewDefaultsTest {

    // Anonymous implementation that only implements the abstract methods
    private final View view = new View() {
        @Override
        public String mainMenu(boolean isAdmin) {
            return "main";
        }

        @Override
        public String missionList(List<String> titles) {
            return "list";
        }

        @Override
        public String missionSearchResults(List<Mission> matches, String phrase) {
            return "search";
        }

        @Override
        public String agentList(List<Agent> agents) {
            return "agents";
        }

        @Override
        public String agentDetail(Agent agent) {
            return "agent";
        }

        @Override
        public String facilityList(List<Facility> facilities) {
            return "facilities";
        }

        @Override
        public String auditLog(List<String> lines) {
            return "audit";
        }

        @Override
        public String error(String message) {
            return "error";
        }
    };

    @Test
    void missionsMenu_default() {
        assertEquals("", view.missionsMenu());
    }

    @Test
    void missionDetail_default() {
        assertEquals("", view.missionDetail("text"));
    }

    @Test
    void missionAdded_default() {
        assertEquals("", view.missionAdded(List.of()));
    }

    @Test
    void agentsMenu_default() {
        assertEquals("", view.agentsMenu());
    }

    @Test
    void agentAdded_default() {
        assertEquals("", view.agentAdded(List.of()));
    }

    @Test
    void facilitiesMenu_default() {
        assertEquals("", view.facilitiesMenu());
    }

    @Test
    void facilityDetail_default() {
        assertEquals("", view.facilityDetail(null));
    }

    @Test
    void facilityAdded_default() {
        assertEquals("", view.facilityAdded(List.of()));
    }
}
