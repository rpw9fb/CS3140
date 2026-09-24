package view;

import models.*;

import java.util.List;

public class TerminalView
{
    // Main Menu
    public String mainMenu(boolean isAdmin)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\nMain Menu\n");
        sb.append("B - Mission Briefs\n");
        sb.append("A - Agents\n");
        sb.append("F - Facilities\n");
        sb.append("G - Messages\n");
        if (isAdmin)
        {
            sb.append("P - Promote User to Admin\n");
            sb.append("R - Review Audit Log\n");
        }
        sb.append("X - Exit\n");
        sb.append("Choice: ");
        return sb.toString();
    }

    // Missions
    public String missionsMenu()
    {
        return "\nMission Briefs\n" +
                "L - List all Briefs\n" +
                "S - Search Briefs\n" +
                "# - Enter Number to Read a Brief\n" +
                "N - Add New Brief\n" +
                "M - Return to Main Menu\n" +
                "Choice: ";
    }

    public String missionList(List<String> titles)
    {
        if (titles == null || titles.isEmpty())
        {
            return "No missions available.";
        }
        StringBuilder sb = new StringBuilder("\nAvailable Mission Briefs:\n");
        for (String title : titles)
        {
            sb.append(title).append("\n");
        }
        return sb.toString().trim();
    }

    public String missionDetail(String missionText)
    {
        if (missionText == null)
        {
            return "Invalid mission number.";
        }
        return missionText;
    }

    public String missionSearchResults(List<Mission> matches, String phrase)
    {
        if (matches == null || matches.isEmpty())
        {
            return "No matches found.";
        }
        StringBuilder sb = new StringBuilder();
        for (Mission m : matches)
        {
            sb.append(m.getTitle()).append(": (").append(m.getDate()).append(")\n");
        }
        return sb.toString().trim();
    }

    public String missionAdded(List<String> updatedTitles)
    {
        return "Mission added Successfully. \n\nUpdated Mission Briefs:\n" + missionList(updatedTitles);
    }

    // Agents
    public String agentsMenu()
    {
        return "\nAgents\n" +
                "L - List all Agents\n" +
                "# - Enter ID to read Agent Details\n" +
                "N - Add New Agent\n" +
                "M - Return to Main Menu\n" +
                "Choice: ";
    }

    public String agentList(List<Agent> agents)
    {
        if (agents == null || agents.isEmpty())
        {
            return "No agents available.";
        }
        StringBuilder sb = new StringBuilder("\nAll Agents:\n");
        for (Agent a : agents)
        {
            sb.append(a).append("\n");
        }
        return sb.toString().trim();
    }

    public String agentDetail(Agent agent)
    {
        if (agent == null)
        {
            return "Agent not found.";
        }
        return "\nAgent Details:\n" +
                "ID: " + agent.getId() + "\n" +
                "Name: " + agent.getName() + "\n" +
                "Date of Birth: " + agent.getDateOfBirth() + "\n" +
                "Date of Death: " + agent.getDateOfDeath() + "\n" +
                "Notes: " + agent.getNotes() + "\n" +
                "Facility ID: " + agent.getFacilityId() + "\n";
    }

    public String agentAdded(List<Agent> updatedAgents)
    {
        return "Agent added successfully. \n\nUpdated Agents:\n" + agentList(updatedAgents);
    }

    // Facilities
    public String facilitiesMenu() {
        return "\nFacilities\n" +
                "L - List all Facilities\n" +
                "N - Add New Facility\n" +
                "E - Edit Facility\n" +
                "# - Enter ID to read facility details\n" +
                "M - Return to Main Menu\n" +
                "Choice: ";
    }


    public String countryList(List<Country> countries)
    {
        StringBuilder sb = new StringBuilder("\nCountries:\n");

        int i = 1;
        for (Country c : countries) {
            sb.append(i)
                    .append(". ")
                    .append(c.getName())
                    .append(", ID: ")
                    .append(c.getId())
                    .append("\n");
            i++;
        }

        return sb.toString();
    }


    public String facilityList(List<Facility> facilities)
    {
        if (facilities == null || facilities.isEmpty())
        {
            return "No facilities available.";
        }
        StringBuilder sb = new StringBuilder("\nAll Facilities:\n");
        for (Facility f : facilities)
        {
            sb.append(f).append("\n");
        }
        return sb.toString().trim();
    }

    public String facilityDetail(Facility facility)
    {
        if (facility == null)
        {
            return "Facility not found.";
        }
        return "\nFacility Details:\n" +
                "ID: " + facility.getID() + "\n" +
                "Name: " + facility.getName() + "\n" +
                "Abbreviation: " + facility.getAbbreviation() + "\n" +
                "Country: " + (facility.getCountryName() == null ? "No Country" : facility.getCountryName()) + "\n";
    }

    public String facilityAdded(List<Facility> updatedFacilities)
    {
        return "Facility added successfully.\n\nUpdated Facilities:\n" + facilityList(updatedFacilities);
    }

    // Messages
    public String messagesMenu()
    {
        return "\nMessages\n" +
                "S - Send message to agent\n" +
                "V - View my inbox\n" +
                "M - Return to Main Menu\n" +
                "Choice: ";
    }

    public String agentInbox(List<Message> messages)
    {
        if (messages == null || messages.isEmpty())
        {
            return "No messages for your account.";
        }
        StringBuilder sb = new StringBuilder("\nInbox:\n");
        for (Message message : messages)
        {
            sb.append("#").append(message.getId())
                    .append(" from ").append(message.getSenderUsername())
                    .append(": ").append(message.getBody())
                    .append("\n");
        }
        return sb.toString().trim();
    }

    // Audit Log
    public String auditLog(List<String> lines)
    {
        if (lines == null || lines.isEmpty())
        {
            return "\nAudit log is empty.";
        }
        StringBuilder sb = new StringBuilder("\nAudit Log:\n");
        for (String line : lines)
        {
            sb.append(line).append("\n");
        }
        return sb.toString().trim();
    }

    // Generic
    public String error(String message)
    {
        return "Error: " + message;
    }

    public String unknownOption(String input)
    {
        return "Unknown option '" + input + "'.\n";
    }

    public String welcome()
    {
        return "Welcome to the Mission Brief System.";
    }

    public String goodbye()
    {
        return "\nExiting Mission Brief System.";
    }
}