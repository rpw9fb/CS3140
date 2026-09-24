package models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.AbstractMap;
import java.util.Scanner;

/**
 * Manages credential file (.cip) for user authentication.
 * Supports multiple users in one credential file.
 * Validates usernames (>= 3 chars, letters/digits/hyphen/underscore) and passwords
 * (min 5 chars, must include uppercase and lowercase).
 * Stores credentials ciphered using the default cipher key.
 */
public class Password {

    private final String credentialFilePath;
    private final String keyPath;
    private final Scanner scanner;
    private final Map<Character, Character> encryptionMap;
    private final Map<Character, Character> decryptionMap;
    private String currentUsername;
    private boolean accountCreatedDuringAuthenticate;

    private static final String USERNAME_PATTERN = "^[A-Za-z0-9_-]{3,}$";

    /**
     * Constructs a Password manager.
     *
     * @param credentialFilePath path to the .cip credential file
     * @param keyPath            path to the cipher key file
     * @param scanner            Scanner for user input
     * @throws IOException if the key file cannot be read
     */
    public Password(String credentialFilePath, String keyPath, Scanner scanner) throws IOException {
        this.credentialFilePath = credentialFilePath;
        this.keyPath = keyPath;
        this.scanner = scanner;
        this.encryptionMap = new HashMap<>();
        this.decryptionMap = new HashMap<>();
        loadKey();
    }

