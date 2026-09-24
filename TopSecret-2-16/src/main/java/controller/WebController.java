package controller;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import models.*;
import view.HTMLView;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WebController {
    public HTMLView view = new HTMLView();
    public ProjectControl projectControl;
    public Password passwordHandler;
    public ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, Boolean> adminSessions = new ConcurrentHashMap<>();
    public SessionManager sm = new SessionManager();

    public WebController(Password password) {
        projectControl = new ProjectControl(new FileHandler());
        projectControl.setCurrentUsername(password.getCurrentUsername());
        passwordHandler = password;
    }


    public void run() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(3002), 0);
            server.createContext("/", this::handleRoot);
            server.createContext("/login", this::handleLogin);
            server.createContext("/logout", this::logoutResponse);
            server.createContext("/briefs", this::handleBriefs);
            server.createContext("/agents", this::handleAgents);
            server.createContext("/facilities", this::handleFacilities);
            server.createContext("/audit", this::handleAudit);
            server.createContext("/search", this::handleSearch);
            server.createContext("/search/results", this::handleSearchResults);
            server.createContext("/admin/users", this::handleAdminUsers);
            server.createContext("/admin/promote", this::handleAdminPromote);
            server.createContext("/messages", this::handleMessages);
            server.createContext("/messages/view", this::handleViewMessage);
            server.createContext("/messages/send", this::handleSendMessage);
            server.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleRoot(HttpExchange exchange) throws IOException {
        sendResponse(exchange, rootContextResponse(exchange));
    }

    public void handleLogin(HttpExchange exchange) throws IOException {
        sendResponse(exchange, loginResponse(exchange));
    }

    public void handleBriefs(HttpExchange exchange) throws IOException {
        sendResponse(exchange, briefsResponse(exchange));
    }

    public void handleAgents(HttpExchange exchange) throws IOException {
        sendResponse(exchange, agentsResponse(exchange));
    }

    public void handleFacilities(HttpExchange exchange) throws IOException {
        sendResponse(exchange, facilityResponse(exchange));
    }

    public void handleAudit(HttpExchange exchange) throws IOException {
        sendResponse(exchange, auditResponse(exchange));
    }

    public void handleSearch(HttpExchange exchange) throws IOException {
        sendResponse(exchange, searchResponse(exchange));
    }

    public void handleSearchResults(HttpExchange exchange) throws IOException {
        sendResponse(exchange, searchResultsResponse(exchange));
    }

    public void handleAdminUsers(HttpExchange exchange) throws IOException {
        sendResponse(exchange, adminUserResponse(exchange));
    }

    public void handleAdminPromote(HttpExchange exchange) throws IOException {
        sendResponse(exchange, adminPromoteResponse(exchange));
    }

    public void handleMessages(HttpExchange exchange) throws IOException {
        sendResponse(exchange, messagesResponse(exchange));
    }

    public void handleViewMessage(HttpExchange exchange) throws IOException {
        sendResponse(exchange, viewMessageResponse(exchange));
    }

    public void handleSendMessage(HttpExchange exchange) throws IOException {
        sendResponse(exchange, sendMessageResponse(exchange));
    }

    public String rootContextResponse(HttpExchange exchange) {
        String response;
        if (sm.checkSession(exchange, sessions)) {
            response = view.mainMenu(sm.isSessionAdmin(exchange, adminSessions));
        }
        else {
            response = view.loginPage(null);
        }
        return response;
    }


    public String briefsResponse(HttpExchange exchange) {
        if (!sm.checkSession(exchange, sessions)) {
            return view.loginPage(null);
        }

        boolean isAdmin = sm.isSessionAdmin(exchange, adminSessions);
        String path = exchange.getRequestURI().getPath();
        String response;

        try {
            if (path.equals("/briefs")) {
                List<Mission> missions = projectControl.getAllMissions();
                response = view.missionList(missions, isAdmin);
            } else if (path.equals("/briefs/create")) {
                if (!isAdmin) return view.error("Admin permissions required.");
                if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    Map<String, List<String>> params = parsePostParams(exchange);
                    String title = params.get("title").get(0);
                    String date = params.get("date").get(0);
                    String brief = params.get("brief").get(0);
                    int facilityId = Integer.parseInt(params.get("facilityId").get(0));
                    List<Integer> agentIds = params.get("agentIds").stream().map(Integer::parseInt).toList();
                    projectControl.addMission(title, brief, date, facilityId, agentIds);
                    redirect(exchange, "/briefs");
                    return "";
                }
                response = view.missionForm(null, projectControl.listFacilities(), projectControl.listAgents(), isAdmin);
            } else if (path.startsWith("/briefs/edit/")) {
                if (!isAdmin) return view.error("Admin permissions required.");
                int id = Integer.parseInt(path.substring("/briefs/edit/".length()));
                if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    Map<String, List<String>> params = parsePostParams(exchange);
                    String title = params.get("title").get(0);
                    String date = params.get("date").get(0);
                    String brief = params.get("brief").get(0);
                    int facilityId = Integer.parseInt(params.get("facilityId").get(0));
                    List<Integer> agentIds = params.get("agentIds").stream().map(Integer::parseInt).toList();
                    projectControl.updateMission(id, title, brief, date, facilityId, agentIds);
                    redirect(exchange, "/briefs/" + id);
                    return "";
                }
                response = view.missionForm(projectControl.getMissionById(id), projectControl.listFacilities(), projectControl.listAgents(), isAdmin);
            } else {
                String[] pathPieces = path.split("/");
                int id = Integer.parseInt(pathPieces[2]);
                Mission mission = projectControl.getMissionById(id);
                Facility facility = mission == null ? null : projectControl.retrieveFacility(mission.getFacilityId());
                List<Agent> agents = mission == null ? List.of() : projectControl.getAgentsForMission(id);
                response = view.missionDetail(mission, facility, agents, isAdmin);
            }
            return response;
        } catch (SQLException | IOException e) {
            return view.error("Error: " + e.getMessage());
        }
    }


    public String agentsResponse(HttpExchange exchange) {
        if (!sm.checkSession(exchange, sessions)) {
            return view.loginPage(null);
        }

        boolean isAdmin = sm.isSessionAdmin(exchange, adminSessions);
        String path = exchange.getRequestURI().getPath();
        String response;

        try {
            if (path.equals("/agents")) {
                List<Agent> agents = projectControl.listAgents();
                response = view.agentList(agents, isAdmin);
            } else if (path.equals("/agents/create")) {
                if (!isAdmin) return view.error("Admin permissions required.");
                if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    Map<String, List<String>> params = parsePostParams(exchange);
                    String name = params.get("name").get(0);
                    String dob = params.get("dob").get(0);
                    String dod = params.get("dod").get(0);
                    String notes = params.get("notes").get(0);
                    int facilityId = Integer.parseInt(params.get("facilityId").get(0));
                    projectControl.addAgent(name, dob, dod, notes, facilityId);
                    redirect(exchange, "/agents");
                    return "";
                }
                response = view.agentForm(null, projectControl.listFacilities(), isAdmin);
            } else if (path.startsWith("/agents/edit/")) {
                if (!isAdmin) return view.error("Admin permissions required.");
                int id = Integer.parseInt(path.substring("/agents/edit/".length()));
                if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    Map<String, List<String>> params = parsePostParams(exchange);
                    String name = params.get("name").get(0);
                    String dob = params.get("dob").get(0);
                    String dod = params.get("dod").get(0);
                    String notes = params.get("notes").get(0);
                    int facilityId = Integer.parseInt(params.get("facilityId").get(0));
                    projectControl.updateAgent(id, name, dob, dod, notes, facilityId);
                    redirect(exchange, "/agents/" + id);
                    return "";
                }
                response = view.agentForm(projectControl.retrieveAgent(id), projectControl.listFacilities(), isAdmin);
            } else {
                String[] pathPieces = path.split("/");
                int id = Integer.parseInt(pathPieces[2]);
                Agent agent = projectControl.retrieveAgent(id);
                Facility facility = agent == null ? null : projectControl.retrieveFacility(agent.getFacilityId());
                List<Mission> missions = agent == null ? List.of() : projectControl.getMissionsForAgent(id);
                response = view.agentDetail(agent, facility, missions, isAdmin);
            }
            return response;
        } catch (SQLException | IOException e) {
            return view.error("Error: " + e.getMessage());
        }
    }


    public String facilityResponse(HttpExchange exchange) {
        if (!sm.checkSession(exchange, sessions)) {
            return view.loginPage(null);
        }

        boolean isAdmin = sm.isSessionAdmin(exchange, adminSessions);
        String path = exchange.getRequestURI().getPath();
        String response;

        try {
            if (path.equals("/facilities")) {
                List<Facility> facilities = projectControl.listFacilities();
                response = view.facilityList(facilities, isAdmin);
            } else if (path.equals("/facilities/create")) {
                if (!isAdmin) return view.error("Admin permissions required.");
                if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    Map<String, List<String>> params = parsePostParams(exchange);
                    String name = params.get("name").get(0);
                    String abbreviation = params.get("abbreviation").get(0);
                    String countryIdStr = params.get("countryId").get(0);
                    Integer countryId = countryIdStr.isEmpty() ? null : Integer.parseInt(countryIdStr);
                    projectControl.addFacility(name, abbreviation, countryId);
                    redirect(exchange, "/facilities");
                    return "";
                }
                response = view.facilityForm(null, projectControl.listCountries(), isAdmin);
            } else if (path.startsWith("/facilities/edit/")) {
                if (!isAdmin) return view.error("Admin permissions required.");
                int id = Integer.parseInt(path.substring("/facilities/edit/".length()));
                if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    Map<String, List<String>> params = parsePostParams(exchange);
                    String name = params.get("name").get(0);
                    String abbreviation = params.get("abbreviation").get(0);
                    String countryIdStr = params.get("countryId").get(0);
                    Integer countryId = countryIdStr.isEmpty() ? null : Integer.parseInt(countryIdStr);
                    projectControl.updateFacility(id, name, abbreviation, countryId);
                    redirect(exchange, "/facilities/" + id);
                    return "";
                }
                response = view.facilityForm(projectControl.retrieveFacility(id), projectControl.listCountries(), isAdmin);
            } else {
                String[] pathPieces = path.split("/");
                int id = Integer.parseInt(pathPieces[2]);
                Facility facility = projectControl.retrieveFacility(id);
                List<Integer> agentIDs = projectControl.getAgentsAtFacility(id);
                List<Agent> agents = agentIDs.stream()
                        .map(projectControl::retrieveAgent)
                        .filter(Objects::nonNull)
                        .toList();
                List<Mission> missions = projectControl.getMissionsAtFacility(id);
                response = view.facilityDetail(facility, agents, missions, isAdmin);
            }

            return response;
        } catch (SQLException | IOException e) {
            return view.error("Error: " + e.getMessage());
        }
    }


    public String auditResponse(HttpExchange exchange) {
        if (!sm.checkSession(exchange, sessions) || !sm.isSessionAdmin(exchange, adminSessions)) {
            return view.loginPage(null);
        }

        return view.auditLog(projectControl.listAuditLogLines());
    }


    public String searchResponse(HttpExchange exchange) {
        if (!sm.checkSession(exchange, sessions)) {
            return view.loginPage(null);
        }

        return view.missionSearchPage();
    }


    public String searchResultsResponse(HttpExchange exchange) {
        if (!sm.checkSession(exchange, sessions)) {
            return view.loginPage(null);
        }
        String query = exchange.getRequestURI().getQuery();
        String phrase = "";
        if (query != null && query.startsWith("q=")) {
            phrase = java.net.URLDecoder.decode(query.substring(2), StandardCharsets.UTF_8);
        }
        List<Mission> matches = projectControl.searchMissions(phrase);
        return view.missionSearchResults(matches, phrase);
    }


    public String adminUserResponse(HttpExchange exchange) {
        if (!sm.checkSession(exchange, sessions) || !sm.isSessionAdmin(exchange, adminSessions)) {
            return view.loginPage(null);
        }
        java.util.List<java.util.Map.Entry<String, String>> users = passwordHandler.getAllUsers();
        return view.userManagement(users, null);
    }


    public String loginResponse(HttpExchange exchange) {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            return view.loginPage(null);
        }

        String body = null;
        try {
            body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String[] pairs = body.split("&");
        String username = "";
        String password = "";
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                String key = kv[0];
                String value = kv[1];
                if (key.equals("username")) username = value;
                if (key.equals("password")) password = value;
            }
        }

        if (passwordHandler.webAuth(username, password)) {
            String sessionId = UUID.randomUUID().toString();
            sessions.put(sessionId, username);
            adminSessions.put(sessionId, checkIfAdmin(username));
            projectControl.setCurrentUsername(username);
            projectControl.recordLogin(username);
            exchange.getResponseHeaders().add("Set-Cookie", "SESSIONID=" + sessionId + "; HttpOnly; Path=/");
            exchange.getResponseHeaders().add("Location", "/");
            try {
                exchange.sendResponseHeaders(302, -1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return "";
        } else {
            return view.error("The provided username and/or password was incorrect.");
        }
    }


    public void logoutResponse(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                cookie = cookie.trim();
                if (cookie.startsWith("SESSIONID=")) {
                    String sessionId = cookie.substring("SESSIONID=".length());
                    sessions.remove(sessionId);
                    adminSessions.remove(sessionId);
                }
            }
        }
        exchange.getResponseHeaders().add("Location", "/");
        try {
            exchange.sendResponseHeaders(302, -1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        exchange.close();
    }


    public String adminPromoteResponse(HttpExchange exchange) {
        if (!sm.checkSession(exchange, sessions) || !sm.isSessionAdmin(exchange, adminSessions)) {
            return view.loginPage(null);
        }
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.getResponseHeaders().add("Location", "/admin/users");
            try {
                exchange.sendResponseHeaders(302, -1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            exchange.close();
            return "";
        }

        String body = null;
        try {
            body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String targetUsername = "";
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equals("username")) {
                targetUsername = java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }

        String message;
        if (passwordHandler.promoteToAdmin(targetUsername)) {
            String actorUsername = getSessionUsername(exchange);
            projectControl.recordUserRoleChange(targetUsername, actorUsername);
            message = "User '" + targetUsername + "' has been promoted to admin.";
        } else {
            message = "Failed to promote user '" + targetUsername + "'. User may not exist or is already an admin.";
        }

        java.util.List<java.util.Map.Entry<String, String>> users = passwordHandler.getAllUsers();
        return view.userManagement(users, message);
    }


    public void sendResponse(HttpExchange exchange, String response) {
        if (response == null || response.isEmpty()) { return; }
        try {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
            exchange.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public boolean checkIfAdmin(String username) {
        try {
            return passwordHandler.isAdminUser(username);
        } catch (Exception e) {
            return false;
        }
    }


    private String getSessionUsername(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                cookie = cookie.trim();
                if (cookie.startsWith("SESSIONID=")) {
                    String sessionId = cookie.substring("SESSIONID=".length());
                    return sessions.get(sessionId);
                }
            }
        }
        return null;
    }

    public String messagesResponse(HttpExchange exchange)
    {
        if (!sm.checkSession(exchange, sessions))
        {
            return view.loginPage(null);
        }
        try
        {
            String currentUser = getSessionUsername(exchange);
            projectControl.setCurrentUsername(currentUser);
            List<Message> inbox = projectControl.getAgentMessagesForCurrentUser();
            return view.agentInbox(inbox, currentUser);
        } catch (SQLException e) {
            return view.error("Could not load messages. ");
        }
    }

    public String viewMessageResponse(HttpExchange exchange)
    {
        if (!sm.checkSession(exchange, sessions))
        {
            return view.loginPage(null);
        }
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.contains("id="))
        {
            return view.error("No message ID provided.");
        }
        try
        {
            int msgId = Integer.parseInt(query.split("=")[1]);
            Message message = projectControl.getMessageById(msgId);
            if (message == null)
            {
                return view.error("Message not found.");
            }
            String currentUser = getSessionUsername(exchange);
            List<Agent> agents = projectControl.listAgents();
            Agent recipient = agents.stream()
                    .filter(a -> a.getId() == message.getRecipientAgentId())
                    .findFirst()
                    .orElse(null);
            if (recipient == null || !recipient.getName().equalsIgnoreCase(currentUser))
            {
                return view.error("You do not have permission to view this message.");
            }
            projectControl.markMessageAsRead(msgId);
            return view.messageDetail(projectControl.getMessageById(msgId));
        } catch (SQLException e)
        {
            return view.error("Error reading message. ");
        }
    }

    public String sendMessageResponse(HttpExchange exchange)
    {
        if (!sm.checkSession(exchange, sessions))
        {
            return view.loginPage(null);
        }
        if (exchange.getRequestMethod().equalsIgnoreCase("POST"))
        {
            try
            {
                Map<String, List<String>> params = parsePostParams(exchange);
                int recipientId = Integer.parseInt(params.get("recipientId").get(0));
                String messageBody = params.get("body").get(0);
                String senderUsername = getSessionUsername(exchange);
                projectControl.addAgentMessage(recipientId, messageBody, senderUsername);
                redirect(exchange, "/messages");
                return "";
            }
            catch (IOException | SQLException e)
            {
                return view.error("Error sending message: " + e.getMessage());
            }
        }
        try
        {
            List<Agent> allAgents = projectControl.listAgents();
            return view.sendMessagePage(allAgents);
        }
        catch (SQLException e)
        {
            return view.error("Error loading agent list. ");
        }
    }

    private Map<String, List<String>> parsePostParams(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, List<String>> params = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = java.net.URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }
        return params;
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().add("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }
}
