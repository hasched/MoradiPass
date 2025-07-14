package com.moradipass.data;

import com.moradipass.PasswordEntry;
import com.moradipass.controller.LoginController;
import com.moradipass.util.SecurityUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.crypto.SecretKey;
import java.sql.*;
import java.util.Base64;

public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:moradipass.db";

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS password_entries (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "site TEXT NOT NULL," +
                    "username TEXT NOT NULL," +
                    "password TEXT NOT NULL," +
                    "iv TEXT NOT NULL" +
                    ");";
            stmt.execute(sql);
            System.out.println("Database and table 'password_entries' initialized.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public ObservableList<PasswordEntry> getAllEntries() {
        ObservableList<PasswordEntry> entries = FXCollections.observableArrayList();
        String sql = "SELECT id, site, username, password, iv FROM password_entries";

        // --- FIX START ---
        SecretKey encryptionKey = LoginController.getSessionEncryptionKey(); // Corrected method call
        // --- FIX END ---
        if (encryptionKey == null) {
            System.err.println("Encryption key not set. Cannot load encrypted data.");
            return entries; // Return empty list or handle error
        }

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String site = rs.getString("site");
                String encryptedUsername = rs.getString("username");
                String encryptedPassword = rs.getString("password");
                byte[] iv = Base64.getDecoder().decode(rs.getString("iv"));

                // Decrypt username and password
                String decryptedUsername = SecurityUtil.decrypt(encryptedUsername, encryptionKey, iv);
                String decryptedPassword = SecurityUtil.decrypt(encryptedPassword, encryptionKey, iv);

                entries.add(new PasswordEntry(id, site, decryptedUsername, decryptedPassword));
            }
        } catch (Exception e) {
            System.err.println("Error retrieving and decrypting entries: " + e.getMessage());
            // It's critical here to inform the user if decryption fails (e.g., wrong master password)
        }
        return entries;
    }

    public void addEntry(PasswordEntry entry) {
        String sql = "INSERT INTO password_entries(site, username, password, iv) VALUES(?, ?, ?, ?)";

        // --- FIX START ---
        SecretKey encryptionKey = LoginController.getSessionEncryptionKey(); // Corrected method call
        // --- FIX END ---
        if (encryptionKey == null) {
            System.err.println("Encryption key not set. Cannot add encrypted data.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            byte[] iv = SecurityUtil.generateIv(); // Generate a new IV for THIS entry
            String encryptedUsername = SecurityUtil.encrypt(entry.getUsername(), encryptionKey, iv);
            String encryptedPassword = SecurityUtil.encrypt(entry.getPassword(), encryptionKey, iv);

            pstmt.setString(1, entry.getSite());
            pstmt.setString(2, encryptedUsername);
            pstmt.setString(3, encryptedPassword);
            pstmt.setString(4, Base64.getEncoder().encodeToString(iv));

            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                entry.setId(generatedKeys.getInt(1));
            }

            System.out.println("Entry added: " + entry.getSite());
        } catch (Exception e) {
            System.err.println("Error adding entry: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateEntry(PasswordEntry entry) {
        if (entry.getId() == -1) {
            System.err.println("Cannot update entry without a valid ID.");
            return;
        }
        String sql = "UPDATE password_entries SET site = ?, username = ?, password = ?, iv = ? WHERE id = ?";

        // --- FIX START ---
        SecretKey encryptionKey = LoginController.getSessionEncryptionKey(); // Corrected method call
        // --- FIX END ---
        if (encryptionKey == null) {
            System.err.println("Encryption key not set. Cannot update encrypted data.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            byte[] iv = SecurityUtil.generateIv(); // Generate a new IV for THIS update
            String encryptedUsername = SecurityUtil.encrypt(entry.getUsername(), encryptionKey, iv);
            String encryptedPassword = SecurityUtil.encrypt(entry.getPassword(), encryptionKey, iv);

            pstmt.setString(1, entry.getSite());
            pstmt.setString(2, encryptedUsername);
            pstmt.setString(3, encryptedPassword);
            pstmt.setString(4, Base64.getEncoder().encodeToString(iv));
            pstmt.setInt(5, entry.getId());
            pstmt.executeUpdate();
            System.out.println("Entry updated: " + entry.getSite() + " (ID: " + entry.getId() + ")");
        } catch (Exception e) {
            System.err.println("Error updating entry: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteEntry(int id) {
        String sql = "DELETE FROM password_entries WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Entry deleted by ID: " + id);
        } catch (SQLException e) {
            System.err.println("Error deleting entry: " + e.getMessage());
            e.printStackTrace();
        }
    }
}