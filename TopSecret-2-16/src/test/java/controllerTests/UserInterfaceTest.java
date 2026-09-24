package controllerTests;

import controller.DatabaseManager;
import controller.ProjectControl;
import controller.UserInterface;
import models.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserInterfaceTest {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void redirectOut() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void resetOut() {
        System.setOut(originalOut);
    }

    private String out() {
        return outContent.toString();
    }

    private UserInterface buildUI(ProjectControl pc, String input) {
        return new UserInterface(pc, new Scanner(input));
    }

    private UserInterface buildAdminUI(ProjectControl pc, String input) {
        return new UserInterface(pc, new Scanner(input), true);
    }

    // ===========================
    // argument_handler() tests
    // ===========================

    @Test
    public void argumentHandlerTooManyArguments() {
        assertEquals("Too many arguments", UserInterface.argument_handler(new String[]{"1", "key", "extra"}));
    }

    @Test
    public void argumentHandlerNonIntegerArgument() {
        assertEquals("The first argument was not an integer", UserInterface.argument_handler(new String[]{"abc"}));
    }

    @Test
    public void argumentHandlerNoArguments() {
        assertNotNull(UserInterface.argument_handler(new String[]{}));
    }

    // ===========================
    // Main Menu tests
    // ===========================

    @Test
    public void runExitImmediately() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "X\n").run();
        assertTrue(out().contains("Exiting"));
    }

    @Test
    public void runLowerCaseExit() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "x\n").run();
        assertTrue(out().contains("Exiting"));
    }

    @Test
    public void mainMenuDisplaysBriefsOption() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "X\n").run();
        assertTrue(out().contains("B - Mission Briefs"));
    }

    @Test
    public void mainMenuDisplaysAgentsOption() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "X\n").run();
        assertTrue(out().contains("A - Agents"));
    }

    @Test
    public void mainMenuDisplaysFacilitiesOption() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "X\n").run();
        assertTrue(out().contains("F - Facilities"));
    }

    @Test
    public void mainMenuDisplaysMessagesOption() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "X\n").run();
        assertTrue(out().contains("G - Messages"));
    }

    @Test
    public void mainMenuDisplaysExitOption() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "X\n").run();
        assertTrue(out().contains("X - Exit"));
    }

    @Test
    public void mainMenuAdminShowsAuditLogOption() {
        ProjectControl pc = mock(ProjectControl.class);
        buildAdminUI(pc, "X\n").run();
        assertTrue(out().contains("R - Review Audit Log"));
    }

    @Test
    public void mainMenuNonAdminHidesAuditLogOption() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "X\n").run();
        assertFalse(out().contains("R - Review Audit Log"));
    }

    @Test
    public void setAdminTrueShowsAuditLogOption() {
        ProjectControl pc = mock(ProjectControl.class);
        UserInterface ui = buildUI(pc, "X\n");
        ui.setAdmin(true);
        ui.run();
        assertTrue(out().contains("R - Review Audit Log"));
    }

    @Test
    public void mainMenuUnknownOption() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "Z\nX\n").run();
        assertTrue(out().contains("Unknown option"));
    }

    @Test
    public void mainMenuNonAdminSelectsR() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "R\nX\n").run();
        assertTrue(out().contains("Unknown option"));
    }

    @Test
    public void mainMenuRedisplaysAfterAction() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "B\nM\nX\n").run();
        long count = out().lines().filter(l -> l.startsWith("Choice: ")).count();
        assertTrue(count >= 2);
    }

    // ===========================
    // Briefs Menu tests
    // ===========================

    @Test
    public void briefsMenuDisplaysAllOptions() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "B\nM\nX\n").run();
        String output = out();
        assertTrue(output.contains("L - List all Briefs"));
        assertTrue(output.contains("S - Search Briefs"));
        assertTrue(output.contains("# - Enter Number to Read a Brief"));
        assertTrue(output.contains("N - Add New Brief"));
        assertTrue(output.contains("M - Return to Main Menu"));
    }

    @Test
    public void briefsListShowsTitles() {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listMissionTitles()).thenReturn(List.of("1. Op Alpha", "2. Op Beta"));
        buildUI(pc, "B\nL\nM\nX\n").run();
        String output = out();
        assertTrue(output.contains("Available Mission Briefs"));
        assertTrue(output.contains("Op Alpha"));
        assertTrue(output.contains("Op Beta"));
    }

    @Test
    public void briefsListEmpty() {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listMissionTitles()).thenReturn(List.of());
        buildUI(pc, "B\nL\nM\nX\n").run();
        assertTrue(out().contains("No missions available"));
    }

    @Test
    public void briefsListLowerCase() {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listMissionTitles()).thenReturn(List.of("1. Op Alpha"));
        buildUI(pc, "B\nl\nM\nX\n").run();
        assertTrue(out().contains("Available Mission Briefs"));
    }

    @Test
    public void briefsReadValidMission() {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.retrieveMission(3)).thenReturn("Op Gamma: Secret brief (2026-01-01)");
        buildUI(pc, "B\n3\nM\nX\n").run();
        verify(pc).retrieveMission(3);
        assertTrue(out().contains("Op Gamma: Secret brief (2026-01-01)"));
    }

    @Test
    public void briefsReadInvalidMissionNumber() {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.retrieveMission(99)).thenReturn(null);
        buildUI(pc, "B\n99\nM\nX\n").run();
        assertTrue(out().contains("Invalid mission number"));
    }

    @Test
    public void briefsSearchWithResults() {
        ProjectControl pc = mock(ProjectControl.class);
        Mission m = new Mission(1, "Op Alpha", "Secret intel", "2026-01-01", 1, List.of(1));
        when(pc.searchMissions("intel")).thenReturn(List.of(m));
        buildUI(pc, "B\nS\nintel\nM\nX\n").run();
        verify(pc).searchMissions("intel");
        assertTrue(out().contains("Op Alpha"));
    }

    @Test
    public void briefsSearchNoResults() {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.searchMissions("nonexistent")).thenReturn(List.of());
        buildUI(pc, "B\nS\nnonexistent\nM\nX\n").run();
        assertTrue(out().contains("No matches found"));
    }

    @Test
    public void briefsAddEmptyTitle() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "B\nN\n\nM\nX\n").run();
        assertTrue(out().contains("Title cannot be empty"));
    }

    @Test
    public void briefsAddNoFacilities() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listFacilities()).thenReturn(List.of());
        buildUI(pc, "B\nN\nTestTitle\nTestBrief\n2026-01-01\nM\nX\n").run();
        assertTrue(out().contains("No facilities available"));
    }

    @Test
    public void briefsInvalidOption() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "B\nabc\nM\nX\n").run();
        assertTrue(out().contains("Unknown option"));
    }

    @Test
    public void briefsReturnToMainMenu() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "B\nM\nX\n").run();
        assertTrue(out().contains("Exiting"));
    }

    @Test
    public void briefsMultipleActions() {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listMissionTitles()).thenReturn(List.of("1. Op Alpha"));
        when(pc.retrieveMission(1)).thenReturn("Op Alpha: Brief text (2026-01-01)");
        buildUI(pc, "B\nL\n1\nM\nX\n").run();
        verify(pc).listMissionTitles();
        verify(pc).retrieveMission(1);
    }

    @Test
    public void briefsRetrieveNeverCalledOnList() {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listMissionTitles()).thenReturn(List.of());
        buildUI(pc, "B\nL\nM\nX\n").run();
        verify(pc, never()).retrieveMission(anyInt());
    }

    // ===========================
    // Agents Menu tests
    // ===========================

    @Test
    public void agentsMenuDisplaysAllOptions() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "A\nM\nX\n").run();
        String output = out();
        assertTrue(output.contains("L - List all Agents"));
        assertTrue(output.contains("# - Enter ID to read Agent Details"));
        assertTrue(output.contains("N - Add New Agent"));
        assertTrue(output.contains("M - Return to Main Menu"));
    }

    @Test
    public void agentsListShowsAgents() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        Agent a1 = new Agent(1, "Alice", "1990-01-01", "", "Notes", 1);
        when(pc.listAgents()).thenReturn(List.of(a1));
        buildUI(pc, "A\nL\nM\nX\n").run();
        assertTrue(out().contains("Alice"));
    }

    @Test
    public void agentsListEmpty() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listAgents()).thenReturn(List.of());
        buildUI(pc, "A\nL\nM\nX\n").run();
        assertTrue(out().contains("No agents available"));
    }

    @Test
    public void agentsReadById() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        Agent a = new Agent(5, "Bob", "1985-03-15", "2020-01-01", "Deep cover", 2);
        when(pc.listAgents()).thenReturn(List.of(a));
        buildUI(pc, "A\n5\nM\nX\n").run();
        String output = out();
        assertTrue(output.contains("Agent Details:"));
        assertTrue(output.contains("Bob"));
        assertTrue(output.contains("1985-03-15"));
        assertTrue(output.contains("Deep cover"));
    }

    @Test
    public void agentsReadNotFound() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listAgents()).thenReturn(List.of());
        buildUI(pc, "A\n999\nM\nX\n").run();
        assertTrue(out().contains("Agent not found"));
    }

    @Test
    public void agentsAddEmptyName() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "A\nN\n\nM\nX\n").run();
        assertTrue(out().contains("Name cannot be empty"));
    }

    @Test
    public void agentsAddNoFacilities() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listFacilities()).thenReturn(List.of());
        buildUI(pc, "A\nN\nJohn\n1990-01-01\n\n\nM\nX\n").run();
        assertTrue(out().contains("No facilities available"));
    }

    @Test
    public void agentsAddInvalidFacilityId() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listFacilities()).thenReturn(List.of(new Facility(1, "HQ", "HQ")));
        buildUI(pc, "A\nN\nJohn\n1990-01-01\n\n\nabc\nM\nX\n").run();
        assertTrue(out().contains("Invalid facility ID"));
    }

    @Test
    public void agentsAddSuccess() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listFacilities()).thenReturn(List.of(new Facility(1, "HQ", "HQ")));
        when(pc.listAgents()).thenReturn(List.of(new Agent(1, "John", "1990-01-01", "", "", 1)));
        buildUI(pc, "A\nN\nJohn\n1990-01-01\n\n\n1\nM\nX\n").run();
        verify(pc).addAgent("John", "1990-01-01", "", "", 1);
        assertTrue(out().contains("Agent added successfully"));
    }

    @Test
    public void agentsInvalidOption() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "A\nabc\nM\nX\n").run();
        assertTrue(out().contains("Unknown option"));
    }

    @Test
    public void agentsReturnToMainMenu() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "A\nM\nX\n").run();
        assertTrue(out().contains("Exiting"));
    }

    // ===========================
    // Messages Menu tests
    // ===========================

    @Test
    public void messagesMenuDisplaysAllOptions() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "G\nM\nX\n").run();
        String output = out();
        assertTrue(output.contains("Messages"));
        assertTrue(output.contains("S - Send message to agent"));
        assertTrue(output.contains("V - View my inbox"));
        assertTrue(output.contains("M - Return to Main Menu"));
    }

    @Test
    public void messagesSendSuccess() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        Agent a1 = new Agent(1, "Ghost", "1970-01-01", "", "", 1);
        when(pc.listAgents()).thenReturn(List.of(a1));

        buildUI(pc, "G\nS\n1\nMeet me at dawn\nM\nX\n").run();
        verify(pc).addAgentMessageForCurrentUser(1, "Meet me at dawn");
        assertTrue(out().contains("Message sent."));
    }

    @Test
    public void messagesViewInbox() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.getAgentMessagesForCurrentUser()).thenReturn(
                List.of(new Message(1, 2, "Status update", "director"))
        );
        buildUI(pc, "G\nV\nM\nX\n").run();
        assertTrue(out().contains("Inbox:"));
        assertTrue(out().contains("Status update"));
        assertTrue(out().contains("director"));
    }

    @Test
    public void messagesViewInboxEmpty() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.getAgentMessagesForCurrentUser()).thenReturn(List.of());
        buildUI(pc, "G\nV\nM\nX\n").run();
        assertTrue(out().contains("No messages for your account."));
    }

    // ===========================
    // Facilities Menu tests
    // ===========================

    @Test
    public void facilitiesMenuDisplaysAllOptions() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "F\nM\nX\n").run();
        String output = out();
        assertTrue(output.contains("L - List all Facilities"));
        assertTrue(output.contains("# - Enter ID to read facility details"));
        assertTrue(output.contains("N - Add New Facility"));
        assertTrue(output.contains("M - Return to Main Menu"));
    }

    @Test
    public void facilitiesListShowsFacilities() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listFacilities()).thenReturn(List.of(new Facility(1, "Alpha Base", "AB")));
        buildUI(pc, "F\nL\nM\nX\n").run();
        assertTrue(out().contains("Alpha Base"));
    }

    @Test
    public void facilitiesListEmpty() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listFacilities()).thenReturn(List.of());
        buildUI(pc, "F\nL\nM\nX\n").run();
        assertTrue(out().contains("No facilities available"));
    }

    @Test
    public void facilitiesReadById() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listFacilities()).thenReturn(List.of(new Facility(3, "Bravo HQ", "BHQ")));
        buildUI(pc, "F\n3\nM\nX\n").run();
        String output = out();
        assertTrue(output.contains("Facility Details:"));
        assertTrue(output.contains("Bravo HQ"));
        assertTrue(output.contains("BHQ"));
    }

    @Test
    public void facilitiesReadNotFound() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listFacilities()).thenReturn(List.of());
        buildUI(pc, "F\n999\nM\nX\n").run();
        assertTrue(out().contains("Facility not found"));
    }

    @Test
    public void facilitiesAddEmptyName() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "F\nN\n\nM\nX\n").run();
        assertTrue(out().contains("Name cannot be empty"));
    }

    @Test
    public void facilitiesAddEmptyAbbreviation() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "F\nN\nTestFacility\n\nM\nX\n").run();
        assertTrue(out().contains("Abbreviation cannot be empty"));
    }

    @Test
    public void facilitiesInvalidOption() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "F\nabc\nM\nX\n").run();
        assertTrue(out().contains("Unknown option"));
    }

    @Test
    public void facilitiesReturnToMainMenu() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "F\nM\nX\n").run();
        assertTrue(out().contains("Exiting"));
    }

    // ===========================
    // Audit Log tests
    // ===========================

    @Test
    public void auditLogAdminViewsEntries() {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listAuditLogLines()).thenReturn(List.of(
                "READ | BRIEF | 1 | user=admin | 2026-01-01",
                "CREATE | AGENT | 2 | user=admin | 2026-01-02"
        ));
        buildAdminUI(pc, "R\nX\n").run();
        String output = out();
        assertTrue(output.contains("Audit Log:"));
        assertTrue(output.contains("READ | BRIEF | 1 | user=admin | 2026-01-01"));
        assertTrue(output.contains("CREATE | AGENT | 2 | user=admin | 2026-01-02"));
    }

    @Test
    public void auditLogAdminEmptyLog() {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listAuditLogLines()).thenReturn(List.of());
        buildAdminUI(pc, "R\nX\n").run();
        assertTrue(out().contains("Audit log is empty"));
    }

    @Test
    public void auditLogNonAdminCannotAccess() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "R\nX\n").run();
        assertTrue(out().contains("Unknown option"));
        verify(pc, never()).listAuditLogLines();
    }

    @Test
    public void auditLogAdminCallsProjectControl() {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listAuditLogLines()).thenReturn(List.of("entry"));
        buildAdminUI(pc, "R\nX\n").run();
        verify(pc).listAuditLogLines();
    }

    // ===========================
    // Navigation flow tests
    // ===========================

    @Test
    public void navigateAllMenusAndReturn() {
        ProjectControl pc = mock(ProjectControl.class);
        buildUI(pc, "B\nM\nA\nM\nF\nM\nX\n").run();
        String output = out();
        assertTrue(output.contains("Mission Briefs"));
        assertTrue(output.contains("Agents"));
        assertTrue(output.contains("Facilities"));
        assertTrue(output.contains("Exiting"));
    }

    @Test
    public void promoteUserSuccess() throws SQLException {
        Password password = mock(Password.class);
        when(password.authenticate()).thenReturn(true);
        when(password.getCurrentUsername()).thenReturn("admin");
        when(password.isAdminUser("admin")).thenReturn(true);
        when(password.getAllUsers()).thenReturn(List.of(new java.util.AbstractMap.SimpleEntry<>("user1", "USER")));
        when(password.promoteToAdmin("user1")).thenReturn(true);

        UserInterface ui = new UserInterface(password, new Scanner("P\nuser1\nX\n"), mock(DatabaseManager.class));
        ui.run();
        assertTrue(out().contains("promoted to admin"));
    }

    @Test
    public void facilitiesAddSuccess() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listCountries()).thenReturn(List.of(new Country(1, "UK")));
        when(pc.listFacilities()).thenReturn(List.of(new Facility(1, "HQ", "HQ", 1, "UK")));
        buildUI(pc, "F\nN\nHQ\nHQ\n1\nM\nX\n").run();
        verify(pc).addFacility("HQ", "HQ", 1);
        assertTrue(out().contains("Facility added successfully"));
    }

    @Test
    public void facilitiesEditSuccess() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        Facility f = new Facility(1, "Old", "O", null, null);
        when(pc.listFacilities()).thenReturn(List.of(f));
        when(pc.listCountries()).thenReturn(List.of());
        buildUI(pc, "F\nE\n1\nNew\nN\n-1\nM\nX\n").run();
        verify(pc).updateFacility(1, "New", "N", null);
        assertTrue(out().contains("Facility updated"));
    }

    @Test
    public void agentsAddWithSpecificFacility() throws SQLException {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listFacilities()).thenReturn(List.of(new Facility(10, "Secret Lab", "SL")));
        when(pc.listAgents()).thenReturn(List.of());
        buildUI(pc, "A\nN\nNewAgent\n1990\n\n\n10\nM\nX\n").run();
        verify(pc).addAgent("NewAgent", "1990", "", "", 10);
    }

    @Test
    public void adminNavigateAllMenusIncludingAuditLog() {
        ProjectControl pc = mock(ProjectControl.class);
        when(pc.listAuditLogLines()).thenReturn(List.of());
        buildAdminUI(pc, "B\nM\nA\nM\nF\nM\nR\nX\n").run();
        String output = out();
        assertTrue(output.contains("Mission Briefs"));
        assertTrue(output.contains("Agents"));
        assertTrue(output.contains("Facilities"));
        assertTrue(output.contains("Audit log is empty"));
        assertTrue(output.contains("Exiting"));
    }

    @Test
    void constructor_successfulAuthentication_recordsLoginAndUserCreation() {
        Password password = mock(Password.class);
        Scanner scanner = mock(Scanner.class);
        DatabaseManager db = mock(DatabaseManager.class);

        when(password.authenticate()).thenReturn(true);
        when(password.getCurrentUsername()).thenReturn("alice");
        when(password.isAdminUser("alice")).thenReturn(false);
        when(password.consumeAccountCreatedDuringAuthenticate()).thenReturn(true);

        UserInterface ui = new UserInterface(password, scanner, db);

        verify(password).authenticate();
        verify(password).getCurrentUsername();
        verify(password).isAdminUser("alice");
        verify(password).consumeAccountCreatedDuringAuthenticate();
        verify(password).getCurrentUsername();
    }

    @Test
    void run_changePassword_argument_callsPasswordChange() {
        Password password = mock(Password.class);
        Scanner scanner = mock(Scanner.class);
        DatabaseManager db = mock(DatabaseManager.class);

        when(password.authenticate()).thenReturn(true);
        when(password.getCurrentUsername()).thenReturn("alice");
        when(password.isAdminUser("alice")).thenReturn(false);
        when(password.consumeAccountCreatedDuringAuthenticate()).thenReturn(false);

        when(scanner.nextLine()).thenReturn("X");
        UserInterface ui = new UserInterface(password, scanner, db);

        ui.run(new String[]{"-p"});
        verify(password, times(1)).changePassword();
    }

}
