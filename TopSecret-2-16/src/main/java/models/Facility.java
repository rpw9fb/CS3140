package models;

public class Facility {
    private int id;
    private String name;
    private String abbreviation;
    private Integer countryID;
    private String countryName;

    public Facility(int id, String name, String abbreviation, Integer countryID, String countryName) {
        this.id = id;
        this.name = name;
        this.abbreviation = abbreviation;
        this.countryID = countryID;
        this.countryName = countryName;
    }

    public Facility(int id, String name, String abbreviation) {
        this(id, name, abbreviation, null, null);
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getCountryName() {
        return countryName;
    }

    public Integer getCountryID() {
        return countryID;
    }

    @Override
    public String toString() {
        String countryDisplay = (countryName == null) ? "No Country" : countryName;
        return id + ". " + name + " (" + abbreviation + ") - " + countryDisplay;
    }
}

