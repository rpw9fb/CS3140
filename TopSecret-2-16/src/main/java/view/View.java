package view;

import models.Agent;
import models.Facility;
import models.Mission;
import java.util.List;

public interface View
{
    // Main Menu
    String mainMenu (boolean isAdmin);

    // Missions
    default String missionsMenu()
    {
        return "";

    }
    String missionList(List<String> titles);
    default String missionDetail(String missionText)
    {
        return "";
    }
    String missionSearchResults(List<Mission> matches, String phrase);
    default String missionAdded(List<String> updatedTitles)
    {
        return "";
    }

    // Agents
    default String agentsMenu()
    {
        return "";
    }
    String agentList(List<Agent> agents);
    String agentDetail(Agent agent);
    default String agentAdded(List<Agent> updatedAgents)
    {
        return "";
    }

    // Facilities
    default String facilitiesMenu()
    {
        return "";
    }
    String facilityList(List<Facility> facilities);
    default String facilityDetail(Facility facility)
    {
        return "";
    }
    default String facilityAdded(List<Facility> updatedFacilities)
    {
        return "";
    }

    // Audit Log
    String auditLog(List<String> lines);

    String error (String message);
}
