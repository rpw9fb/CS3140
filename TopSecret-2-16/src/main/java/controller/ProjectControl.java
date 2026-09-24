package controller;

import models.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ProjectControl {
    public final FileHandler fileHandler;
    public final DatabaseManager databaseManager;
    private String currentUsername = DatabaseManager.DEFAULT_AUDIT_USERNAME;

    public ProjectControl(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        this.databaseManager = new DatabaseManager();
    }

    public ProjectControl(FileHandler fileHandler, DatabaseManager databaseManager) {
        this.fileHandler = fileHandler;
        this.databaseManager = databaseManager;
    }

    public ProjectControl(FileHandler fileHandler, String dbName) {
        this.fileHandler = fileHandler;
        this.databaseManager = new DatabaseManager(dbName);
    }

    public ProjectControl(DatabaseManager databaseManager) {
        this.fileHandler = new FileHandler(); // or null if unused
        this.databaseManager = databaseManager;
    }

    // =============
    // Missions
    // =============

    public void addMission(String title, String brief, String date, int facilityId, List<Integer> agentIds) throws SQLException {
        databaseManager.addMission(title, brief, date, facilityId, agentIds);
        recordCreateEvent(AuditLog.TYPE_MISSION, title);
    }

    public void importMissions(String tsvFile) {
        databaseManager.loadData(tsvFile);
    }

    public List<String> listMissionTitles() {
        return databaseManager.getAllTitles();
    }

    public String retrieveMission(int number) {
        return retrieveMission(number, currentUsername);
    }

    public String retrieveMission(int number, String username) {
        try {
            Mission mission = databaseManager.getMissionById(number);

            if (mission == null) {
                return "Mission not found.";
            }

            List<Agent> agents = databaseManager.getAgentsForMission(number);

            StringBuilder sb = new StringBuilder();
            sb.append("Title: ").append(mission.getTitle()).append("\n");
            sb.append("Brief: ").append(mission.getBrief()).append("\n");
            sb.append("Date: ").append(mission.getDate()).append("\n");

            sb.append("Agents: ");
            if (agents == null || agents.isEmpty()) {
                sb.append("None");
            } else {
                for (int i = 0; i < agents.size(); i++) {
                    sb.append(agents.get(i).getName());
                    if (i < agents.size() - 1) sb.append(", ");
                }
            }

            recordReadEvent(AuditLog.TYPE_BRIEF, String.valueOf(number), username);
            return sb.toString();

        } catch (SQLException e) {
            return "Error loading mission.";
        }
    }

    public List<Mission> searchMissions(String phrase) {
        return databaseManager.searchMissions(phrase);
    }

    public List<Mission> getAllMissions() throws SQLException {
        return databaseManager.getAllMissions();
    }

    public Mission getMissionById(int id) throws SQLException {
        return databaseManager.getMissionById(id);
    }

    public void assignAgentToMission(int missionId, int agentId) throws SQLException {
        databaseManager.assignAgentToMission(missionId, agentId);
        recordUpdateEvent(AuditLog.TYPE_MISSION, String.valueOf(missionId));
    }

    public void removeAgentFromMission(int missionId, int agentId) throws SQLException {
        databaseManager.removeAgentFromMission(missionId, agentId);
        recordUpdateEvent(AuditLog.TYPE_MISSION, String.valueOf(missionId));
    }

    public List<Agent> getAgentsForMission(int missionId) throws SQLException {
        return databaseManager.getAgentsForMission(missionId);
    }

    public List<Mission> getMissionsForAgent(int agentId) throws SQLException {
        return databaseManager.getMissionsForAgent(agentId);
    }

    // ===============
    // File Retrieval
    // ===============

    public String retrieve(int num) {
        return retrieve(num, "key.txt");
    }

    public String retrieve(int num, String key) {
        File[] files = getValidFiles();

        if (num < 1 || num > files.length) {
            return "Invalid file number.";
        }

        String filename = files[num - 1].getName();

        try {
            Cipher cipher = new Cipher("ciphers/" + key);
            String encrypted = fileHandler.handle(filename);
            return cipher.decrypt(encrypted);
        } catch (IOException e) {
            return "Key file not found.";
        } catch (IllegalArgumentException e) {
            return "Cipher error.";
        }
    }

    private static File[] getValidFiles() {
        File folder = new File("data");

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt") || name.endsWith(".cip"));

        if (files == null) {
            return new File[0];
        }

        Arrays.sort(files);
        return files;
    }

    public static String listFiles() {
        File[] files = getValidFiles();

        if (files.length == 0) {
            return "No files available.";
        }

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < files.length; i++) {
            output.append(String.format("%d %s%n",
                            i + 1,
                            files[i].getName()))
                    .append(System.lineSeparator());
        }

        return output.toString().trim();
    }

    // ===========
    // Agents
    // ===========

    public List<Agent> listAgents() throws SQLException {
        return databaseManager.getAllAgents();
    }

    public void addAgent(String name, String dob, String dod, String notes, int facilityId) throws SQLException {
        databaseManager.addAgent(name, dob, dod, notes, facilityId);
        recordCreateEvent(AuditLog.TYPE_AGENT, name);
    }

    public boolean updateAgent(int id, String name, String dob, String dod, String notes, int facilityId) throws SQLException {
        boolean updated = databaseManager.updateAgent(id, name, dob, dod, notes, facilityId);
        if (updated) {
            recordUpdateEvent(AuditLog.TYPE_AGENT, String.valueOf(id));
        }
        return updated;
    }

    // ===========
    // Facilities
    // ===========

    public List<Facility> listFacilities() throws SQLException {
        return databaseManager.getAllFacilities();
    }

    public void addFacility(String name, String abbreviation, Integer countryId) throws SQLException {
        databaseManager.addFacility(name, abbreviation, countryId);
        recordCreateEvent(AuditLog.TYPE_FACILITY, name);
    }

    public void addFacility(String name, String abbreviation) throws SQLException {
        addFacility(name, abbreviation, null);
    }

    public boolean updateFacility(int id, String name, String abbreviation, Integer countryId) throws SQLException {
        boolean updated = databaseManager.updateFacility(id, name, abbreviation, countryId);

        if (updated) {
            recordUpdateEvent(AuditLog.TYPE_FACILITY, String.valueOf(id));
        }
        return updated;
    }

    public boolean updateFacility(int id, String name, String abbreviation) throws SQLException {
        return updateFacility(id, name, abbreviation, null);
    }

    public List<Integer> getAgentsAtFacility(int facilityId) throws SQLException {
        return databaseManager.getAgentsFacility(facilityId);
    }

    public boolean updateMission(int id, String title, String brief, String date, int facilityId, List<Integer> agentIds) throws SQLException {
        boolean updated = databaseManager.updateMission(id, title, brief, date, facilityId, agentIds);
        if (updated) {
            recordUpdateEvent(AuditLog.TYPE_MISSION, String.valueOf(id));
        }
        return updated;
    }

    // ===========
    // Agent messages
    // ===========

    public void addAgentMessage(int recipientAgentId, String body, String senderUsername) throws SQLException {
        databaseManager.addAgentMessage(recipientAgentId, body, senderUsername);
        recordCreateEvent(AuditLog.TYPE_MESSAGE, String.valueOf(recipientAgentId));
    }

    public void addAgentMessageForCurrentUser(int recipientAgentId, String body) throws SQLException {
        databaseManager.addAgentMessage(recipientAgentId, body, currentUsername);
        recordCreateEvent(AuditLog.TYPE_MESSAGE, String.valueOf(recipientAgentId));
    }

    public List<Message> getAgentMessagesForCurrentUser() throws SQLException {
        return databaseManager.getAgentMessagesForRecipientUsername(currentUsername);
    }

    // ===========
    // Audit
    // ===========

    public void setCurrentUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            this.currentUsername = DatabaseManager.DEFAULT_AUDIT_USERNAME;
        } else {
            this.currentUsername = username;
        }
    }

    public AuditLog getAuditLog() {
        return databaseManager.getAuditLog();
    }

    public List<String> listAuditLogLines() {
        try {
            AuditLog log = databaseManager.getAuditLog();
            if (log == null) {
                return List.of();
            }
            return log.readAllLines();
        } catch (Exception e) {
            return List.of();
        }
    }

    public boolean recordUserAccountCreated(String createdUsername, String actorUsername) {
        try {
            AuditLog log = databaseManager.getAuditLog();
            if (log == null) {
                return false;
            }
            log.recordCreate(AuditLog.TYPE_USER_ACCOUNT, createdUsername, actorUsername);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean recordUserRoleChange(String promotedUsername, String actorUsername) {
        try {
            AuditLog log = databaseManager.getAuditLog();
            if (log == null) {
                return false;
            }
            log.record(AuditLog.EVENT_UPDATE, AuditLog.TYPE_USER_ACCOUNT, promotedUsername, actorUsername);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean recordLogin(String username) {
        try {
            AuditLog log = databaseManager.getAuditLog();
            if (log == null) {
                return false;
            }
            log.recordRead(AuditLog.TYPE_LOGIN, username, username);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void recordCreateEvent(String type, String contentId) {
        try {
            AuditLog log = databaseManager.getAuditLog();
            if (log != null) {
                log.recordCreate(type, contentId, currentUsername);
            }
        } catch (Exception ignored) {
            // Audit log failures should not break core operations.
        }
    }

    private void recordReadEvent(String type, String contentId, String username) {
        try {
            AuditLog log = databaseManager.getAuditLog();
            if (log != null) {
                String actor = (username == null || username.trim().isEmpty()) ? currentUsername : username;
                log.recordRead(type, contentId, actor);
            }
        } catch (Exception ignored) {
            // Audit log failures should not break core operations.
        }
    }

    public Agent retrieveAgent(int id) {
        try {
            List<Agent> agents = listAgents();
            return agents.stream()
                    .filter(a -> a.getId() == id)
                    .findFirst()
                    .orElse(null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Facility retrieveFacility(int id) {
        try {
            List<Facility> facilities = listFacilities();
            return facilities.stream()
                    .filter(f -> f.getID() == id)
                    .findFirst()
                    .orElse(null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void markMessageAsRead(int messageId) throws SQLException
    {
        databaseManager.markMessageAsRead(messageId);
        recordUpdateEvent(AuditLog.TYPE_MESSAGE, String.valueOf(messageId));
    }

    public List<Mission> getMissionsAtFacility(int facilityId) throws SQLException {
        return databaseManager.getAllMissions()
                .stream()
                .filter(mission -> mission.getFacilityId() == facilityId)
                .toList();
    }

    private void recordUpdateEvent(String type, String contentId) {
        try {
            AuditLog log = databaseManager.getAuditLog();
            if (log != null) {
                log.record(AuditLog.EVENT_UPDATE, type, contentId, currentUsername);
            }
        } catch (Exception ignored) {
            // Audit log failures should not break core operations.
        }
    }

    public Message getMessageById(int msgId) throws SQLException {
        return databaseManager.getMessageById(msgId);
    }

    public List<Country> listCountries() throws SQLException {
        return databaseManager.getAllCountries();
    }
}