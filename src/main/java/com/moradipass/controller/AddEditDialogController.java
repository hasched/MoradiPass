package com.moradipass.controller;

import com.moradipass.PasswordEntry;
import com.moradipass.util.PasswordGenerator; // Will create this next
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AddEditDialogController {
    @FXML private TextField siteField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField; // For showing password
    @FXML private CheckBox showPasswordCheckbox;
    @FXML private Label errorLabel;

    private PasswordEntry passwordEntry; // The entry being edited or created
    private boolean okClicked = false; // To know if Save was clicked

    @FXML
    private void initialize() {
        // Bind the text properties bidirectionally so they always have the same content
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    /**
     * Sets the password entry to be edited in the dialog.
     * If null, it means a new entry is being created.
     */
    public void setPasswordEntry(PasswordEntry entry) {
        this.passwordEntry = entry;
        if (entry != null) {
            siteField.setText(entry.getSite());
            usernameField.setText(entry.getUsername());
            passwordField.setText(entry.getPassword()); // Set to passwordField first
            visiblePasswordField.setText(entry.getPassword()); // Also set to visible for consistency
        }
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Returns the PasswordEntry object from the dialog fields.
     */
    public PasswordEntry getPasswordEntry() {
        if (passwordEntry == null) {
            passwordEntry = new PasswordEntry(siteField.getText(), usernameField.getText(), passwordField.getText());
        } else {
            passwordEntry.setSite(siteField.getText());
            passwordEntry.setUsername(usernameField.getText());
            passwordEntry.setPassword(passwordField.getText());
        }
        return passwordEntry;
    }

    @FXML
    private void togglePasswordVisibility() {
        if (showPasswordCheckbox.isSelected()) {
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
        }
    }

    @FXML
    private void handleGeneratePassword() {
        String generatedPassword = PasswordGenerator.generateRandomPassword(16, true, true, true, true);
        passwordField.setText(generatedPassword);
        // visiblePasswordField is already bound to passwordField, so it updates automatically
    }

    // Input validation (called before the dialog closes, often in DashboardController)
    public boolean isValidInput() {
        String errorMessage = "";

        if (siteField.getText() == null || siteField.getText().isEmpty()) {
            errorMessage += "No valid site!\n";
        }
        if (usernameField.getText() == null || usernameField.getText().isEmpty()) {
            errorMessage += "No valid username!\n";
        }
        if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
            errorMessage += "No valid password!\n";
        }

        if (errorMessage.isEmpty()) {
            errorLabel.setText("");
            return true;
        } else {
            errorLabel.setText(errorMessage);
            return false;
        }
    }

    // This method needs to be called by the parent controller (DashboardController)
    // when the "Save" button is clicked on the DialogPane.
    // DialogPane doesn't directly expose onAction for its buttons like regular buttons.
    // We'll handle this in DashboardController's showAddEditDialog method.
    public void setOkClicked(boolean okClicked) {
        this.okClicked = okClicked;
    }
}