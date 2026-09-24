package controllerTests;

import controller.FileHandler;
import controller.ProjectControl;
import controller.UserInterface;
import models.Agent;
import models.Facility;
import models.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class UserInterfaceIntegrationTest {

    private ProjectControl sharedPC;
    private ByteArrayOutputStream outContent;
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setup() {
        sharedPC = new ProjectControl(new FileHandler(), "ui_it_" + System.nanoTime() + ".db");
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void teardown() {
        System.setOut(originalOut);
    }

    private String out() {
        return outContent.toString();
    }

    private String runUI(String input, boolean isAdmin) {
        UserInterface ui = new UserInterface(sharedPC, new Scanner(input), isAdmin);
        ui.run();
        return out();
    }

    // ===========================
    // Facility integration tests
    // ===========================

    @Test
    void addFacilityThroughUIAndVerifyInList() {
        String output = runUI("F\nN\nUITestFacility\nUTF\n\nL\nM\nX\n", false);
        assertTrue(output.contains("Facility added successfully"));
        assertTrue(output.contains("UITestFacility"));
    }

    @Test
    void addFacilityShowsUpdatedListImmediately() {
        String output = runUI("F\nN\nImmediateFac\nIMF\n\nM\nX\n", false);
        assertTrue(output.contains("Facility added successfully"));
        assertTrue(output.contains("Updated Facilities:"));
        assertTrue(output.contains("ImmediateFac"));
    }

    @Test
    void listFacilitiesShowsExistingData() throws SQLException {
        sharedPC.addFacility("PreloadedFac", "PLF");
        outContent.reset();
        runUI("F\nL\nM\nX\n", false);
        assertTrue(out().contains("PreloadedFac"));
    }

    @Test
    void readFacilityByIdShowsDetails() throws SQLException {
        sharedPC.addFacility("DetailFac", "DTF");
        List<Facility> facs = sharedPC.listFacilities();
        int id = facs.get(facs.size() - 1).getID();

        outContent.reset();
        runUI("F\n" + id + "\nM\nX\n", false);
        String output = out();
        assertTrue(output.contains("Facility Details:"));
        assertTrue(output.contains("DetailFac"));
        assertTrue(output.contains("DTF"));
    }

    @Test
    void readFacilityNonExistentIdShowsNotFound() {
        String output = runUI("F\n99999\nM\nX\n", false);
        assertTrue(output.contains("Facility not found"));
    }

    // ===========================
    // Agent integration tests
    // ===========================

    @Test
    void addAgentThroughUIAndVerifyInList() throws SQLException {
        sharedPC.addFacility("AgentFac1", "AF1");
        List<Facility> facs = sharedPC.listFacilities();
        int facId = facs.get(facs.size() - 1).getID();

        outContent.reset();
        String input = "A\nN\nUITestAgent\n1985-03-15\n\nField operative\n" + facId + "\nL\nM\nX\n";
        runUI(input, false);
        String output = out();
        assertTrue(output.contains("Agent added successfully"));
        assertTrue(output.contains("UITestAgent"));
    }

    @Test
    void addAgentShowsUpdatedListImmediately() throws SQLException {
        sharedPC.addFacility("AgentFac2", "AF2");
        List<Facility> facs = sharedPC.listFacilities();
        int facId = facs.get(facs.size() - 1).getID();

        outContent.reset();
        String input = "A\nN\nImmediateAgent\n1990-06-01\n2020-12-31\nRetired\n" + facId + "\nM\nX\n";
        runUI(input, false);
        String output = out();
        assertTrue(output.contains("Agent added successfully"));
        assertTrue(output.contains("Updated Agents:"));
        assertTrue(output.contains("ImmediateAgent"));
    }

    @Test
    void addAgentInvalidFacilityIdShowsError() {
        // Ensure facilities exist so the prompt reaches facility-id parsing branch.
        assertDoesNotThrow(() -> sharedPC.addFacility("AgentInvalidFac", "AIF"));
        runUI("A\nN\nBadIdAgent\n2000-01-01\n\n\nabc\nM\nX\n", false);
        assertTrue(out().contains("Invalid facility ID"));
    }

    @Test
    void readAgentByIdShowsDetails() throws SQLException {
        sharedPC.addFacility("AgentFac4", "AF4");
        List<Facility> facs = sharedPC.listFacilities();
        int facId = facs.get(facs.size() - 1).getID();
        sharedPC.addAgent("DetailAgent", "1975-11-20", "2010-05-05", "Deep cover", facId);
        List<Agent> agents = sharedPC.listAgents();
        int agentId = agents.get(agents.size() - 1).getId();

        outContent.reset();
        runUI("A\n" + agentId + "\nM\nX\n", false);
        String output = out();
        assertTrue(output.contains("Agent Details:"));
        assertTrue(output.contains("DetailAgent"));
        assertTrue(output.contains("1975-11-20"));
        assertTrue(output.contains("Deep cover"));
    }

    @Test
    void readAgentNonExistentIdShowsNotFound() {
        String output = runUI("A\n99999\nM\nX\n", false);
        assertTrue(output.contains("Agent not found"));
    }

    @Test
    void listAgentsShowsExistingData() throws SQLException {
        sharedPC.addFacility("AgentFac5", "AF5");
        List<Facility> facs = sharedPC.listFacilities();
        int facId = facs.get(facs.size() - 1).getID();
        sharedPC.addAgent("ListableAgent", "1988-02-14", "", "", facId);

        outContent.reset();
        runUI("A\nL\nM\nX\n", false);
        assertTrue(out().contains("ListableAgent"));
    }

    // ===========================
    // Brief/Mission integration tests
    // ===========================

    @Test
    void addBriefThroughUIAndVerifyInList() throws SQLException {
        sharedPC.addFacility("BriefFac1", "BF1");
        List<Facility> facs = sharedPC.listFacilities();
        int facId = facs.get(facs.size() - 1).getID();
        sharedPC.addAgent("BriefAgent1", "1980-01-01", "", "", facId);
        List<Agent> agents = sharedPC.listAgents();
        int agentId = agents.get(agents.size() - 1).getId();

        outContent.reset();
        String input = "B\nN\nUITestMission\nTop secret details\n2026-04-01\n"
                + facId + "\n" + agentId + "\nL\nM\nX\n";
        runUI(input, false);
        String output = out();
        assertTrue(output.contains("Mission added Successfully"));
        assertTrue(output.contains("UITestMission"));
    }

    @Test
    void addBriefShowsUpdatedListImmediately() throws SQLException {
        sharedPC.addFacility("BriefFac2", "BF2");
        List<Facility> facs = sharedPC.listFacilities();
        int facId = facs.get(facs.size() - 1).getID();
        sharedPC.addAgent("BriefAgent2", "1985-06-15", "", "", facId);
        List<Agent> agents = sharedPC.listAgents();
        int agentId = agents.get(agents.size() - 1).getId();

        outContent.reset();
        String input = "B\nN\nImmediateMission\nUrgent brief\n2026-05-01\n"
                + facId + "\n" + agentId + "\nM\nX\n";
        runUI(input, false);
        String output = out();
        assertTrue(output.contains("Mission added Successfully"));
        assertTrue(output.contains("Updated Mission Briefs:"));
        assertTrue(output.contains("ImmediateMission"));
    }

    @Test
    void addBriefInvalidFacilityIdShowsError() {
        // Ensure facilities exist so the prompt reaches facility-id parsing branch.
        assertDoesNotThrow(() -> sharedPC.addFacility("BriefInvalidFac", "BIF"));
        String output = runUI("B\nN\nBadFacMission\nBrief\n2026-01-01\nabc\nM\nX\n", false);
        assertTrue(output.contains("Invalid facility ID"));
    }

    @Test
    void addBriefInvalidAgentIdShowsError() throws SQLException {
        sharedPC.addFacility("BriefFac5", "BF5");
        List<Facility> facs = sharedPC.listFacilities();
        int facId = facs.get(facs.size() - 1).getID();
        sharedPC.addAgent("BriefAgent5", "1980-01-01", "", "", facId);

        outContent.reset();
        runUI("B\nN\nBadAgentMission\nBrief\n2026-01-01\n" + facId + "\nabc\nM\nX\n", false);
        assertTrue(out().contains("Invalid agent ID"));
    }

    @Test
    void searchBriefsThroughUI() throws SQLException {
        sharedPC.addFacility("BriefFac7", "BF7");
        List<Facility> facs = sharedPC.listFacilities();
        int facId = facs.get(facs.size() - 1).getID();
        sharedPC.addAgent("BriefAgent7", "1980-01-01", "", "", facId);
        List<Agent> agents = sharedPC.listAgents();
        int agentId = agents.get(agents.size() - 1).getId();
        sharedPC.addMission("SearchableMission", "unique_search_xyz", "2026-08-01", facId, List.of(agentId));

        outContent.reset();
        runUI("B\nS\nunique_search_xyz\nM\nX\n", false);
        assertTrue(out().contains("SearchableMission"));
    }

    // ===========================
    // Audit Log integration tests
    // ===========================

    @Test
    void auditLogAdminCanView() {
        String output = runUI("R\nX\n", true);
        assertTrue(output.contains("Audit Log:") || output.contains("Audit log is empty"));
    }

    @Test
    void auditLogNonAdminCannotView() {
        String output = runUI("R\nX\n", false);
        assertTrue(output.contains("Unknown option"));
        assertFalse(output.contains("Audit Log:"));
    }

    @Test
    void auditLogShowsReadEventsAfterBriefAccess() {
        String output = runUI("B\n1\nM\nR\nX\n", true);
        if (output.contains("Audit Log:")) {
            assertTrue(output.contains("READ"));
        }
    }

    // ===========================
    // Full flow integration tests
    // ===========================

    @Test
    void fullFlowAddFacilityThenAgent() throws SQLException {
        sharedPC.addFacility("FlowFac", "FLF");
        List<Facility> facs = sharedPC.listFacilities();
        int facId = facs.get(facs.size() - 1).getID();

        outContent.reset();
        runUI("A\nN\nFlowAgent\n1995-07-04\n\nFlow test\n" + facId + "\nM\nX\n", false);
        String output = out();
        assertTrue(output.contains("Agent added successfully"));
        assertTrue(output.contains("FlowAgent"));
    }

    @Test
    void fullFlowAddFacilityAgentAndMission() throws SQLException {
        sharedPC.addFacility("FullFlowFac", "FFF");
        List<Facility> facs = sharedPC.listFacilities();
        int facId = facs.get(facs.size() - 1).getID();
        sharedPC.addAgent("FullFlowAgent", "1992-03-20", "", "", facId);
        List<Agent> agents = sharedPC.listAgents();
        int agentId = agents.get(agents.size() - 1).getId();

        outContent.reset();
        String input = "B\nN\nFullFlowMission\nComplete flow brief\n2026-09-01\n"
                + facId + "\n" + agentId + "\nL\nM\nX\n";
        runUI(input, false);
        String output = out();
        assertTrue(output.contains("Mission added Successfully"));
        assertTrue(output.contains("FullFlowMission"));
    }

    @Test
    void navigateAllMenusIntegration() {
        String output = runUI("B\nM\nA\nM\nF\nM\nX\n", false);
        assertTrue(output.contains("Mission Briefs"));
        assertTrue(output.contains("Agents"));
        assertTrue(output.contains("Facilities"));
        assertTrue(output.contains("Exiting"));
    }

    @Test
    void adminNavigateAllMenusIncludingAuditLog() {
        String output = runUI("B\nM\nA\nM\nF\nM\nR\nX\n", true);
        assertTrue(output.contains("Mission Briefs"));
        assertTrue(output.contains("Agents"));
        assertTrue(output.contains("Facilities"));
        assertTrue(output.contains("Exiting"));
    }

    @Test
    void sendAndReadAgentMessageFlow() throws SQLException {
        sharedPC.addFacility("MsgFac", "MSG");
        int facId = sharedPC.listFacilities().get(sharedPC.listFacilities().size() - 1).getID();
        sharedPC.addAgent("ghost", "1980-01-01", "", "", facId);
        int ghostId = sharedPC.listAgents().get(sharedPC.listAgents().size() - 1).getId();

        sharedPC.setCurrentUsername("director");
        sharedPC.addAgentMessageForCurrentUser(ghostId, "Check-in at 2200.");

        sharedPC.setCurrentUsername("GHOST");
        List<Message> inbox = sharedPC.getAgentMessagesForCurrentUser();
        assertFalse(inbox.isEmpty());
        assertEquals("Check-in at 2200.", inbox.get(inbox.size() - 1).getBody());
        assertEquals("director", inbox.get(inbox.size() - 1).getSenderUsername());
    }
}
