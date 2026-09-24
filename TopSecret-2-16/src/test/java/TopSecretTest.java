import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class TopSecretTest {

    @Test
    void testDefaultConstructor() {
        // Covers the implicit default constructor (class declaration line)
        TopSecret ts = new TopSecret();
        assertNotNull(ts);
    }

    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    private void setInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    private String output() {
        return outContent.toString();
    }

    /**
     * Wrong credentials → UserInterface constructor throws RuntimeException.
     * Covers the else branch and the RuntimeException catch block.
     */
    @Test
    void testMainTerminalAuthFailure() {
        // Non-existent user → "Username not found" → answer n → "Login cancelled"
        // → authenticate() returns false → UserInterface throws RuntimeException
        setInput("nonexistent_user_xyz_12345\nn\n");
        TopSecret.main(new String[]{});
        assertTrue(output().contains("Could not start session"));
    }

    /**
     * -w flag → WebController path is executed.
     * Covers lines 41-44 (isWeb branch).
     */
    @Test
    void testMainWebPath() {
        // Provide input in case credentials.cip doesn't exist and it prompts for creation
        setInput("webuser\nWebPass1\n");
        assertDoesNotThrow(() -> TopSecret.main(new String[]{"-w"}));
    }

    /**
     * -w with extra args → warning message printed.
     * Covers lines 36-38 (isWeb & args.length > 1 branch).
     */
    @Test
    void testMainWebWithExtraArgs() {
        // Provide input in case credentials.cip doesn't exist and it prompts for creation
        setInput("webuser2\nWebPass1\n");
        TopSecret.main(new String[]{"-w", "extra"});
        assertTrue(output().contains("Additional command line arguments cannot be processed"));
    }

    /**
     * Bad key file → IOException → error message printed and method returns.
     * Covers lines 21-23 (IOException catch block).
     */
    @Test
    void testMainIOException() throws IOException {
        Path keyPath = Paths.get("ciphers/key.txt");
        Path keyBackup = Paths.get("ciphers/key.txt.topsecret_bak");
        Files.move(keyPath, keyBackup, StandardCopyOption.REPLACE_EXISTING);
        try {
            setInput("");
            TopSecret.main(new String[]{});
            assertTrue(output().contains("Error loading cipher key"));
        } finally {
            Files.move(keyBackup, keyPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * No credentials file → "create new account" path → creates account and launches UI.
     * Covers lines 26-33 (credential creation block) and the normal terminal UI path.
     */
    @Test
    void testMainCredentialCreation() throws IOException {
        Path credPath = Paths.get("credentials.cip");
        Path credBackup = Paths.get("credentials.cip.topsecret_bak");
        boolean originalExists = Files.exists(credPath);
        if (originalExists) {
            Files.copy(credPath, credBackup, StandardCopyOption.REPLACE_EXISTING);
            Files.delete(credPath);
        }
        try {
            // Input: create-username, create-password, auth-username, auth-password, UI-exit
            setInput("tsuser123\nTestPass1\ntsuser123\nTestPass1\nX\n");
            TopSecret.main(new String[]{});
            assertTrue(output().contains("Credentials created successfully"));
        } finally {
            Files.deleteIfExists(credPath);
            if (originalExists) {
                Files.move(credBackup, credPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
