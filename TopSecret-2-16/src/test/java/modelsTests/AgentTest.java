package modelsTests;
import models.Agent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AgentTest {

    @Test
    void testConstructorAndGetters() {
        Agent a = new Agent(1, "Bond", "1990-01-01", "2020-06-15", "Field operative", 2);
        assertEquals(1, a.getId());
        assertEquals("Bond", a.getName());
        assertEquals("1990-01-01", a.getDateOfBirth());
        assertEquals("2020-06-15", a.getDateOfDeath());
        assertEquals("Field operative", a.getNotes());
        assertEquals(2, a.getFacilityId());
    }

    @Test
    void testConstructorNullFields() {
        Agent a = new Agent(5, "Ghost", null, null, null, 3);
        assertNull(a.getDateOfBirth());
        assertNull(a.getDateOfDeath());
        assertNull(a.getNotes());
    }

    @Test
    void testToString() {
        Agent a = new Agent(7, "Shadow", "1985-03-20", "", "", 4);
        String result = a.toString();
        assertTrue(result.contains("7"));
        assertTrue(result.contains("Shadow"));
        assertTrue(result.contains("4"));
    }

    @Test
    void testToStringFormat() {
        Agent a = new Agent(1, "Alpha", "1970", "", "", 1);
        assertEquals("1. Alpha (Facility 1)", a.toString());
    }
}