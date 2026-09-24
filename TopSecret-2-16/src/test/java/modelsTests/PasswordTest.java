package modelsTests;

import models.Password;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

public class PasswordTest {

    private static final String KEY_PATH = "ciphers/key.txt";
    private static final String TEST_CREDENTIAL_FILE = "test_credentials.cip";

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void tearDown() throws IOException {
        System.setOut(originalOut);
        // Clean up test credential file after each test
        Files.deleteIfExists(Paths.get(TEST_CREDENTIAL_FILE));
    }

    private String output() {
        return outContent.toString();
    }

    private Password buildPassword(String input) throws IOException {
        return new Password(TEST_CREDENTIAL_FILE, KEY_PATH, new Scanner(input));
    }

    // ========================
    // Unit Tests: isValidUsername
    // ========================

    @Test
    public void testValidUsernameLowercase() throws IOException {
        Password pw = buildPassword("");
        assertTrue(pw.isValidUsername("alice"));
    }

    @Test
    public void testInvalidUsernameTooShort() throws IOException {
        Password pw = buildPassword("");
        assertFalse(pw.isValidUsername("ab"));
    }

    @Test
    public void testInvalidUsernameEmpty() throws IOException {
        Password pw = buildPassword("");
        assertFalse(pw.isValidUsername(""));
    }

    @Test
    public void testInvalidUsernameNull() throws IOException {
        Password pw = buildPassword("");
        assertFalse(pw.isValidUsername(null));
    }

    @Test
    public void testValidUsernameUppercaseAllowed() throws IOException {
        Password pw = buildPassword("");
        assertTrue(pw.isValidUsername("Alice"));
    }

    @Test
    public void testValidUsernameWithDigits() throws IOException {
        Password pw = buildPassword("");
        assertTrue(pw.isValidUsername("alice123"));
    }

    @Test
    public void testValidUsernameWithHyphenUnderscore() throws IOException {
        Password pw = buildPassword("");
        assertTrue(pw.isValidUsername("al-ice_123"));
    }

    @Test
    public void testInvalidUsernameWithSpaces() throws IOException {
        Password pw = buildPassword("");
        assertFalse(pw.isValidUsername("al ice"));
    }

    @Test
    public void testInvalidUsernameWithSpecialChars() throws IOException {
        Password pw = buildPassword("");
        assertFalse(pw.isValidUsername("alice!"));
    }

    // ========================
    // Unit Tests: isValidPassword
    // ========================

    @Test
    public void testValidPasswordFiveCharsMixedCase() throws IOException {
        Password pw = buildPassword("");
        assertTrue(pw.isValidPassword("Abcde"));
    }

    @Test
    public void testValidPasswordLong() throws IOException {
        Password pw = buildPassword("");
        assertTrue(pw.isValidPassword("MySecurePassword"));
    }

    @Test
    public void testInvalidPasswordTooShort() throws IOException {
        Password pw = buildPassword("");
        assertFalse(pw.isValidPassword("abcd"));
    }

    @Test
    public void testInvalidPasswordOneChar() throws IOException {
        Password pw = buildPassword("");
        assertFalse(pw.isValidPassword("a"));
    }

    @Test
    public void testInvalidPasswordEmpty() throws IOException {
        Password pw = buildPassword("");
        assertFalse(pw.isValidPassword(""));
    }

    @Test
    public void testInvalidPasswordNull() throws IOException {
        Password pw = buildPassword("");
        assertFalse(pw.isValidPassword(null));
    }

    @Test
    public void testInvalidPasswordNoUppercase() throws IOException {
        Password pw = buildPassword("");
        assertFalse(pw.isValidPassword("abcde"));
    }

    @Test
    public void testInvalidPasswordNoLowercase() throws IOException {
        Password pw = buildPassword("");
        assertFalse(pw.isValidPassword("ABCDE"));
    }

    // ========================
    // Unit Tests: encrypt / decrypt
    // ========================

