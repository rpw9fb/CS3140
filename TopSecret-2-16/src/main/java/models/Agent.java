package models;

public class Agent
{
    private int id;
    private String name;
    private String dateOfBirth;
    private String dateOfDeath;
    private String notes;
    private int facilityId;

    public Agent(int id, String name, String dateOfBirth, String dateOfDeath, String notes, int facilityId)
    {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.dateOfDeath = dateOfDeath;
        this.notes = notes;
        this.facilityId = facilityId;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDateOfBirth()
    {
        return dateOfBirth;
    }

    public String getDateOfDeath()
    {
        return dateOfDeath;
    }

    public String getNotes()
    {
        return notes;
    }

    public int getFacilityId() {
        return facilityId;
    }

    @Override
    public String toString()
    {
        return id + ". " + name + " (Facility " + facilityId + ")";
    }
}
