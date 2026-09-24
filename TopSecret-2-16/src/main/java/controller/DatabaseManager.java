package controller;

import models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DatabaseManager implements SearchFeature {

    public static final String DEFAULT_AUDIT_USERNAME = "unknown";
    private boolean initialized = false;

    private Random random;
    private Connection connection;
    private FileHandler fileHandler;
    private AuditLog auditLog;

    public DatabaseManager(){
        this("DatabaseManager.db", new Random());
    }

    public DatabaseManager(String name)
    {
        this(name, new Random());
    }

    public DatabaseManager(String name, Random random){
        this(name, random, false);
    }

    public DatabaseManager(String name, Random random, boolean skipLoad){
        try{
            this.random = random;
            this.fileHandler = new FileHandler();

            String dbPath = name.equals(":memory:")
                    ? "jdbc:sqlite::memory:"
                    : "jdbc:sqlite:data/" + name;

            connection = DriverManager.getConnection(dbPath);

            Statement pragma = connection.createStatement();
            pragma.execute("PRAGMA foreign_keys = ON;");
            pragma.close();

            String createTable = """
                    CREATE TABLE IF NOT EXISTS missions (
                        id INTEGER PRIMARY KEY,
                        title TEXT,
                        brief TEXT,
                        date TEXT,
                        facility_id INTEGER NOT NULL,
                        FOREIGN KEY(facility_id) REFERENCES facilities (id)
                    );
                    """;
            Statement statement = connection.createStatement();
            statement.executeUpdate(createTable);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS countries (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL UNIQUE
                );
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS facilities (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    abbreviation TEXT NOT NULL UNIQUE,
                    country_id INTEGER,
                    FOREIGN KEY (country_id) REFERENCES countries(id)
                );
                """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS agents (
                        id INTEGER PRIMARY KEY,
                        name TEXT NOT NULL,
                        date_of_birth TEXT,
                        date_of_death TEXT,
                        notes TEXT,
                        facility_id INTEGER NOT NULL,
                        FOREIGN KEY(facility_id) REFERENCES facilities(id)
                    );
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS mission_agents (
                        mission_id INTEGER,
                        agent_id INTEGER,
                        PRIMARY KEY (mission_id, agent_id),
                        FOREIGN KEY (mission_id) REFERENCES missions(id),
                        FOREIGN KEY (agent_id) REFERENCES agents(id)
                    );
                    """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS briefs (
                    id INTEGER PRIMARY KEY,
                    mission_id INTEGER NOT NULL,
                    content TEXT NOT NULL,
                    FOREIGN KEY (mission_id) REFERENCES missions(id)
                );
                """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS agent_messages (
                        id INTEGER PRIMARY KEY,
                        agent_id INTEGER NOT NULL,
                        body TEXT NOT NULL,
                        sender_username TEXT NOT NULL,
                        is_read INTEGER DEFAULT 0, -- 0 for false, 1 for true
                        FOREIGN KEY (agent_id) REFERENCES agents(id)
                    );
                    """);

            this.auditLog = new AuditLog(connection);

            if (!skipLoad) {
                initializeData();
            }

        } catch (SQLException e){
            System.out.println("Database connection failed");
        }
    }

    private void initializeData() {
        if (initialized) return;

        loadCountries("countries.tsv");
        loadFacilities("facilities.tsv");

        try (Statement statement = connection.createStatement()) {
            ResultSet results = statement.executeQuery("SELECT COUNT(*) AS count FROM missions");
            results.next();

            if (results.getInt("count") == 0) {
                loadData("mission_briefs.tsv");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        initialized = true;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void addFacility(String name, String abbreviation, Integer country_id) throws SQLException {
        String sql = ("INSERT INTO facilities (name, abbreviation, country_id) VALUES (?, ?, ?)");
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.setString(2, abbreviation);
        if (country_id == null) {
            stmt.setNull(3, Types.INTEGER);
        } else {
            stmt.setInt(3, country_id);
        }
        stmt.executeUpdate();
        stmt.close();
    }

    public void addFacility(String name, String abbreviation) throws SQLException {
        addFacility(name, abbreviation, null);
    }

    public boolean updateFacility(int id, String name, String abbreviation, Integer country_id) throws SQLException
    {
        String sql = "UPDATE facilities SET name = ?, abbreviation = ?, country_id = ? WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.setString(2, abbreviation);
        if (country_id == null) {
            stmt.setNull(3, Types.INTEGER);
        } else {
            stmt.setInt(3, country_id);
        }
        stmt.setInt(4, id);
        int affectedRows = stmt.executeUpdate();
        stmt.close();
        return affectedRows > 0;
    }

    public boolean updateFacility(int id, String name, String abbreviation) throws SQLException {
        return updateFacility(id, name, abbreviation, null);
    }

    public boolean facilityExists(int id) throws SQLException
    {
        String sql = "SELECT 1 FROM facilities WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql))
        {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public List<Facility> getAllFacilities() throws SQLException
    {
        List<Facility> facilities = new ArrayList<>();
        String sql = """
            SELECT f.id, f.name, f.abbreviation, f.country_id, c.name AS country_name
            FROM facilities f
            LEFT JOIN countries c ON f.country_id = c.id
            ORDER BY f.id
        """;
        ResultSet rs = connection.createStatement().executeQuery(sql);

        while (rs.next())
        {
            Integer countryId = rs.getInt("country_id");
            if (rs.wasNull()) {
                countryId = null;
            }
            facilities.add(new Facility(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("abbreviation"),
                            countryId,
                            rs.getString("country_name")
                    )
            );
        }
        rs.close();
        return facilities;
    }

    public void loadAgents(String filename)
    {
        FileHandler fh = new FileHandler();
        String content = fh.handle(filename);

        if (content.equals("File Not Found.") || content.equals("Insufficient File"))
        {
            System.out.println("Could not load facilities from " + filename);
            return;
        }

        List<Integer> facilityIds = new ArrayList<>();
        try
        {
            ResultSet frs = connection.createStatement().executeQuery("SELECT id FROM facilities");
            while (frs.next())
            {
                facilityIds.add(frs.getInt("id"));
            }
            frs.close();
        } catch (SQLException e)
        {
            System.out.println("Error fetching facility IDs for agents");
            return;
        }

        if (facilityIds.isEmpty())
        {
            System.out.println("No facilities available for agent assignment.");
            return;
        }

        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++)
        {
            String[] data = lines[i].split("\t");
            if (data.length >= 2)
            {
                try
                {
                    String name = data[0].trim();
                    String dob = data[1].trim();
                    String dod = "";
                    String notes = "";
                    int facilityId = data.length > 2
                            ? Integer.parseInt(data[2].trim())
                            : facilityIds.get(random.nextInt(facilityIds.size()));
                    addAgent(name, dob, dod, notes, facilityId);
                } catch (SQLException e)
                {
                    System.out.println("Error adding agent: " + data[0]);
                } catch (NumberFormatException e)
                {
                    System.out.println("Invalid facility ID for agent: " + data[0]);
                }
            }
        }
    }

    public void addAgent(String name, String dob, String dod, String notes, int facilityId) throws SQLException
    {
        if (!facilityExists(facilityId))
        {
            throw new IllegalArgumentException("Facility does not exist.");
        }

        String sql = "INSERT INTO agents (name, date_of_birth, date_of_death, notes, facility_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.setString(2, dob);
        stmt.setString(3, dod);
        stmt.setString(4, notes);
        stmt.setInt(5, facilityId);
        stmt.executeUpdate();
        stmt.close();
    }

    public boolean updateAgent(int id, String name, String dob, String dod, String notes, int facilityId) throws SQLException
    {
        if (!facilityExists(facilityId)) {
            throw new IllegalArgumentException("Facility does not exist.");
        }
        String sql = "UPDATE agents SET name = ?, date_of_birth = ?, date_of_death = ?, notes = ?, facility_id = ? WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.setString(2, dob);
        stmt.setString(3, dod);
        stmt.setString(4, notes);
        stmt.setInt(5, facilityId);
        stmt.setInt(6, id);
        int affectedRows = stmt.executeUpdate();
        stmt.close();
        return affectedRows > 0;
    }

    public List<Integer> getAgentsFacility(int facilityId) throws SQLException
    {
        List<Integer> agentIds = new ArrayList<>();
        String sql = "SELECT id FROM agents WHERE facility_id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, facilityId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
        {
            agentIds.add(rs.getInt("id"));
        }
        rs.close();
        stmt.close();
        return agentIds;
    }

    public boolean agentExists(int id) throws SQLException
    {
        String sql = "SELECT id FROM agents WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        boolean exists = rs.next();
        rs.close();
        stmt.close();
        return exists;
    }

    public List<Agent> getAllAgents() throws SQLException
    {
        List<Agent> agents = new ArrayList<>();
        String sql = "SELECT * FROM agents ORDER BY id";
        ResultSet rs = connection.createStatement().executeQuery(sql);

        while (rs.next())
        {
            agents.add(new Agent(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("date_of_birth"),
                    rs.getString("date_of_death"),
                    rs.getString("notes"),
                    rs.getInt("facility_id")
            ));
        }
        rs.close();
        return agents;
    }

    public boolean agentToFacility (int agentId, int facilityId) throws SQLException
    {
        String sql = "SELECT id FROM agents WHERE id = ? AND facility_id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, agentId);
        stmt.setInt(2, facilityId);
        ResultSet rs = stmt.executeQuery();
        boolean valid = rs.next();
        rs.close();
        stmt.close();
        return valid;
    }

    public void addAgentMessage(int recipientAgentId, String body, String senderUsername) throws SQLException
    {
        if (body == null || body.trim().isEmpty())
        {
            throw new IllegalArgumentException("Message body cannot be empty.");
        }
        if (senderUsername == null || senderUsername.trim().isEmpty())
        {
            throw new IllegalArgumentException("Sender username is required.");
        }
        if (!agentExists(recipientAgentId))
        {
            throw new IllegalArgumentException("Agent does not exist.");
        }

        String sql = "INSERT INTO agent_messages (agent_id, body, sender_username) VALUES (?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, recipientAgentId);
        stmt.setString(2, body.trim());
        stmt.setString(3, senderUsername.trim());
        stmt.executeUpdate();
        stmt.close();
    }

    public List<Message> getAgentMessagesForRecipientUsername(String username) throws SQLException
    {
        List<Message> messages = new ArrayList<>();
        if (username == null || username.trim().isEmpty())
        {
            return messages;
        }

        String sql = """
                SELECT m.id, m.agent_id, m.body, m.sender_username, m.is_read
                FROM agent_messages m
                INNER JOIN agents a ON a.id = m.agent_id
                WHERE LOWER(a.name) = LOWER(?)
                ORDER BY m.id
                """;
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, username.trim());
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
        {
            messages.add(new Message(
                    rs.getInt("id"),
                    rs.getInt("agent_id"),
                    rs.getString("body"),
                    rs.getString("sender_username"),
                    rs.getInt("is_read") == 1
            ));
        }
        rs.close();
        stmt.close();
        return messages;
    }

    public void loadData(String tsv)
    {
        FileHandler fh = new FileHandler();
        String content = fh.handle(tsv);

        if (content.equals("File Not Found.") || content.equals("Insufficient File"))
        {
            System.out.println("Cannot load missions from " + tsv);
            return;
        }

        List<Integer> facilityIds = new ArrayList<>();
        try
        {
            ResultSet rs = connection.createStatement().executeQuery("SELECT id FROM facilities");
            while (rs.next())
            {
                facilityIds.add(rs.getInt("id"));
            }
            rs.close();
        } catch (SQLException e)
        {
            System.out.println("Error fetching facility IDs");
            return;
        }

        if (facilityIds.isEmpty())
        {
            System.out.println("No facilities available for assignment.");
            return;
        }

        String[] lines = content.split("\n");
        for (int i = 1; i < lines.length; i++)
        {
            String[] data = lines[i].split("\t");
            if (data.length >= 3)
            {
                try
                {
                    String title = data[0].trim();
                    String date = data[1].trim();
                    String brief = data[2].trim();

                    List<Integer> shuffled = new ArrayList<>(facilityIds);
                    java.util.Collections.shuffle(shuffled, random);
                    int facilityId = -1;
                    List<Integer> agentIds = new ArrayList<>();
                    for (int fid : shuffled)
                    {
                        List<Integer> candidates = getAgentsFacility(fid);
                        if (!candidates.isEmpty())
                        {
                            facilityId = fid;
                            agentIds = candidates;
                            break;
                        }
                    }
                    if (facilityId == -1)
                    {
                        continue;
                    }

                    addMission(title, brief, date, facilityId, agentIds);
                } catch (SQLException e)
                {
                    System.out.println("Error adding mission: " + data[0]);
                }
            }
        }
    }

    public void addMission(String title, String brief, String date, int facilityId, List<Integer> agentIds) throws SQLException
    {
        if (!facilityExists(facilityId))
        {
            throw new IllegalArgumentException("Facility does not exist.");
        }

        if (agentIds == null || agentIds.isEmpty())
        {
            throw new IllegalArgumentException("Mission must have at least one agent.");
        }

        for (int agentId : agentIds)
        {
            if (!agentExists(agentId))
            {
                throw new IllegalArgumentException("Agent does not exist: " + agentId);
            }
        }

        String sql = "INSERT INTO missions (title, brief, date, facility_id) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, title);
        stmt.setString(2, brief);
        stmt.setString(3, date);
        stmt.setInt(4, facilityId);
        stmt.executeUpdate();

        ResultSet keys = stmt.getGeneratedKeys();
        keys.next();
        int missionId = keys.getInt(1);
        keys.close();
        stmt.close();

        for (int agentId : agentIds)
        {
            String linkSQL = "INSERT INTO mission_agents (mission_id, agent_id) VALUES (?, ?)";
            PreparedStatement linkStmt = connection.prepareStatement(linkSQL);
            linkStmt.setInt(1, missionId);
            linkStmt.setInt(2, agentId);
            linkStmt.executeUpdate();
            linkStmt.close();
        }
    }

    public boolean updateMission(int id, String title, String brief, String date, int facilityId, List<Integer> agentIds) throws SQLException
    {
        if (!facilityExists(facilityId))
        {
            throw new IllegalArgumentException("Facility does not exist.");
        }

        if (agentIds == null || agentIds.isEmpty())
        {
            throw new IllegalArgumentException("Mission must have at least one agent.");
        }

        for (int agentId : agentIds)
        {
            if (!agentExists(agentId))
            {
                throw new IllegalArgumentException("Agent does not exist: " + agentId);
            }
        }

        String updateMissionSql = """
                UPDATE missions
                SET title = ?, brief = ?, date = ?, facility_id = ?
                WHERE id = ?
                """;
        PreparedStatement missionStmt = connection.prepareStatement(updateMissionSql);
        missionStmt.setString(1, title);
        missionStmt.setString(2, brief);
        missionStmt.setString(3, date);
        missionStmt.setInt(4, facilityId);
        missionStmt.setInt(5, id);
        int affectedRows = missionStmt.executeUpdate();
        missionStmt.close();

        if (affectedRows == 0)
        {
            return false;
        }

        PreparedStatement deleteLinks = connection.prepareStatement("DELETE FROM mission_agents WHERE mission_id = ?");
        deleteLinks.setInt(1, id);
        deleteLinks.executeUpdate();
        deleteLinks.close();

        for (int agentId : agentIds)
        {
            PreparedStatement linkStmt = connection.prepareStatement(
                    "INSERT INTO mission_agents (mission_id, agent_id) VALUES (?, ?)"
            );
            linkStmt.setInt(1, id);
            linkStmt.setInt(2, agentId);
            linkStmt.executeUpdate();
            linkStmt.close();
        }
        return true;
    }

    public List<Mission> getAllMissions() throws SQLException
    {
        List<Mission> missions = new ArrayList<>();
        String sql = "SELECT id, title, brief, date, facility_id FROM missions ORDER BY id";
        ResultSet rs = connection.createStatement().executeQuery(sql);

        while (rs.next())
        {
            int missionId = rs.getInt("id");
            List<Integer> agentIds2 = getAgentIdsForMission(missionId);

            missions.add(new Mission(
                    missionId,
                    rs.getString("title"),
                    rs.getString("brief"),
                    rs.getString("date"),
                    rs.getInt("facility_id"),
                    agentIds2
            ));
        }
        rs.close();
        return missions;
    }

    public Mission getMissionById(int id) throws SQLException
    {
        String sql = "SELECT id, title, brief, date, facility_id FROM missions WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next())
        {
            List<Integer> agentIds2 = getAgentIdsForMission(id);
            Mission mission = new Mission(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("brief"),
                    rs.getString("date"),
                    rs.getInt("facility_id"),
                    agentIds2
            );
            rs.close();
            stmt.close();
            return mission;
        }

        rs.close();
        stmt.close();
        return null;
    }

    public void assignAgentToMission(int missionId, int agentId) throws SQLException
    {
        if (!agentExists(agentId))
        {
            throw new IllegalArgumentException("Agent does not exist: " + agentId);
        }

        String checkSql = "SELECT id FROM missions WHERE id = ?";
        PreparedStatement checkStmt = connection.prepareStatement(checkSql);
        checkStmt.setInt(1, missionId);
        ResultSet checkRs = checkStmt.executeQuery();
        if (!checkRs.next())
        {
            checkRs.close();
            checkStmt.close();
            throw new IllegalArgumentException("Mission does not exist: " + missionId);
        }
        checkRs.close();
        checkStmt.close();

        String dupSql = "SELECT mission_id FROM mission_agents WHERE mission_id = ? AND agent_id = ?";
        PreparedStatement dupStmt = connection.prepareStatement(dupSql);
        dupStmt.setInt(1, missionId);
        dupStmt.setInt(2, agentId);
        ResultSet dupRs = dupStmt.executeQuery();
        if (dupRs.next())
        {
            dupRs.close();
            dupStmt.close();
            return;
        }
        dupRs.close();
        dupStmt.close();

        String sql = "INSERT INTO mission_agents (mission_id, agent_id) VALUES (?, ?)";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, missionId);
        stmt.setInt(2, agentId);
        stmt.executeUpdate();
        stmt.close();
    }

    public void removeAgentFromMission(int missionId, int agentId) throws SQLException
    {
        String sql = "DELETE FROM mission_agents WHERE mission_id = ? AND agent_id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, missionId);
        stmt.setInt(2, agentId);
        stmt.executeUpdate();
        stmt.close();
    }

    public List<Agent> getAgentsForMission(int missionId) throws SQLException
    {
        List<Agent> agents = new ArrayList<>();
        String sql = "SELECT a.id, a.name, a.date_of_birth, a.date_of_death, a.notes, a.facility_id " +
                "FROM agents a INNER JOIN mission_agents ma ON a.id = ma.agent_id " +
                "WHERE ma.mission_id = ? ORDER BY a.id";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, missionId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next())
        {
            agents.add(new Agent(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("date_of_birth"),
                    rs.getString("date_of_death"),
                    rs.getString("notes"),
                    rs.getInt("facility_id")
            ));
        }
        rs.close();
        stmt.close();
        return agents;
    }

    public List<Mission> getMissionsForAgent(int agentId) throws SQLException
    {
        List<Mission> missions = new ArrayList<>();
        String sql = "SELECT m.id, m.title, m.brief, m.date, m.facility_id " +
                "FROM missions m INNER JOIN mission_agents ma ON m.id = ma.mission_id " +
                "WHERE ma.agent_id = ? ORDER BY m.id";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, agentId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next())
        {
            int mid = rs.getInt("id");
            List<Integer> mAgentIds = getAgentIdsForMission(mid);
            missions.add(new Mission(
                    mid,
                    rs.getString("title"),
                    rs.getString("brief"),
                    rs.getString("date"),
                    rs.getInt("facility_id"),
                    mAgentIds
            ));
        }
        rs.close();
        stmt.close();
        return missions;
    }

    private List<Integer> getAgentIdsForMission(int missionId) throws SQLException
    {
        List<Integer> agentIds = new ArrayList<>();
        String sql = "SELECT agent_id FROM mission_agents WHERE mission_id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, missionId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
        {
            agentIds.add(rs.getInt("agent_id"));
        }
        rs.close();
        stmt.close();
        return agentIds;
    }

    @Override
    public List<Mission> searchMissions(String phrase){
        List<Mission> missions = new ArrayList<Mission>();

        if (phrase == null || phrase.trim().isEmpty())
        {
            return missions;
        }

        try {
            String search = """
                    SELECT DISTINCT m.id, m.title, m.brief, m.date, m.facility_id
                    FROM missions m
                    LEFT JOIN facilities f ON m.facility_id = f.id
                    LEFT JOIN mission_agents ma ON ma.mission_id = m.id
                    LEFT JOIN agents a ON a.id = ma.agent_id
                    WHERE LOWER(COALESCE(m.title, '')) LIKE ?
                       OR LOWER(COALESCE(m.brief, '')) LIKE ?
                       OR LOWER(COALESCE(m.date, '')) LIKE ?
                       OR LOWER(COALESCE(f.name, '')) LIKE ?
                       OR LOWER(COALESCE(f.abbreviation, '')) LIKE ?
                       OR LOWER(COALESCE(a.name, '')) LIKE ?
                       OR LOWER(COALESCE(a.date_of_birth, '')) LIKE ?
                       OR LOWER(COALESCE(a.date_of_death, '')) LIKE ?
                       OR LOWER(COALESCE(a.notes, '')) LIKE ?
                    ORDER BY m.id
                    """;
            PreparedStatement statement = connection.prepareStatement(search);
            String value = "%" + phrase.toLowerCase() + "%";
            statement.setString(1, value);
            statement.setString(2, value);
            statement.setString(3, value);
            statement.setString(4, value);
            statement.setString(5, value);
            statement.setString(6, value);
            statement.setString(7, value);
            statement.setString(8, value);
            statement.setString(9, value);
            ResultSet results = statement.executeQuery();

            while (results.next())
            {
                int missionId = results.getInt("id");

                List<Integer> agentIds = new ArrayList<>();
                String agentSql = ("SELECT agent_id FROM mission_agents WHERE mission_id = ?");
                PreparedStatement agentStmt = connection.prepareStatement(agentSql);
                agentStmt.setInt(1, missionId);
                ResultSet agentRs = agentStmt.executeQuery();
                while (agentRs.next())
                {
                    agentIds.add(agentRs.getInt("agent_id"));
                }
                agentRs.close();
                agentStmt.close();

                missions.add(new Mission(
                        missionId,
                        results.getString("title"),
                        results.getString("brief"),
                        results.getString("date"),
                        results.getInt("facility_id"),
                        agentIds
                ));
            }
            results.close();
            statement.close();
        } catch (SQLException e){
            System.out.println("Search failed");
        }

        return missions;
    }

    public List<String> getAllTitles()
    {
        List<String> titles = new ArrayList<>();
        try
        {
            String sql = "SELECT title FROM missions ORDER BY id ASC";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            int number = 1;
            while (rs.next())
            {
                titles.add(number + ". " + rs.getString("title"));
                number++;
            }
            rs.close();
        } catch (SQLException e)
        {
            System.out.println("Error retrieving titles");
        }
        return titles;
    }

    public String getMissionByNumber(int number)
    {
        return getMissionByNumber(number, DEFAULT_AUDIT_USERNAME);
    }

    public String getMissionByNumber(int number, String username)
    {
        try
        {
            String sql = "SELECT title, brief, date FROM missions WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, number);
            ResultSet rs = statement.executeQuery();

            if (rs.next())
            {
                String mission = rs.getString("title") +
                        ": " + rs.getString("brief") +
                        " (" + rs.getString("date") + ")";
                rs.close();
                statement.close();
                if (auditLog != null) {
                    try {
                        auditLog.recordRead(
                                AuditLog.TYPE_BRIEF,
                                String.valueOf(number),
                                username == null || username.trim().isEmpty() ? DEFAULT_AUDIT_USERNAME : username
                        );
                    } catch (SQLException e) {
                        System.out.println("Audit log failed");
                    }
                }
                return mission;
            } else
            {
                rs.close();
                statement.close();
                return null;
            }
        } catch (SQLException e)
        {
            System.out.println("Error retrieving mission");
            return null;
        }
    }

    public String formatResults(List<Mission> missions)
    {
        if(missions.isEmpty())
        {
            return "No matches found.";
        }

        StringBuilder sb = new StringBuilder();
        for (Mission m : missions)
        {
            sb.append(m.getTitle())
                    .append(": ")
                    .append(" (")
                    .append(m.getDate())
                    .append(")")
                    .append(System.lineSeparator());
        }
        return sb.toString().trim();
    }


    public void loadFacilities(String filename) {
        FileHandler fh = new FileHandler();
        String content = fh.handle(filename);

        if (content.equals("File Not Found.") || content.equals("Insufficient File")) {
            System.out.println("Cannot load facilities from " + filename);
            return;
        }

        String[] lines = content.split("\n");

        for (String line : lines) {
            String[] data = line.split("\t");

            if (data.length >= 2) {
                try {
                    String name = data[0].trim();
                    String abbreviation = data[1].trim();

                    String sql = "INSERT OR IGNORE INTO facilities (name, abbreviation) VALUES (?, ?)";
                    PreparedStatement ps = connection.prepareStatement(sql);
                    ps.setString(1, name);
                    ps.setString(2, abbreviation);
                    ps.executeUpdate();

                } catch (SQLException e) {
                    System.out.println("Error adding facility: " + data[0]);
                }
            }
        }
    }


    public void loadCountries(String filename)
    {
        FileHandler fh = new FileHandler();
        String content = fh.handle(filename);

        if (content.equals("File Not Found.") || content.equals("Insufficient File"))
        {
            System.out.println("Could not load countries from " + filename);
            return;
        }

        String[] lines = content.split("\n");

        for (int i = 1; i < lines.length; i++) // skip header
        {
            String name = lines[i].trim();

            if (!name.isEmpty())
            {
                try
                {
                    addCountry(name);
                }
                catch (SQLException e)
                {
                    System.out.println("Error adding country: " + name);
                }
            }
        }
    }


    public boolean facilityDelete(int id) throws SQLException
    {
        // block delete if agents exist
        String agentSQL = "SELECT id FROM agents WHERE facility_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(agentSQL))
        {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                return false;
            }
        }

        // block delete if missions exist
        String missionSQL = "SELECT id FROM missions WHERE facility_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(missionSQL))
        {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                return false;
            }
        }

        // perform delete
        String deleteSQL = "DELETE FROM facilities WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSQL))
        {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean agentDelete(int id) throws SQLException
    {
        // check if agent is used in missions
        String sql = "SELECT agent_id FROM mission_agents WHERE agent_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql))
        {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                return false;
            }
        }

        // delete agent
        String deleteSQL = "DELETE FROM agents WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSQL))
        {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public void markMessageAsRead(int messageId) throws SQLException
    {
        String sql = "UPDATE agent_messages SET is_read = 1 WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql))
        {
            stmt.setInt(1, messageId);
            stmt.executeUpdate();
        }
    }

    public Message getMessageById(int id) throws SQLException
    {
        String sql = "SELECT id, agent_id, body, sender_username, is_read FROM agent_messages WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Message(
                            rs.getInt("id"),
                            rs.getInt("agent_id"),
                            rs.getString("body"),
                            rs.getString("sender_username"),
                            rs.getInt("is_read") == 1 // Converts 1 to true, 0 to false
                    );
                }
            }
        }
        return null;
    }

    public void addCountry(String name) throws SQLException
    {
        String sql = "INSERT OR IGNORE INTO countries (name) VALUES (?)";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.executeUpdate();
        stmt.close();
    }


    public List<Country> getAllCountries() throws SQLException {
        String sql = "SELECT id, name FROM countries ORDER BY name";
        ResultSet rs = connection.createStatement().executeQuery(sql);

        List<Country> countries = new ArrayList<>();

        while (rs.next()) {
            countries.add(new Country(
                    rs.getInt("id"),
                    rs.getString("name")
            ));
        }

        rs.close();
        return countries;
    }
}