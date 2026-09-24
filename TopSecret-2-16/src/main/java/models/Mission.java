package models;

import java.util.ArrayList;
import java.util.List;

public class Mission
{
    private int id;
    private String title;
    private String brief;
    private String date;

    private int facilityId;
    private List<Integer> agentIds;

    public Mission(int id, String title, String brief, String date, int facilityId, List<Integer> agentIds)
    {
        this.id = id;
        this.title = title;
        this.brief = brief;
        this.date = date;
        this.facilityId = facilityId;
        this.agentIds = agentIds != null ? new ArrayList<>(agentIds) : new ArrayList<>();
    }

    // Getters

    public int getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getBrief()
    {
        return brief;
    }

    public String getDate()
    {
        return date;
    }

    public int getFacilityId()
    {
        return facilityId;
    }

    public List<Integer> getAgentIds()
    {
        return agentIds;
    }

    // Setters

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setBrief(String brief)
    {
        this.brief = brief;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public void setFacilityId(int facilityId)
    {
        this.facilityId = facilityId;
    }

    public void setAgentIds(List<Integer> agentIds)
    {
        this.agentIds = agentIds != null ? new ArrayList<>(agentIds) : new ArrayList<>();
    }

    // Agent helpers

    public void addAgentId(int agentId)
    {
        if (!agentIds.contains(agentId))
        {
            agentIds.add(agentId);
        }
    }

    public void removeAgentId(int agentId)
    {
        agentIds.remove(Integer.valueOf(agentId));
    }

    @Override
    public String toString()
    {
        return id + ". " + title + " (Facility " + facilityId + ", Agents: " + agentIds.size() + ")";
    }
}
