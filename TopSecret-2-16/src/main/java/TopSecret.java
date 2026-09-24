import controller.*;
import models.Password;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class TopSecret {

    private static final String CREDENTIAL_FILE = "credentials.cip";
    private static final String KEY_PATH = "ciphers/key.txt";
    private static DatabaseManager db;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Password password;
        DatabaseManager databaseManager = new DatabaseManager();

        try {
            password = new Password(CREDENTIAL_FILE, KEY_PATH, scanner);
        } catch (IOException e) {
            System.out.println("Error loading cipher key: " + e.getMessage());
            return;
        }

        // If no credentials exist yet, prompt to create first account, then exit
        if (!password.credentialFileExists() || password.isCredentialsEmpty()) {
            System.out.println("No credential file found. Please create a new account.");
            boolean created = password.createCredentials();
            if (created) {
                ProjectControl pc = new ProjectControl(new FileHandler(), databaseManager);
                pc.recordUserAccountCreated(password.getCurrentUsername(), password.getCurrentUsername());
            }
        }

        boolean isWeb = Arrays.asList(args).contains("-w");
        if (isWeb & args.length > 1) {
            System.out.println("Additional command line arguments cannot be processed in web view. Please rerun without '-w'.");
        }

        // Authentication successful — launch the main UI
        if (isWeb) {
            WebController wc = new WebController(password);
            wc.run();
        }
        else {
            try {
                UserInterface ui = new UserInterface(password, scanner, databaseManager);
                ui.run(args);
            } catch (RuntimeException e) {
                System.out.println("Could not start session: " + e.getMessage());
            }
        }
    }
}