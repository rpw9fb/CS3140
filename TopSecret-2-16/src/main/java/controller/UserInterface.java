package controller;

import models.*;
import view.TerminalView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserInterface {

    private final ProjectControl projectControl;
    private final Scanner scanner;
    private final TerminalView view = new TerminalView();
    private boolean isAdmin;
    private Password password;

    public UserInterface(Password password, Scanner scanner, DatabaseManager databaseManager) {
        // Credential file exists — authenticate
        this.password = password;
        if (!this.password.authenticate()) {
            throw new RuntimeException("Failed to authenticate");
        }

        String currentUser = password.getCurrentUsername();
        isAdmin = password.isAdminUser(currentUser);
        projectControl = new ProjectControl(new FileHandler(), databaseManager);
        projectControl.setCurrentUsername(currentUser);
        if (password.consumeAccountCreatedDuringAuthenticate()) {
            projectControl.recordUserAccountCreated(currentUser, currentUser);
        }
        projectControl.recordLogin(currentUser);
        this.scanner = scanner;
    }

    /**
     * Test-friendly constructor used by existing unit/integration tests.
     * It bypasses authentication and uses the supplied controller directly.
     */
    public UserInterface(ProjectControl projectControl, Scanner scanner) {
        this.projectControl = projectControl;
        this.scanner = scanner;
        this.password = null;
        this.isAdmin = false;
    }

    /**
     * Test-friendly constructor with explicit admin flag.
     */
    public UserInterface(ProjectControl projectControl, Scanner scanner, boolean isAdmin) {
        this.projectControl = projectControl;
        this.scanner = scanner;
        this.password = null;
        this.isAdmin = isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void run(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("-p")) {
                password.changePassword();
            }

            else if (arg.equals("-u")) {
                i++;
                String newUsername = args[i].trim();
                boolean created = password.createUserUsernameOnly(newUsername);
                if (created) {
                    projectControl.recordUserAccountCreated(newUsername, password.getCurrentUsername());
                    System.out.println("User created successfully!");
                }
            }

            else {
                System.out.printf("Unknown argument %s\n", arg);
                return;
            }
        }

        System.out.println(view.welcome());

        boolean running = true;
        while (running) {
            System.out.print(view.mainMenu(isAdmin));
            String input = scanner.nextLine().trim().toUpperCase();

            switch (input) {
                case "B": briefsMenu();     break;
                case "A": agentsMenu();     break;
                case "F": facilitiesMenu(); break;
                case "G": messagesMenu();   break;
                case "E": editFacility();   break;
                case "P":
                    if (isAdmin) {
                        promoteUser();
                    } else {
                        System.out.println(view.unknownOption(input));
                    }
                    break;
                case "R":
                    if (isAdmin) {
                        System.out.println(view.auditLog(projectControl.listAuditLogLines()));
                    } else {
                        System.out.println(view.unknownOption(input));
                    }
                    break;
                case "X":
                    System.out.println(view.goodbye());
                    running = false;
                    break;
                default:
                    System.out.println(view.unknownOption(input));
                    break;
            }
        }
    }

    /**
     * Backward-compatible entrypoint for tests that call run() directly.
     */
    public void run() {
        run(new String[0]);
    }

    // ===================
    // Briefs Menu
    // ===================

    private void briefsMenu() {
        boolean inBriefs = true;
        while (inBriefs) {
            System.out.print(view.missionsMenu());
            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "L":
                    System.out.println(view.missionList(projectControl.listMissionTitles()));
                    break;
                case "S":
                    searchMissions();
                    break;
                case "N":
                    addMission();
                    break;
                case "M":
                    inBriefs = false;
                    break;
                default:
                    try {
                        int number = Integer.parseInt(input);
                        System.out.println(view.missionDetail(projectControl.retrieveMission(number)));
                    } catch (NumberFormatException e) {
                        System.out.println(view.unknownOption(input));
                    }
                    break;
            }
        }
    }

    public void searchMissions() {
        System.out.print("Enter search phrase: ");
        String phrase = scanner.nextLine().trim();
        System.out.println(view.missionSearchResults(projectControl.searchMissions(phrase), phrase));
    }

    private void addMission() {
        try {
            System.out.print("Enter title: ");
            String title = scanner.nextLine().trim();
            if (title.isEmpty()) {
                System.out.println("Title cannot be empty.");
                return;
            }

            System.out.print("Enter brief: ");
            String brief = scanner.nextLine().trim();

            System.out.print("Enter date: ");
            String date = scanner.nextLine().trim();

            List<Facility> facilities = projectControl.listFacilities();
            if (facilities.isEmpty()) {
                System.out.println("No facilities available. Please add a facility first.");
                return;
            }
            System.out.println(view.facilityList(facilities));
            System.out.print("Enter facility ID: ");
            int facilityId;
            try {
                facilityId = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid facility ID.");
                return;
            }

            List<Integer> facilityAgentIds = projectControl.getAgentsAtFacility(facilityId);
            if (facilityAgentIds.isEmpty()) {
                System.out.println("No agents available at this facility.");
                return;
            }

            List<Agent> allAgents = projectControl.listAgents();
            System.out.println("\nAvailable Agents at Facility " + facilityId + ":");
            for (Agent a : allAgents) {
                if (facilityAgentIds.contains(a.getId())) {
                    System.out.println(a);
                }
            }

            System.out.print("Enter agent IDs (comma-separated): ");
            String agentInput = scanner.nextLine().trim();
            List<Integer> agentIds = new ArrayList<>();
            for (String s : agentInput.split(",")) {
                try {
                    agentIds.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid agent ID: " + s.trim());
                    return;
                }
            }
            if (agentIds.isEmpty()) {
                System.out.println("At least one agent is required.");
                return;
            }

            projectControl.addMission(title, brief, date, facilityId, agentIds);
            System.out.println(view.missionAdded(projectControl.listMissionTitles()));

        } catch (SQLException e) {
            System.out.println(view.error(e.getMessage()));
        }
    }

    // ===================
    // Agents Menu
    // ===================

    private void agentsMenu() {
        boolean inAgents = true;
        while (inAgents) {
            System.out.print(view.agentsMenu());
            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "L":
                    try {
                        System.out.println(view.agentList(projectControl.listAgents()));
                    } catch (SQLException e) {
                        System.out.println(view.error(e.getMessage()));
                    }
                    break;
                case "N":
                    addAgent();
                    break;
                case "M":
                    inAgents = false;
                    break;
                default:
                    try {
                        int id = Integer.parseInt(input);
                        displayAgent(id);
                    } catch (NumberFormatException e) {
                        System.out.println(view.unknownOption(input));
                    }
                    break;
            }
        }
    }

    private void displayAgent(int id) {
        try {
            List<Agent> agents = projectControl.listAgents();
            Agent found = agents.stream()
                    .filter(a -> a.getId() == id)
                    .findFirst()
                    .orElse(null);
            System.out.println(view.agentDetail(found));
        } catch (SQLException e) {
            System.out.println(view.error(e.getMessage()));
        }
    }

    private void addAgent() {
        try {
            System.out.print("Enter name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Name cannot be empty.");
                return;
            }

            System.out.print("Enter date of birth: ");
            String dob = scanner.nextLine().trim();

            System.out.print("Enter date of death (or leave blank): ");
            String dod = scanner.nextLine().trim();

            System.out.print("Enter notes (or leave blank): ");
            String notes = scanner.nextLine().trim();

            List<Facility> facilities = projectControl.listFacilities();
            if (facilities.isEmpty()) {
                System.out.println("No facilities available. Please add a facility first.");
                return;
            }
            System.out.println(view.facilityList(facilities));
            System.out.print("Enter facility ID: ");
            int facilityId;
            try {
                facilityId = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid facility ID.");
                return;
            }

            projectControl.addAgent(name, dob, dod, notes, facilityId);
            System.out.println(view.agentAdded(projectControl.listAgents()));

        } catch (SQLException e) {
            System.out.println(view.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // ===================
    // Facilities Menu
    // ===================

    private void facilitiesMenu() {
        boolean inFacilities = true;
        while (inFacilities) {
            System.out.print(view.facilitiesMenu());
            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "L":
                    try {
                        System.out.println(view.facilityList(projectControl.listFacilities()));
                    } catch (SQLException e) {
                        System.out.println(view.error(e.getMessage()));
                    }
                    break;
                case "N":
                    addFacility();
                    break;
                case "M":
                    inFacilities = false;
                    break;
                case "E":
                    editFacility();
                    break;
                default:
                    try {
                        int id = Integer.parseInt(input);
                        displayFacility(id);
                    } catch (NumberFormatException e) {
                        System.out.println(view.unknownOption(input));
                    }
                    break;
            }
        }
    }

    private void displayFacility(int id) {
        try {
            List<Facility> facilities = projectControl.listFacilities();
            Facility found = facilities.stream()
                    .filter(f -> f.getID() == id)
                    .findFirst()
                    .orElse(null);
            System.out.println(view.facilityDetail(found));
        } catch (SQLException e) {
            System.out.println(view.error(e.getMessage()));
        }
    }

    private void addFacility() {
        try {
            System.out.print("Enter facility name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Name cannot be empty.");
                return;
            }

            System.out.print("Enter abbreviation: ");
            String abbreviation = scanner.nextLine().trim();
            if (abbreviation.isEmpty()) {
                System.out.println("Abbreviation cannot be empty.");
                return;
            }

            List<Country> countries;
            try {
                countries = projectControl.listCountries();
            } catch (SQLException e) {
                System.out.println(view.error(e.getMessage()));
                return;
            }

            if (countries == null || countries.isEmpty()) {
                System.out.println("No countries available. Cannot create facility.");
                return;
            }

            System.out.println("Available Countries:");
            int i = 1;
            for (Country c : countries) {
                System.out.println(i + ". " + c.getName() + ", ID: " + c.getId());
                i++;
            }

            System.out.print("Enter country ID (or leave blank for none): ");
            String raw = scanner.nextLine().trim();

            Integer countryId = null;
            if (!raw.isEmpty()) {
                try {
                    countryId = Integer.parseInt(raw);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid country ID.");
                    return;
                }
            }

            projectControl.addFacility(name, abbreviation, countryId);

            System.out.println(view.facilityAdded(projectControl.listFacilities()));

        } catch (SQLException e) {
            System.out.println(view.error(e.getMessage()));
        }
    }


    private void editFacility() {
        try {
            List<Facility> facilities = projectControl.listFacilities();
            System.out.println("Select Facility ID:");

            for (Facility f : facilities) {
                System.out.println(f.getID() + ". " + f.getName());
            }

            int id = Integer.parseInt(scanner.nextLine().trim());

            Facility f = facilities.stream()
                    .filter(x -> x.getID() == id)
                    .findFirst()
                    .orElse(null);

            if (f == null) {
                System.out.println("Facility not found.");
                return;
            }

            System.out.print("Name (" + f.getName() + "): ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = f.getName();

            System.out.print("Abbreviation (" + f.getAbbreviation() + "): ");
            String abbr = scanner.nextLine().trim();
            if (abbr.isEmpty()) abbr = f.getAbbreviation();

            List<Country> countries;
            try {
                countries = projectControl.listCountries();
            } catch (SQLException e) {
                System.out.println(view.error(e.getMessage()));
                return;
            }

            System.out.println(view.countryList(countries));

            System.out.print("Country ID (-1 for none): ");
            int countryIdRaw = Integer.parseInt(scanner.nextLine().trim());

            Integer countryId = (countryIdRaw == -1) ? null : countryIdRaw;

            projectControl.updateFacility(id, name, abbr, countryId);

            System.out.println("Facility updated.");

        } catch (Exception e) {
            System.out.println(view.error(e.getMessage()));
        }
    }

    // ===================
    // Agent Messages Menu (Team D)
    // ===================

    private void messagesMenu() {
        boolean inMessages = true;
        while (inMessages) {
            System.out.print(view.messagesMenu());
            String input = scanner.nextLine().trim().toUpperCase();
            switch (input) {
                case "S":
                    sendAgentMessage();
                    break;
                case "V":
                    viewInbox();
                    break;
                case "M":
                    inMessages = false;
                    break;
                default:
                    System.out.println(view.unknownOption(input));
                    break;
            }
        }
    }

    private void sendAgentMessage() {
        try {
            List<Agent> agents = projectControl.listAgents();
            if (agents.isEmpty()) {
                System.out.println("No agents available.");
                return;
            }

            System.out.println(view.agentList(agents));
            System.out.print("Enter recipient agent ID: ");
            int recipientId;
            try {
                recipientId = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid agent ID.");
                return;
            }

            System.out.print("Enter message: ");
            String body = scanner.nextLine().trim();
            if (body.isEmpty()) {
                System.out.println("Message body cannot be empty.");
                return;
            }

            projectControl.addAgentMessageForCurrentUser(recipientId, body);
            System.out.println("Message sent.");
        } catch (SQLException e) {
            System.out.println(view.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void viewInbox() {
        try {
            List<Message> inbox = projectControl.getAgentMessagesForCurrentUser();
            System.out.println(view.agentInbox(inbox));
        } catch (SQLException e) {
            System.out.println(view.error(e.getMessage()));
        }
    }

    // ===================
    // Admin: Promote User
    // ===================

    private void promoteUser() {
        if (password == null) {
            System.out.println("User management is not available in this session.");
            return;
        }
        List<java.util.Map.Entry<String, String>> users = password.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        System.out.println("\nAll Users:");
        for (java.util.Map.Entry<String, String> u : users) {
            System.out.println("  " + u.getKey() + " [" + u.getValue() + "]");
        }
        System.out.print("Enter username to promote to admin: ");
        String target = scanner.nextLine().trim();
        if (target.isEmpty()) {
            System.out.println("No username entered.");
            return;
        }
        if (password.promoteToAdmin(target)) {
            projectControl.recordUserRoleChange(target, password.getCurrentUsername());
            System.out.println("User '" + target + "' has been promoted to admin.");
        } else {
            System.out.println("Failed to promote '" + target + "'. User may not exist or is already an admin.");
        }
    }

    // ===================
    // Legacy CLI support
    // ===================

    public static String argument_handler(String[] args) {
        if (args.length > 2) return "Too many arguments";
        if (args.length == 0) return ProjectControl.listFiles();

        ProjectControl pc = new ProjectControl(new FileHandler());
        int fileNumber;
        try {
            fileNumber = Integer.parseInt(args[0]);
        } catch (Exception e) {
            return "The first argument was not an integer";
        }

        return args.length == 1 ? pc.retrieve(fileNumber) : pc.retrieve(fileNumber, args[1]);
    }
}