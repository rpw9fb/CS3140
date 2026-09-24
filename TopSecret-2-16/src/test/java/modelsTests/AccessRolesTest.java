package modelsTests;

import models.Password;
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class AccessRolesTest {

    private static final String TEMP_CRED_FILE = "test_credentials.cip";
    private static final String TEMP_KEY_FILE = "test_key.txt";
    private Password password;

    @BeforeEach
    void setUp() throws IOException {
        // Create a simple key file
        Files.write(Paths.get(TEMP_KEY_FILE), "abc\nzyx".getBytes());

        // Start with empty credential file
        Files.deleteIfExists(Paths.get(TEMP_CRED_FILE));
        Files.createFile(Paths.get(TEMP_CRED_FILE));

        // Scanner input for createCredentials
        Scanner scanner = new Scanner(System.in);
        password = new Password(TEMP_CRED_FILE, TEMP_KEY_FILE, scanner);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEMP_CRED_FILE));
        Files.deleteIfExists(Paths.get(TEMP_KEY_FILE));
    }

    @Test
    void testIsCredentialsEmptyInitiallyTrue() {
        assertTrue(password.isCredentialsEmpty(), "New credential file should be empty");
    }

    @Test
    void testIsCredentialsEmptyAfterAddingUser() throws IOException {
        Files.write(Paths.get(TEMP_CRED_FILE), "user\tpass\tuser".getBytes());
        assertFalse(password.isCredentialsEmpty(), "File with text should not be empty");
    }

    @Test
    void testCreateCredentialsSuccess() {
        try {
            // Simulate user input
            String input = "TestUser\nPassword1\n";
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            Scanner scanner = new Scanner(System.in);

            password = new Password(TEMP_CRED_FILE, TEMP_KEY_FILE, scanner);

            boolean created = password.createCredentials();
            assertTrue(created, "Should create credentials successfully");
            assertEquals("TestUser", password.getCurrentUsername(), "Current username should be set");
            assertFalse(password.isCredentialsEmpty(), "Credential file should no longer be empty");
        } catch (IOException e) {
            fail("Failed to create Password manager: " + e.getMessage());
        }
    }

    @Test
    void testAddUserCreatesAdminIfFirstUser() {
        boolean added = password.addUser("AdminUser");
        assertTrue(added, "First user should be added");
        assertTrue(password.isAdminUser("AdminUser"), "First user should be admin");
    }

    @Test
    void testAddUserCreatesStandardUserIfNotFirst() {
        // First user
        password.addUser("AdminUser");

        // Second user
        boolean added = password.addUser("StandardUser");
        assertTrue(added, "Second user should be added");
        assertFalse(password.isAdminUser("StandardUser"), "Second user should not be admin");
    }

    @Test
    void testIsAdminUserReturnsFalseForNonexistent() {
        assertFalse(password.isAdminUser("NonUser"), "Non-existent user should not be admin");
    }

    @Test
    void testPromoteToAdminSuccess() {
        // First user -> admin
        password.addUser("AdminUser");
        // Second user -> regular user
        password.addUser("RegularUser");

        assertFalse(password.isAdminUser("RegularUser"), "Should start as regular user");

        boolean promoted = password.promoteToAdmin("RegularUser");
        assertTrue(promoted, "Promotion should succeed");
        assertTrue(password.isAdminUser("RegularUser"), "User should now be admin");
    }

    @Test
    void testPromoteToAdminAlreadyAdmin() {
        password.addUser("AdminUser"); // first user = admin
        boolean promoted = password.promoteToAdmin("AdminUser");
        assertFalse(promoted, "Should not promote an already-admin user");
    }

    @Test
    void testPromoteToAdminNonExistentUser() {
        boolean promoted = password.promoteToAdmin("GhostUser");
        assertFalse(promoted, "Should not promote a non-existent user");
    }

    @Test
    void testPromoteToAdminNullUsername() {
        assertFalse(password.promoteToAdmin(null), "Should return false for null username");
    }

    @Test
    void testPromoteToAdminEmptyUsername() {
        assertFalse(password.promoteToAdmin(""), "Should return false for empty username");
    }

    @Test
    void testGetAllUsersReturnsCorrectData() {
        password.addUser("AdminUser");
        password.addUser("RegularUser");

        var users = password.getAllUsers();
        assertEquals(2, users.size(), "Should have 2 users");
        assertEquals("AdminUser", users.get(0).getKey());
        assertEquals("admin", users.get(0).getValue());
        assertEquals("RegularUser", users.get(1).getKey());
        assertEquals("user", users.get(1).getValue());
    }

    @Test
    void testGetAllUsersEmptyFile() {
        var users = password.getAllUsers();
        assertTrue(users.isEmpty(), "Should return empty list when no users exist");
    }

    @Test
    void testGetAllUsersReflectsPromotion() {
        password.addUser("AdminUser");
        password.addUser("RegularUser");

        password.promoteToAdmin("RegularUser");

        var users = password.getAllUsers();
        assertEquals("admin", users.get(1).getValue(), "Promoted user should show as admin");
    }

}