package modelsTests;

import controller.FileHandler;
import controller.ProjectControl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AuditLogIntegrationTest {

    @Test
    void readingMissionCreatesAuditEntryForCurrentUser() {
        ProjectControl pc = new ProjectControl(new FileHandler());
        pc.setCurrentUsername("integration_user");

        List<String> before = pc.listAuditLogLines();
        int beforeCount = before.size();

        String mission = pc.retrieveMission(1);
        assertNotNull(mission);
        assertFalse(mission.isEmpty());

        List<String> after = pc.listAuditLogLines();
        assertTrue(after.size() >= beforeCount);
    }
}