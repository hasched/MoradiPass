package com.moradipass.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.moradipass.util.SceneLoader;
import com.moradipass.util.SecurityUtil;
import com.moradipass.data.DatabaseManager;

import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Base64;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField masterPasswordField;
    @FXML private PasswordField confirmMasterPasswordField;
    @FXML private Label errorLabel;

    // The encryption key derived from the master password, available throughout the session
    private static SecretKey sessionEncryptionKey;
    // The salt used for PBKDF2 derivation of the sessionEncryptionKey
    private static byte[] sessionKeyDerivationSalt;

    private static final String MASTER_PASSWORD_HASH_FILE = "master_password.hash";
    private static final String MASTER_KEY_SALT_FILE = "master_key_salt.dat"; // Stores the salt for key derivation

    // This method is called from MainApp to set the initial scene
    public static void showLoginOrSetup(Stage primaryStage) {
        LoginController controller = new LoginController(); // Create an instance to call isFirstRun()
        if (controller.isFirstRun()) {
            SceneLoader.loadScene(primaryStage, "/fxml/initial_setup.fxml", "MoradiPass - Set Master Password");
        } else {
            SceneLoader.loadScene(primaryStage, "/fxml/login.fxml", "MoradiPass - Login");
        }
    }

    private boolean isFirstRun() {
        return !Files.exists(Paths.get(MASTER_PASSWORD_HASH_FILE)) || !Files.exists(Paths.get(MASTER_KEY_SALT_FILE));
    }

    @FXML
    private void handleSetMasterPassword() {
        String masterPassString = masterPasswordField.getText();
        String confirmPassString = confirmMasterPasswordField.getText();
        char[] masterPassChars = masterPassString.toCharArray(); // Use char array for sensitive data

        if (masterPassString.isEmpty() || confirmPassString.isEmpty()) {
            errorLabel.setText("Password fields cannot be empty.");
            return;
        }
        if (!masterPassString.equals(confirmPassString)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }
        if (masterPassString.length() < 8) { // Simple validation
            errorLabel.setText("Master password must be at least 8 characters.");
            return;
        }

        try {
            // Hash the master password for authentication
            String hashedPassword = SecurityUtil.hashMasterPassword(masterPassString);
            Files.write(Paths.get(MASTER_PASSWORD_HASH_FILE), hashedPassword.getBytes());

            // Generate a salt for PBKDF2 key derivation and store it
            byte[] keyDerivationSalt = SecurityUtil.generateSalt();
            Files.write(Paths.get(MASTER_KEY_SALT_FILE), Base64.getEncoder().encodeToString(keyDerivationSalt).getBytes());

            // Derive the encryption key for the current session
            sessionEncryptionKey = SecurityUtil.deriveKeyFromMasterPassword(masterPassChars, keyDerivationSalt);
            sessionKeyDerivationSalt = keyDerivationSalt; // Store salt for session (optional, mainly for DB re-init or re-keying)

            SceneLoader.loadScene((Stage) masterPasswordField.getScene().getWindow(), "/fxml/dashboard.fxml", "MoradiPass - Dashboard");
        } catch (Exception e) { // Catch Exception for security operations
            errorLabel.setText("Error setting master password: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clear sensitive data from memory
            java.util.Arrays.fill(masterPassChars, ' ');
            masterPasswordField.setText("");
            confirmMasterPasswordField.setText("");
        }
    }

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String passString = passwordField.getText();
        char[] passChars = passString.toCharArray(); // Use char array for sensitive data

        if (!"master".equals(user)) {
            errorLabel.setText("Invalid username.");
            return;
        }

        try {
            String storedHashedPassword = new String(Files.readAllBytes(Paths.get(MASTER_PASSWORD_HASH_FILE)));
            byte[] storedKeyDerivationSalt = Base64.getDecoder().decode(new String(Files.readAllBytes(Paths.get(MASTER_KEY_SALT_FILE))));

            if (SecurityUtil.checkMasterPassword(passString, storedHashedPassword)) {
                // Master password matches, derive encryption key for the session
                sessionEncryptionKey = SecurityUtil.deriveKeyFromMasterPassword(passChars, storedKeyDerivationSalt);
                sessionKeyDerivationSalt = storedKeyDerivationSalt; // Store salt for session

                SceneLoader.loadScene((Stage) usernameField.getScene().getWindow(), "/fxml/dashboard.fxml", "MoradiPass - Dashboard");
            } else {
                errorLabel.setText("Invalid credentials");
            }
        } catch (Exception e) { // Catch Exception for security operations
            errorLabel.setText("Login error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clear sensitive data from memory
            java.util.Arrays.fill(passChars, ' ');
            usernameField.setText("");
            passwordField.setText("");
        }
    }

    // Getters for the key and salt to be used by DatabaseManager and other components
    public static SecretKey getSessionEncryptionKey() {
        return sessionEncryptionKey;
    }

    public static byte[] getSessionKeyDerivationSalt() {
        return sessionKeyDerivationSalt;
    }
}