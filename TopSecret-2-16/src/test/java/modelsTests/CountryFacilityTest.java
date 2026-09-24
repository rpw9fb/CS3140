package modelsTests;

import models.Country;
import models.Facility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CountryFacilityTest {

    // === Country ===

    @Test
    void country_constructorAndGetters() {
        Country c = new Country(42, "France");
        assertEquals(42, c.getId());
        assertEquals("France", c.getName());
    }

    @Test
    void country_differentValues() {
        Country c = new Country(1, "United Kingdom");
        assertEquals(1, c.getId());
        assertEquals("United Kingdom", c.getName());
    }

    // === Facility ===

    @Test
    void facility_fullConstructorAndGetters() {
        Facility f = new Facility(3, "CIA", "CIA", 10, "United States");
        assertEquals(3, f.getID());
        assertEquals("CIA", f.getName());
        assertEquals("CIA", f.getAbbreviation());
        assertEquals(10, f.getCountryID());
        assertEquals("United States", f.getCountryName());
    }

    @Test
    void facility_shortConstructorNullCountry() {
        Facility f = new Facility(1, "HQ", "HQ");
        assertEquals(1, f.getID());
        assertEquals("HQ", f.getName());
        assertEquals("HQ", f.getAbbreviation());
        assertNull(f.getCountryID());
        assertNull(f.getCountryName());
    }

    @Test
    void facility_toStringWithCountry() {
        Facility f = new Facility(1, "CIA", "CIA", 5, "USA");
        String result = f.toString();
        assertTrue(result.contains("CIA"));
        assertTrue(result.contains("USA"));
    }

    @Test
    void facility_toStringNoCountry() {
        Facility f = new Facility(2, "NSA", "NS");
        String result = f.toString();
        assertTrue(result.contains("NSA"));
        assertTrue(result.contains("No Country"));
    }
}