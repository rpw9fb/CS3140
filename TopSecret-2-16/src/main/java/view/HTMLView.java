package view;

import models.Agent;
import models.Facility;
import models.Mission;
import models.Country;

import java.util.List;
import java.util.Map;

public class HTMLView {

    // Login Page
    public String loginPage(String error) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Login</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(false));
        sb.append("<div class='container'>");
        sb.append("<h2>Login</h2>");
        if (error != null) {
            sb.append("<p style='color:red;'>").append(error).append("</p>");
        }
        sb.append("<form method='POST' action='/login'>")
                .append("Username: <input type='text' name='username'/><br/>")
                .append("Password: <input type='password' name='password'/><br/>")
                .append("<input type='submit' value='Login'/>")
                .append("</form></body></html>");
        return sb.toString();
    }

    // Main Menu
    public String mainMenu(boolean isAdmin) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Main Menu</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(isAdmin));
        sb.append("<div class='container'>");
        sb.append("<h2>Main Menu</h2>");
        sb.append("<ul>");
        sb.append("<li><a href='/briefs'>Mission Briefs</a></li>");
        sb.append("<li><a href='/agents'>Agents</a></li>");
        sb.append("<li><a href='/facilities'>Facilities</a></li>");
        sb.append("<li><a href='/messages'>Messaging Center</a></li>");
        if (isAdmin) {
            sb.append("<li><a href='/admin/users'>Manage Users</a></li>");
            sb.append("<li><a href='/audit'>Review Audit Log</a></li>");
        }
        sb.append("<li><a href='/logout'>Logout</a></li>");
        sb.append("</ul>");
        sb.append("</body></html>");
        return sb.toString();
    }

    // Mission List & Details
    public String missionList(List<Mission> missions, boolean isAdmin) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Mission Briefs</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(isAdmin));
        sb.append("<div class='container'>");
        sb.append("<h2>Available Mission Briefs</h2>");
        if (isAdmin) {
            sb.append("<p><a href='/briefs/create'>[+] Create New Mission</a></p>");
        }
        if (missions == null || missions.isEmpty()) {
            sb.append("<p>No missions available.</p>");
        } else {
            sb.append("<ul>");
            for (Mission m : missions) {
                sb.append("<li><a href='/briefs/").append(m.getId()).append("'>")
                        .append(m.getTitle()).append("</a>");
                if (isAdmin) {
                    sb.append(" <a href='/briefs/edit/").append(m.getId()).append("'>[Edit]</a>");
                }
                sb.append("</li>");
            }
            sb.append("</ul>");
        }
        sb.append("<p><a href='/search'>Search Missions</a></p>");
        sb.append("<p><a href='/'>Back to Main Menu</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    public String missionList(List<String> titles) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Mission Briefs</title></head><body>");
        sb.append("<h2>Available Mission Briefs</h2>");
        if (titles == null || titles.isEmpty()) {
            sb.append("<p>No missions available.</p>");
        } else {
            sb.append("<ul>");
            for (int i = 0; i < titles.size(); i++) {
                sb.append("<li><a href='/briefs/").append(i + 1).append("'>")
                        .append(titles.get(i)).append("</a></li>");
            }
            sb.append("</ul>");
        }
        sb.append("<p><a href='/search'>Search Missions</a></p>");
        sb.append("<p><a href='/'>Back to Main Menu</a></p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    public String missionDetail(String missionText) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Mission Detail</title></head><body>");
        sb.append("<h2>Mission Detail</h2>");
        if (missionText == null || missionText.isEmpty()) {
            sb.append("<p>Mission not found.</p>");
        } else {
            sb.append("<pre>").append(missionText).append("</pre>");
        }
        sb.append("<p><a href='/briefs'>Back to Mission List</a></p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    public String missionDetail(Mission mission, Facility facility, List<Agent> agents) {
        return missionDetail(mission, facility, agents, false);
    }

    public String missionDetail(Mission mission, Facility facility, List<Agent> agents, boolean isAdmin) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Mission Detail</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(isAdmin));
        sb.append("<div class='container'>");
        sb.append("<h2>Mission Detail</h2>");
        if (mission == null) {
            sb.append("<p>Mission not found.</p>");
        } else {
            if (isAdmin) {
                sb.append("<p><a href='/briefs/edit/").append(mission.getId()).append("'>[Edit Mission]</a></p>");
            }
            sb.append("<p><strong>Title:</strong> ").append(mission.getTitle()).append("</p>");
            sb.append("<p><strong>Date:</strong> ").append(mission.getDate()).append("</p>");
            sb.append("<p><strong>Brief:</strong> ").append(mission.getBrief()).append("</p>");
            if (facility != null) {
                sb.append("<p><strong>Facility:</strong> <a href='/facilities/")
                        .append(facility.getID())
                        .append("'>")
                        .append(facility.getName())
                        .append(" (")
                        .append(facility.getAbbreviation())
                        .append(")</a></p>");
            }

            sb.append("<h3>Assigned Agents</h3>");
            if (agents == null || agents.isEmpty()) {
                sb.append("<p>None</p>");
            } else {
                sb.append("<ul>");
                for (Agent a : agents) {
                    sb.append("<li><a href='/agents/")
                            .append(a.getId())
                            .append("'>")
                            .append(a.getName())
                            .append("</a></li>");
                }
                sb.append("</ul>");
            }
        }
        sb.append("<p><a href='/briefs'>Back to Mission List</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    // Mission Form (Create/Edit)
    public String missionForm(Mission mission, List<Facility> facilities, List<Agent> allAgents, boolean isAdmin) {
        boolean isEdit = mission != null;
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>").append(isEdit ? "Edit" : "Create").append(" Mission</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(isAdmin));
        sb.append("<div class='container'>");
        sb.append("<h2>").append(isEdit ? "Edit" : "Create").append(" Mission</h2>");
        sb.append("<form method='POST' action='/briefs/").append(isEdit ? "edit/" + mission.getId() : "create").append("'>");
        
        sb.append("Title: <input type='text' name='title' value='").append(isEdit ? mission.getTitle() : "").append("' required/><br/>");
        sb.append("Date: <input type='text' name='date' value='").append(isEdit ? mission.getDate() : "").append("' required/><br/>");
        sb.append("Brief: <textarea name='brief' rows='5' required>").append(isEdit ? mission.getBrief() : "").append("</textarea><br/>");
        
        sb.append("Facility: <select name='facilityId' required>");
        for (Facility f : facilities) {
            sb.append("<option value='").append(f.getID()).append("'")
                    .append(isEdit && f.getID() == mission.getFacilityId() ? " selected" : "")
                    .append(">").append(f.getName()).append("</option>");
        }
        sb.append("</select><br/><br/>");
        
        sb.append("Assign Agents (Ctrl+Click to select multiple):<br/>");
        sb.append("<select name='agentIds' multiple size='10' required>");
        for (Agent a : allAgents) {
            sb.append("<option value='").append(a.getId()).append("'")
                    .append(isEdit && mission.getAgentIds().contains(a.getId()) ? " selected" : "")
                    .append(">").append(a.getName()).append("</option>");
        }
        sb.append("</select><br/><br/>");
        
        sb.append("<input type='submit' value='").append(isEdit ? "Update" : "Create").append(" Mission'/>");
        sb.append("</form>");
        sb.append("<p><a href='/briefs'>Cancel</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    // Mission Search Page
    public String missionSearchPage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Search Missions</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(false));
        sb.append("<div class='container'>");
        sb.append("<h2>Search Missions</h2>");
        sb.append("<form method='GET' action='/search/results'>")
                .append("Search phrase: <input type='text' name='q'/>")
                .append("<input type='submit' value='Search'/>")
                .append("</form>");
        sb.append("<p><a href='/briefs'>Back to Mission List</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    public String missionSearchResults(List<Mission> matches, String phrase) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Search Results</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(false));
        sb.append("<div class='container'>");
        sb.append("<h2>Search Results for: ").append(phrase).append("</h2>");
        if (matches == null || matches.isEmpty()) {
            sb.append("<p>No matches found.</p>");
        } else {
            sb.append("<ul>");
            for (Mission m : matches) {
                sb.append("<li><a href='/briefs/").append(m.getId()).append("'>")
                        .append(m.getTitle()).append(" (").append(m.getDate()).append(")</a></li>");
            }
            sb.append("</ul>");
        }
        sb.append("<p><a href='/search'>New Search</a></p>");
        sb.append("<p><a href='/briefs'>Back to Mission List</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    // Agents
    public String agentList(List<Agent> agents, boolean isAdmin) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Agents</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(isAdmin));
        sb.append("<div class='container'>");
        sb.append("<h2>Agents</h2>");
        if (isAdmin) {
            sb.append("<p><a href='/agents/create'>[+] Create New Agent</a></p>");
        }
        if (agents == null || agents.isEmpty()) {
            sb.append("<p>No agents available.</p>");
        } else {
            sb.append("<ul>");
            for (Agent a : agents) {
                sb.append("<li><a href='/agents/").append(a.getId()).append("'>")
                        .append(a.getName()).append("</a>");
                if (isAdmin) {
                    sb.append(" <a href='/agents/edit/").append(a.getId()).append("'>[Edit]</a>");
                }
                sb.append("</li>");
            }
            sb.append("</ul>");
        }
        sb.append("<p><a href='/'>Back to Main Menu</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    public String agentList(List<Agent> agents) {
        return agentList(agents, false);
    }

    public String agentDetail(Agent agent) {
        return agentDetail(agent, null, null, false);
    }

    public String agentDetail(Agent agent, Facility facility, List<Mission> missions) {
        return agentDetail(agent, facility, missions, false);
    }

    public String agentDetail(Agent agent, Facility facility, List<Mission> missions, boolean isAdmin) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Agent Detail</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(isAdmin));
        sb.append("<div class='container'>");
        sb.append("<h2>Agent Detail</h2>");
        if (agent == null) {
            sb.append("<p>Agent not found.</p>");
        } else {
            if (isAdmin) {
                sb.append("<p><a href='/agents/edit/").append(agent.getId()).append("'>[Edit Agent]</a></p>");
            }
            sb.append("<p><strong>ID:</strong> ").append(agent.getId()).append("</p>");
            sb.append("<p><strong>Name:</strong> ").append(agent.getName()).append("</p>");
            sb.append("<p><strong>DOB:</strong> ").append(agent.getDateOfBirth()).append("</p>");
            sb.append("<p><strong>DOD:</strong> ").append(agent.getDateOfDeath()).append("</p>");
            sb.append("<p><strong>Notes:</strong> ").append(agent.getNotes()).append("</p>");

            if (facility != null) {
                sb.append("<p><strong>Facility:</strong> <a href='/facilities/")
                        .append(facility.getID())
                        .append("'>")
                        .append(facility.getName())
                        .append(" (")
                        .append(facility.getAbbreviation())
                        .append(")</a></p>");
            } else {
                sb.append("<p><strong>Facility ID:</strong> ").append(agent.getFacilityId()).append("</p>");
            }

            sb.append("<h3>Assigned Missions</h3>");
            if (missions == null || missions.isEmpty()) {
                sb.append("<p>None</p>");
            } else {
                sb.append("<ul>");
                for (Mission m : missions) {
                    sb.append("<li><a href='/briefs/")
                            .append(m.getId())
                            .append("'>")
                            .append(m.getTitle())
                            .append("</a></li>");
                }
                sb.append("</ul>");
            }
        }
        sb.append("<p><a href='/agents'>Back to Agent List</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    // Agent Form (Create/Edit)
    public String agentForm(Agent agent, List<Facility> facilities, boolean isAdmin) {
        boolean isEdit = agent != null;
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>").append(isEdit ? "Edit" : "Create").append(" Agent</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(isAdmin));
        sb.append("<div class='container'>");
        sb.append("<h2>").append(isEdit ? "Edit" : "Create").append(" Agent</h2>");
        sb.append("<form method='POST' action='/agents/").append(isEdit ? "edit/" + agent.getId() : "create").append("'>");
        
        sb.append("Name: <input type='text' name='name' value='").append(isEdit ? agent.getName() : "").append("' required/><br/>");
        sb.append("DOB (YYYY-MM-DD): <input type='text' name='dob' value='").append(isEdit ? agent.getDateOfBirth() : "").append("'/><br/>");
        sb.append("DOD (YYYY-MM-DD): <input type='text' name='dod' value='").append(isEdit ? agent.getDateOfDeath() : "").append("'/><br/>");
        sb.append("Notes: <textarea name='notes'>").append(isEdit ? agent.getNotes() : "").append("</textarea><br/>");
        
        sb.append("Facility: <select name='facilityId' required>");
        for (Facility f : facilities) {
            sb.append("<option value='").append(f.getID()).append("'")
                    .append(isEdit && f.getID() == agent.getFacilityId() ? " selected" : "")
                    .append(">").append(f.getName()).append("</option>");
        }
        sb.append("</select><br/><br/>");
        
        sb.append("<input type='submit' value='").append(isEdit ? "Update" : "Create").append(" Agent'/>");
        sb.append("</form>");
        sb.append("<p><a href='/agents'>Cancel</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    // Facilities
    public String facilityList(List<Facility> facilities, boolean isAdmin) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Facilities</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(isAdmin));
        sb.append("<div class='container'>");
        sb.append("<h2>Facilities</h2>");
        if (isAdmin) {
            sb.append("<p><a href='/facilities/create'>[+] Create New Facility</a></p>");
        }
        if (facilities == null || facilities.isEmpty()) {
            sb.append("<p>No facilities available.</p>");
        } else {
            sb.append("<ul>");
            for (Facility f : facilities) {
                sb.append("<li><a href='/facilities/").append(f.getID()).append("'>")
                        .append(f.getName())
                        .append(" (").append(f.getAbbreviation()).append(")")
                        .append("</a>");
                if (isAdmin) {
                    sb.append(" <a href='/facilities/edit/").append(f.getID()).append("'>[Edit]</a>");
                }
                sb.append("</li>");
            }
            sb.append("</ul>");
        }
        sb.append("<p><a href='/'>Back to Main Menu</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    public String facilityList(List<Facility> facilities) {
        return facilityList(facilities, false);
    }

    public String facilityDetail(Facility facility, List<Agent> agents) {
        return facilityDetail(facility, agents, null, false);
    }

    public String facilityDetail(Facility facility, List<Agent> agents, List<Mission> missions) {
        return facilityDetail(facility, agents, missions, false);
    }

    public String facilityDetail(Facility facility, List<Agent> agents, List<Mission> missions, boolean isAdmin) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Facility Detail</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(isAdmin));
        sb.append("<div class='container'>");
        sb.append("<h2>Facility Detail</h2>");
        if (facility == null) {
            sb.append("<p>Facility not found.</p>");
        } else {
            if (isAdmin) {
                sb.append("<p><a href='/facilities/edit/").append(facility.getID()).append("'>[Edit Facility]</a></p>");
            }
            sb.append("<pre>")
                    .append("ID: ").append(facility.getID()).append("\n")
                    .append("Name: ").append(facility.getName()).append("\n")
                    .append("Abbreviation: ").append(facility.getAbbreviation()).append("\n")
                    .append("Country: ")
                    .append(facility.getCountryName() == null ? "No Country" : facility.getCountryName())
                    .append("\n")
                    .append("</pre>");
            if (agents != null && !agents.isEmpty()) {
                sb.append("<h3>Agents at this Facility</h3><ul>");
                for (Agent a : agents) {
                    sb.append("<li><a href='/agents/")
                            .append(a.getId())
                            .append("'>")
                            .append(a.getName())
                            .append("</a></li>");
                }
                sb.append("</ul>");
            }
            if (missions != null && !missions.isEmpty()) {
                sb.append("<h3>Missions at this Facility</h3><ul>");
                for (Mission m : missions) {
                    sb.append("<li><a href='/briefs/")
                            .append(m.getId())
                            .append("'>")
                            .append(m.getTitle())
                            .append("</a></li>");
                }
                sb.append("</ul>");
            }
        }
        sb.append("<p><a href='/facilities'>Back to Facility List</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    // Facility Form (Create/Edit)
    public String facilityForm(Facility facility, List<Country> countries, boolean isAdmin) {
        boolean isEdit = facility != null;
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>").append(isEdit ? "Edit" : "Create").append(" Facility</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(isAdmin));
        sb.append("<div class='container'>");
        sb.append("<h2>").append(isEdit ? "Edit" : "Create").append(" Facility</h2>");
        sb.append("<form method='POST' action='/facilities/").append(isEdit ? "edit/" + facility.getID() : "create").append("'>");
        
        sb.append("Name: <input type='text' name='name' value='").append(isEdit ? facility.getName() : "").append("' required/><br/>");
        sb.append("Abbreviation: <input type='text' name='abbreviation' value='").append(isEdit ? facility.getAbbreviation() : "").append("' required/><br/>");
        
        sb.append("Country: <select name='countryId'>");
        sb.append("<option value=''>--- None ---</option>");
        for (Country c : countries) {
            sb.append("<option value='").append(c.getId()).append("'")
                    .append(isEdit && facility.getCountryID() != null && facility.getCountryID() == c.getId() ? " selected" : "")
                    .append(">").append(c.getName()).append("</option>");
        }
        sb.append("</select><br/><br/>");
        
        sb.append("<input type='submit' value='").append(isEdit ? "Update" : "Create").append(" Facility'/>");
        sb.append("</form>");
        sb.append("<p><a href='/facilities'>Cancel</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    // Audit Log
    public String auditLog(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Audit Log</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(true));
        sb.append("<div class='container'>");
        sb.append("<h2>Audit Log</h2>");
        if (lines == null || lines.isEmpty()) {
            sb.append("<p>No audit entries.</p>");
        } else {
            sb.append("<ul>");
            for (String line : lines) {
                sb.append("<li>").append(line).append("</li>");
            }
            sb.append("</ul>");
        }
        sb.append("<p><a href='/'>Back to Main Menu</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    public String agentInbox(List<models.Message> messages, String currentUser)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>My Inbox</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(false));
        sb.append("<div class='container'>");
        sb.append("<h2>Messaging Center for ").append(currentUser).append("</h2>");
        sb.append("<p><a href='/messages/send'>+ Send New Message</a></p>");

        if (messages == null || messages.isEmpty())
        {
            sb.append("<p>No messages found. </p>");
        } else {
            sb.append("<table border='1' cellpadding='5' cellspacing='0' style='width:100%;'>");
            sb.append("<tr style='background-color:#eee;'><th>Status</th><th>From</th><th>Message Preview</th><th>Action</th></tr>");

            for (models.Message m : messages)
            {
                String status = m.isRead() ? "<span style='color:gray;'>Read</span>" : "<strong>NEW</strong>";
                String preview = m.getBody().length() > 30 ? m.getBody().substring(0, 27) + "..." : m.getBody();
                sb.append("<tr>");
                sb.append("<td>").append(status).append("</td>");
                sb.append("<td>").append(m.getSenderUsername()).append("</td>");
                sb.append("<td>").append(preview).append("</td>");
                sb.append("<td><a href='/messages/view?id=").append(m.getId()).append("'>Open</a></td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
        }
        sb.append("<p><a href = '/' > Back to Main Menu</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    public String sendMessagePage(List<models.Agent> agents)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Send Message</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(false));
        sb.append("<div class='container'>");
        sb.append("<h2>Send a New Message</h2>");
        sb.append("<form method='POST' action='/messages/send'>");
        sb.append("To Agent: <select name='recipientId' required>");
        sb.append("<option value=''> --- Select an Agent --- </option>");

        for (models.Agent a : agents)
        {
            sb.append("<option value='").append(a.getId()).append("'>")
                .append(a.getName()).append(" (ID: ").append(a.getId()).append(")")
                .append("</option>");
        }

        sb.append("</select><br/><br/>");
        sb.append("Message:<br/>");
        sb.append("<textarea name='body' rows='5' cols='40' required></textarea><br/><br/>");
        sb.append("<input type='submit' value='Send Message'/>");
        sb.append("</form>");

        sb.append("<p><a href = '/messages'>Back to Inbox</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    public String messageDetail(models.Message message)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>View Message</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(false));
        sb.append("<div class='container'>");
        sb.append("<h2>Message Detail</h2>");

        if (message == null)
        {
            sb.append("<p>Message not found.</p>");
        } else
        {
            sb.append("<div style='border:1px solid #ccc; padding:15px; background-color:#f9f9f9;'>");

            sb.append("<p><strong>From:</strong> ").append(message.getSenderUsername()).append("</p>");
            sb.append("<p><strong>Content:</strong></p>");
            sb.append("<p>").append(message.getBody()).append("</p>");
            sb.append("</div>");
        }

        sb.append("<p><a href='/messages'>Back to Inbox</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }
    
    // User Management (Admin only)
    public String userManagement(List<Map.Entry<String, String>> users, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Manage Users</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(true));
        sb.append("<div class='container'>");
        sb.append("<h2>Manage Users</h2>");
        if (message != null && !message.isEmpty()) {
            sb.append("<p style='color:green;'>").append(message).append("</p>");
        }
        if (users == null || users.isEmpty()) {
            sb.append("<p>No users found.</p>");
        } else {
            sb.append("<table border='1' cellpadding='5' cellspacing='0'>");
            sb.append("<tr><th>Username</th><th>Role</th><th>Action</th></tr>");
            for (Map.Entry<String, String> user : users) {
                sb.append("<tr>");
                sb.append("<td>").append(user.getKey()).append("</td>");
                sb.append("<td>").append(user.getValue()).append("</td>");
                sb.append("<td>");
                if (!user.getValue().equalsIgnoreCase("admin")) {
                    sb.append("<form method='POST' action='/admin/promote' style='margin:0;'>");
                    sb.append("<input type='hidden' name='username' value='").append(user.getKey()).append("'/>");
                    sb.append("<input type='submit' value='Promote to Admin'/>");
                    sb.append("</form>");
                } else {
                    sb.append("Admin");
                }
                sb.append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
        }
        sb.append("<p><a href='/'>Back to Main Menu</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    public String error(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Error</title>");
        sb.append(css());
        sb.append("</head><body>");
        sb.append(header(false));
        sb.append("<div class='container'>");
        sb.append("<h2>Error</h2>");
        sb.append("<p style='color:red;'>").append(message).append("</p>");
        sb.append("<p><a href='/'>Back to Main Menu</a></p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String header(boolean isAdmin) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div class='navbar'>");
        sb.append("<a href='/'>Home</a>");
        sb.append("<a href='/briefs'>Missions</a>");
        sb.append("<a href='/agents'>Agents</a>");
        sb.append("<a href='/facilities'>Facilities</a>");
        sb.append("<a href='/messages'>Messages</a>");

        if (isAdmin) {
            sb.append("<a href='/admin/users'>Admin</a>");
            sb.append("<a href='/audit'>Audit</a>");
        }

        sb.append("<a href='/logout' style='float:right;'>Logout</a>");
        sb.append("</div>");

        return sb.toString();
    }


    private String css() {
        return """
    <style>
        /* ===== Base ===== */
        body {
            margin: 0;
            font-family: "Courier New", Courier, monospace;
            background-color: #0b0f0b;
            color: #00ff66;
        }

        /* ===== Layout Container ===== */
        .container {
            max-width: 1000px;
            margin: 30px auto;
            padding: 25px;
            background: #0f1510;
            border: 1px solid #1f3a25;
            border-radius: 8px;
            box-shadow: 0 0 15px rgba(0, 255, 102, 0.08);
        }

        /* ===== Headings ===== */
        h2 {
            margin-top: 0;
            color: #00ff66;
            border-bottom: 1px solid #1f3a25;
            padding-bottom: 8px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        h3 {
            color: #00cc55;
            margin-top: 20px;
        }

        /* ===== Navbar ===== */
        .navbar {
            background-color: #050805;
            padding: 12px 20px;
            display: flex;
            align-items: center;
            border-bottom: 1px solid #1f3a25;
        }

        .navbar a {
            color: #00ff66;
            margin-right: 15px;
            text-decoration: none;
            font-weight: bold;
        }

        .navbar a:hover {
            color: #ffffff;
            text-shadow: 0 0 5px #00ff66;
        }

        .navbar a.logout {
            margin-left: auto;
            color: #ff4444;
        }

        /* ===== Links ===== */
        a {
            color: #00ff66;
            text-decoration: none;
        }

        a:hover {
            text-decoration: underline;
            text-shadow: 0 0 5px #00ff66;
        }

        /* ===== Lists ===== */
        ul {
            padding-left: 20px;
        }

        li {
            margin: 8px 0;
            padding: 8px;
            background: #0b120d;
            border: 1px solid #1f3a25;
            border-radius: 6px;
        }

        /* ===== Tables ===== */
        table {
            border-collapse: collapse;
            width: 100%;
            background: #0b120d;
            margin-top: 10px;
        }

        th {
            background: #050805;
            color: #00ff66;
            text-align: left;
        }

        th, td {
            padding: 10px;
            border: 1px solid #1f3a25;
        }

        tr:nth-child(even) {
            background: #0f1510;
        }

        /* ===== Forms ===== */
        input, textarea, select {
            padding: 8px;
            margin-top: 5px;
            margin-bottom: 10px;
            width: 100%;
            max-width: 400px;
            background: #0b120d;
            color: #00ff66;
            border: 1px solid #1f3a25;
            border-radius: 6px;
            font-family: monospace;
        }

        input[type="submit"] {
            background: #00ff66;
            color: #0b0f0b;
            border: none;
            cursor: pointer;
            width: auto;
            padding: 10px 15px;
            font-weight: bold;
        }

        input[type="submit"]:hover {
            background: #00cc55;
        }

        /* ===== Cards ===== */
        .card {
            background: #0b120d;
            padding: 15px;
            border-radius: 8px;
            border: 1px solid #1f3a25;
            margin-bottom: 15px;
        }

        /* ===== Utility ===== */
        .error {
            color: #ff4444;
        }

        .success {
            color: #00ff66;
        }

        /* ===== “Terminal glow” effect (subtle) ===== */
        body {
            text-shadow: 0 0 2px rgba(0, 255, 102, 0.2);
        }
    </style>
    """;
    }
}