    /**
     * Loads the cipher key and builds both encryption and decryption maps.
     */
    private void loadKey() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(keyPath));

        if (lines.size() < 2) {
            throw new IllegalArgumentException("Key file must contain at least two lines");
        }

        String plainText = lines.get(0);
        String cipherText = lines.get(1);

        if (plainText.length() != cipherText.length()) {
            throw new IllegalArgumentException("Key file line lengths do not match");
        }

        for (int i = 0; i < plainText.length(); i++) {
            encryptionMap.put(plainText.charAt(i), cipherText.charAt(i));
            decryptionMap.put(cipherText.charAt(i), plainText.charAt(i));
        }
    }

    /**
     * Checks whether the credential file exists.
     *
     * @return true if the credential file exists, false otherwise
     */
    public boolean credentialFileExists() {
        return Files.exists(Paths.get(credentialFilePath));
    }

    /**
     * @return username from most recent successful login (or null)
     */
    public String getCurrentUsername() {
        return currentUsername;
    }

    public boolean consumeAccountCreatedDuringAuthenticate() {
        boolean created = accountCreatedDuringAuthenticate;
        accountCreatedDuringAuthenticate = false;
        return created;
    }

    /**
     * Validates a username. A valid username is at least 3 characters and contains
     * only letters, digits, hyphens and underscores.
     *
     * @param username the username to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return username.matches(USERNAME_PATTERN);
    }

    /**
     * Validates a password. A valid password is at least 5 characters long and
     * includes at least one uppercase and one lowercase letter.
     *
     * @param password the password to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        if (password.length() < 5) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            }
        }
        return hasUpper && hasLower;
    }

    /**
     * Encrypts a string using the cipher key (plaintext -> ciphertext mapping).
     *
     * @param input the plaintext string to encrypt
     * @return the encrypted string
     */
    public String encrypt(String input) {
        if (input == null) {
            return null;
        }

        char[] inputArray = input.toCharArray();
        char[] outputArray = new char[inputArray.length];

        for (int i = 0; i < inputArray.length; i++) {
            char cur = inputArray[i];
            if (encryptionMap.containsKey(cur)) {
                outputArray[i] = encryptionMap.get(cur);
            } else {
                outputArray[i] = cur;
            }
        }

        return new String(outputArray);
    }

    /**
     * Decrypts a string using the cipher key (ciphertext -> plaintext mapping).
     *
     * @param input the ciphered string to decrypt
     * @return the decrypted string
     */
    public String decrypt(String input) {
        if (input == null) {
            return null;
        }

        char[] inputArray = input.toCharArray();
        char[] outputArray = new char[inputArray.length];

        for (int i = 0; i < inputArray.length; i++) {
            char cur = inputArray[i];
            if (decryptionMap.containsKey(cur)) {
                outputArray[i] = decryptionMap.get(cur);
            } else {
                outputArray[i] = cur;
            }
        }

        return new String(outputArray);
    }


    public boolean isCredentialsEmpty(){
        Path path = Paths.get(credentialFilePath); // use the correct path
        if (Files.exists(path)) {
            try {
                List<String> lines = Files.readAllLines(path);
                boolean hasEntries = lines.stream().anyMatch(line -> !line.trim().isEmpty());
                return !hasEntries;
            } catch (IOException e) {
                System.out.println("Failed to read credential file.");
                return true;
            }
        }
        return true;
    }

    /**
     * Prompts the user to create a new username and password pair.
     * Validates both, ciphers the password, and writes to the credential file.
     *
     * @return true if credentials were successfully created, false otherwise
     */
    public boolean createCredentials() {
        System.out.print("Create username: ");
        String username = scanner.nextLine().trim();

        if (!isValidUsername(username)) {
            System.out.println("Invalid username. Username must be at least 3 characters and can only contain letters, numbers, hyphens and underscores.");
            return false;
        }

        if (userExists(username)) {
            System.out.println("Username already exists.");
            return false;
        }

        System.out.print("Create password: ");
        String password = scanner.nextLine().trim();

        if (!isValidPassword(password)) {
            System.out.println("Invalid password. Password must be at least 5 characters long and include uppercase and lowercase letters.");
            return false;
        }

        Path path = Paths.get(credentialFilePath);

        boolean isEmpty = isCredentialsEmpty();

        boolean ok;
        if (isEmpty) {
            ok = setUserPassword(username, password, true);
        } else {
            ok = setUserPassword(username, password, false);
        }

        if (!ok) {
            return false;
        }

        currentUsername = username;
        System.out.println("Credentials created successfully!");
        return true;
    }


    public boolean webAuth(String username, String password) {
        String storedEncryptedPassword = getEncryptedPassword(username);
        if (storedEncryptedPassword == null) {
            return false;
        }
        if (storedEncryptedPassword.isEmpty()) {
            return false;
        }

        String decryptedPassword = decrypt(storedEncryptedPassword);
        if (decryptedPassword != null && decryptedPassword.equals(password)) {
            currentUsername = username;
            return true;
        }
        return false;
    }

    /**
     * Prompts the user for username and password and verifies them
     * against the stored credentials in the .cip file.
     *
     * @return true if authentication succeeds, false otherwise
     */
    public boolean authenticate() {
        accountCreatedDuringAuthenticate = false;
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        String storedEncryptedPassword = getEncryptedPassword(username);
        if (storedEncryptedPassword == null) {
            System.out.print("Username not found. Would you like to create a new account? (y/n): ");
            String answer = scanner.nextLine().trim();
            if (!answer.equalsIgnoreCase("y")) {
                System.out.println("Login cancelled.");
                return false;
            }
            // Re-use username already entered; prompt only for password
            if (!isValidUsername(username)) {
                System.out.println("Invalid username. Username must be at least 3 characters and contain only letters, numbers, hyphens and underscores.");
                return false;
            }
            System.out.print("Create password: ");
            String newPassword = scanner.nextLine().trim();
            if (!isValidPassword(newPassword)) {
                System.out.println("Invalid password. Password must be at least 5 characters long and include uppercase and lowercase letters.");
                return false;
            }
            // First account in file becomes admin, all subsequent accounts are regular users
            boolean makeAdmin = isCredentialsEmpty();
            if (!setUserPassword(username, newPassword, makeAdmin)) {
                return false;
            }
            currentUsername = username;
            accountCreatedDuringAuthenticate = true;
            System.out.println("Account created" + (makeAdmin ? " (admin)" : "") + ". Login successful.");
            return true;
        }

        // If the user exists but has no password yet, set it now (first login).
        if (storedEncryptedPassword.isEmpty()) {
            System.out.println("No password set for this user. Please create one now.");
            if (!promptAndSetNewPassword(username)) {
                return false;
            }
            currentUsername = username;
            System.out.println("Login successful.");
            return true;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        // Compare username directly and decrypt stored password to compare
        String decryptedPassword = decrypt(storedEncryptedPassword);
        if (decryptedPassword != null && decryptedPassword.equals(password)) {
            currentUsername = username;
            System.out.println("Login successful.");
            return true;
        }

        System.out.println("Invalid username or password.");
        return false;
    }

    /**
     * Allows the user to change their password.
     * Requires entering the new password twice. Updates the current user's entry.
     *
     * @return true if the password was changed successfully, false otherwise
     */
    public boolean changePassword() {
        if (currentUsername == null) {
            System.out.println("No user is logged in.");
            return false;
        }

        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine().trim();

        if (!isValidPassword(newPassword)) {
            System.out.println("Invalid password. Password must be at least 5 characters long and include uppercase and lowercase letters.");
            return false;
        }

        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine().trim();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            return false;
        }

        boolean isAdmin = isAdminUser(currentUsername);
        boolean ok = setUserPassword(currentUsername, newPassword, isAdmin);
        if (!ok) {
            return false;
        }

        System.out.println("Password changed successfully.");
        return true;
    }

    /**
     * Adds a new user without a password. The user will be prompted to create a password
     * on first login.
     */
    public boolean addUser(String username) {
        if (!isValidUsername(username)) {
            System.out.println("Invalid username. Username must be at least 3 characters and can only contain letters, numbers, hyphens and underscores.");
            return false;
        }
        if (userExists(username)) {
            System.out.println("Username already exists.");
            return false;
        }
        try {
            List<String> lines = readCredentialLines();
            if(isCredentialsEmpty()) {
                lines.add(formatLine(username, "", "admin"));
            }
            else{
                lines.add(formatLine(username, "", "user"));
            }
            writeCredentialLines(lines);
            return true;
        } catch (IOException e) {
            System.out.println("Error writing credential file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Alias for homework wording: an existing user creates a new username only.
     */
    public boolean createUserUsernameOnly(String username) {
        return addUser(username);
    }

    private boolean promptAndSetNewPassword(String username) {
        System.out.print("Create password: ");
        String password = scanner.nextLine().trim();
        if (!isValidPassword(password)) {
            System.out.println("Invalid password. Password must be at least 5 characters long and include uppercase and lowercase letters.");
            return false;
        }

        System.out.print("Confirm password: ");
        String confirmPassword = scanner.nextLine().trim();
        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            return false;
        }

        boolean isAdmin = isAdminUser(username);
        return setUserPassword(username, password, isAdmin);
    }

    private boolean userExists(String username) {
        return getEncryptedPassword(username) != null;
    }

    public String getEncryptedPassword(String username) {
        if (!credentialFileExists()) {
            return null;
        }
        try {
            for (String line : readCredentialLines()) {
                String[] parts = parseLine(line);
                if (parts != null && parts[0].equals(username)) {
                    return parts[1];
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    private boolean setUserPassword(String username, String passwordPlaintext, boolean isAdmin) {
        String encryptedPassword = encrypt(passwordPlaintext);
        String role = isAdmin ? "admin" : "user";

        try {
            List<String> lines = readCredentialLines();
            boolean updated = false;
            for (int i = 0; i < lines.size(); i++) {
                String[] parts = parseLine(lines.get(i));
                if (parts != null && parts[0].equals(username)) {
                    // Update password and role
                    lines.set(i, formatLine(username, encryptedPassword, parts.length >= 3 ? parts[2] : role));
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                lines.add(formatLine(username, encryptedPassword, role));
            }
            writeCredentialLines(lines);
            return true;
        } catch (IOException e) {
            System.out.println("Error writing credential file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Promotes a user to admin role. Only existing users can be promoted.
     *
     * @param username the username to promote
     * @return true if the user was successfully promoted, false otherwise
     */
    public boolean promoteToAdmin(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (!userExists(username)) {
            return false;
        }
        if (isAdminUser(username)) {
            return false; // already admin
        }
        try {
            List<String> lines = readCredentialLines();
            for (int i = 0; i < lines.size(); i++) {
                String[] parts = parseLine(lines.get(i));
                if (parts != null && parts[0].equals(username)) {
                    lines.set(i, formatLine(parts[0], parts[1], "admin"));
                    writeCredentialLines(lines);
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error updating credential file: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns a list of all users and their roles.
     *
     * @return list of Map.Entry pairs where key=username, value=role
     */
    public List<Map.Entry<String, String>> getAllUsers() {
        List<Map.Entry<String, String>> users = new ArrayList<>();
        try {
            for (String line : readCredentialLines()) {
                String[] parts = parseLine(line);
                if (parts != null) {
                    String role = parts.length >= 3 ? parts[2] : "user";
                    users.add(new AbstractMap.SimpleEntry<>(parts[0], role));
                }
            }
        } catch (IOException e) {
            // return empty list
        }
        return users;
    }

    public boolean isAdminUser(String username) {
        try {
            for (String line : readCredentialLines()) {
                String[] parts = parseLine(line);
                if (parts != null && parts[0].equals(username)) {
                    return parts.length >= 3 && parts[2].equalsIgnoreCase("admin");
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private List<String> readCredentialLines() throws IOException {
        Path p = Paths.get(credentialFilePath);
        if (!Files.exists(p)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(p);
    }

    private void writeCredentialLines(List<String> lines) throws IOException {
        Files.write(Paths.get(credentialFilePath), lines);
    }

    private static String formatLine(String username, String encryptedPassword, String role) {
        return username + "\t" + (encryptedPassword == null ? "" : encryptedPassword) + "\t" + role;
    }

    private static String[] parseLine(String line) {
        if (line == null) return null;
        String trimmed = line.trim();
        if (trimmed.isEmpty()) return null;

        String[] parts = trimmed.split("\t");
        if (parts.length < 2) return new String[] { parts[0], "" };

        // parts[0] = username, parts[1] = password, parts[2] = role (optional)
        if (parts.length == 2) return new String[] { parts[0], parts[1], "user" }; // default role
        return new String[] { parts[0], parts[1], parts[2] };
    }
}