    @Test
    public void testEncryptDecryptRoundTrip() throws IOException {
        Password pw = buildPassword("");
        String original = "hello";
        String encrypted = pw.encrypt(original);
        String decrypted = pw.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    public void testEncryptProducesDifferentString() throws IOException {
        Password pw = buildPassword("");
        String original = "hello";
        String encrypted = pw.encrypt(original);
        assertNotEquals(original, encrypted);
    }

    @Test
    public void testEncryptNull() throws IOException {
        Password pw = buildPassword("");
        assertNull(pw.encrypt(null));
    }

    @Test
    public void testDecryptNull() throws IOException {
        Password pw = buildPassword("");
        assertNull(pw.decrypt(null));
    }

    @Test
    public void testEncryptDecryptMixedCase() throws IOException {
        Password pw = buildPassword("");
        String original = "Hello123";
        String encrypted = pw.encrypt(original);
        String decrypted = pw.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    public void testEncryptDecryptSpecialChars() throws IOException {
        Password pw = buildPassword("");
        String original = "pass!@#";
        String encrypted = pw.encrypt(original);
        String decrypted = pw.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    // ========================
    // Unit Tests: credentialFileExists
    // ========================

    @Test
    public void testCredentialFileExistsFalse() throws IOException {
        Password pw = buildPassword("");
        assertFalse(pw.credentialFileExists());
    }

    @Test
    public void testCredentialFileExistsTrue() throws IOException {
        // Create the credential file first
        Files.writeString(Paths.get(TEST_CREDENTIAL_FILE), "alice\tbcdef");
        Password pw = buildPassword("");
        assertTrue(pw.credentialFileExists());
    }

    @Test
    public void testGetCurrentUsernameNullBeforeLogin() throws IOException {
        Password pw = buildPassword("");
        assertNull(pw.getCurrentUsername());
    }

    // ========================
    // Integration Tests: createCredentials
    // ========================

    @Test
    public void testCreateCredentialsSuccess() throws IOException {
        Password pw = buildPassword("alice\nHelloWorld\n");
        boolean result = pw.createCredentials();
        assertTrue(result);
        assertTrue(Files.exists(Paths.get(TEST_CREDENTIAL_FILE)));
        assertTrue(output().contains("Credentials created successfully"));
    }

    @Test
    public void testCreateCredentialsInvalidUsername() throws IOException {
        Password pw = buildPassword("Al!\nHelloWorld\n");
        boolean result = pw.createCredentials();
        assertFalse(result);
        assertFalse(Files.exists(Paths.get(TEST_CREDENTIAL_FILE)));
        assertTrue(output().contains("Invalid username"));
    }

    @Test
    public void testCreateCredentialsInvalidPassword() throws IOException {
        Password pw = buildPassword("alice\nabc\n");
        boolean result = pw.createCredentials();
        assertFalse(result);
        assertFalse(Files.exists(Paths.get(TEST_CREDENTIAL_FILE)));
        assertTrue(output().contains("Invalid password"));
    }

    // ========================
    // Integration Tests: authenticate
    // ========================

    @Test
    public void testAuthenticateSuccess() throws IOException {
        // Create credentials first
        Password pwCreate = buildPassword("alice\nHelloWorld\n");
        pwCreate.createCredentials();

        // Reset output
        outContent.reset();

        // Authenticate with the same credentials
        Password pwAuth = buildPassword("alice\nHelloWorld\n");
        boolean result = pwAuth.authenticate();
        assertTrue(result);
        assertTrue(output().contains("Login successful"));
    }

    @Test
    public void testAuthenticateWrongPassword() throws IOException {
        Password pwCreate = buildPassword("alice\nHelloWorld\n");
        pwCreate.createCredentials();
        outContent.reset();

        Password pwAuth = buildPassword("alice\nwrongpass\n");
        boolean result = pwAuth.authenticate();
        assertFalse(result);
        assertTrue(output().contains("Invalid username or password"));
    }

    @Test
    public void testAuthenticateWrongUsername() throws IOException {
        Password pwCreate = buildPassword("alice\nHelloWorld\n");
        pwCreate.createCredentials();
        outContent.reset();

        Password pwAuth = buildPassword("bob\nn\n");
        boolean result = pwAuth.authenticate();
        assertFalse(result);
        assertTrue(output().contains("Username not found"));
        assertTrue(output().contains("Login cancelled."));
    }

    @Test
    public void testAuthenticateNoCredentialFile() throws IOException {
        Password pw = buildPassword("alice\nn\n");
        boolean result = pw.authenticate();
        assertFalse(result);
        assertTrue(output().contains("Username not found"));
        assertTrue(output().contains("Login cancelled."));
    }

    // ========================
    // Integration Tests: changePassword
    // ========================

    @Test
    public void testChangePasswordSuccess() throws IOException {
        // Create initial credentials
        Password pwCreate = buildPassword("alice\nHelloWorld\n");
        pwCreate.createCredentials();
        outContent.reset();

        // Authenticate with current password first
        Password pwChange = buildPassword("alice\nHelloWorld\nNewPassword\nNewPassword\n");
        assertTrue(pwChange.authenticate()); // authenticate with old password
        boolean result = pwChange.changePassword();

        assertTrue(result);
        assertTrue(output().contains("Password changed successfully"));
        outContent.reset();

        // Authenticate with new password
        Password pwAuth = buildPassword("alice\nNewPassword\n");
        assertTrue(pwAuth.authenticate());
    }

    @Test
    public void testChangePasswordMismatch() throws IOException {
        Password pwCreate = buildPassword("alice\nHelloWorld\n");
        pwCreate.createCredentials();
        outContent.reset();

        Password pwChange = buildPassword("alice\nHelloWorld\nNewPassword\ndifferent\n");
        assertTrue(pwChange.authenticate());
        boolean result = pwChange.changePassword();
        assertFalse(result);
        assertTrue(output().contains("Passwords do not match"));
    }

    @Test
    public void testChangePasswordTooShort() throws IOException {
        Password pwCreate = buildPassword("alice\nHelloWorld\n");
        pwCreate.createCredentials();
        outContent.reset();

        Password pwChange = buildPassword("alice\nHelloWorld\nabc\nabc\n");
        assertTrue(pwChange.authenticate());
        boolean result = pwChange.changePassword();
        assertFalse(result);
        assertTrue(output().contains("Invalid password"));
    }

    @Test
    public void testChangePasswordOldPasswordNoLongerWorks() throws IOException {
        Password pwCreate = buildPassword("alice\nHelloWorld\n");
        pwCreate.createCredentials();

        Password pwChange = buildPassword("alice\nHelloWorld\nNewPassword\nNewPassword\n");
        assertTrue(pwChange.authenticate());
        pwChange.changePassword();
        outContent.reset();

        // Old password should fail
        Password pwAuth = buildPassword("alice\nHelloWorld\n");
        assertFalse(pwAuth.authenticate());
    }

    @Test
    public void testCreateUserUsernameOnlyThenFirstLoginSetsPassword() throws IOException {
        Password pwCreate = buildPassword("admin\nAdminPass\n");
        assertTrue(pwCreate.createCredentials());

        Password pwAdmin = buildPassword("");
        assertTrue(pwAdmin.createUserUsernameOnly("new_user"));

        Password firstLogin = buildPassword("new_user\nAbcde\nAbcde\n");
        assertTrue(firstLogin.authenticate());
        assertEquals("new_user", firstLogin.getCurrentUsername());
    }

    @Test
    public void consumeAccountCreatedDuringAuthenticateTest() throws IOException {
        Password p = buildPassword("admin\nAdminPass\n");
        boolean result = p.consumeAccountCreatedDuringAuthenticate();
        assertFalse(result);
    }

    @Test void webAuthTest() throws IOException {
        Password p = buildPassword("admin\nAdminPass\n");
        boolean result = p.webAuth("SomeWrongUsername", "SomeWrongPassword");
        assertFalse(result);
    }
}
