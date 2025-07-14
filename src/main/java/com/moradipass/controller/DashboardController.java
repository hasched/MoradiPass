package com.moradipass.controller;

import com.moradipass.PasswordEntry;
import com.moradipass.data.DatabaseManager;
import com.moradipass.util.SceneLoader; // Import SceneLoader if not already there

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class DashboardController {
    @FXML private TableView<PasswordEntry> passwordTable;
    @FXML private TableColumn<PasswordEntry, String> siteColumn;
    @FXML private TableColumn<PasswordEntry, String> usernameColumn;
    @FXML private TableColumn<PasswordEntry, String> passwordColumn;

    private DatabaseManager dbManager;
    private ObservableList<PasswordEntry> passwordList;

    @FXML
    private void initialize() {
        dbManager = new DatabaseManager();

        siteColumn.setCellValueFactory(cell -> cell.getValue().siteProperty());
        usernameColumn.setCellValueFactory(cell -> cell.getValue().usernameProperty());
        passwordColumn.setCellValueFactory(cell -> cell.getValue().passwordProperty());

        // Load passwords from the database
        passwordList = dbManager.getAllEntries();
        passwordTable.setItems(passwordList);
    }

    @FXML
    private void handleAdd() {
        PasswordEntry newEntry = new PasswordEntry("", "", ""); // Create an empty entry

        Optional<PasswordEntry> resultEntry = showAddEditDialog(newEntry); // Call the dialog

        if (resultEntry.isPresent()) {
            PasswordEntry entryToSave = resultEntry.get();
            dbManager.addEntry(entryToSave); // Add to database (encrypted)
            passwordList.add(entryToSave); // Add to ObservableList (UI)
            System.out.println("Add new entry successful: " + entryToSave.getSite());
        } else {
            System.out.println("Add operation cancelled.");
        }
    }

    @FXML
    private void handleEdit() {
        PasswordEntry selectedEntry = passwordTable.getSelectionModel().getSelectedItem();
        if (selectedEntry != null) {
            // Pass the selectedEntry. The dialog controller will modify this same object.
            Optional<PasswordEntry> resultEntry = showAddEditDialog(selectedEntry);
            if (resultEntry.isPresent()) {
                // No need to assign resultEntry.get() back to selectedEntry here,
                // as the dialog controller already modified 'selectedEntry' itself.
                dbManager.updateEntry(selectedEntry); // Update in database (re-encrypted)
                passwordTable.refresh(); // Refresh the table to show changes
                System.out.println("Edit entry successful for ID: " + selectedEntry.getId());
            } else {
                System.out.println("Edit operation cancelled.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No Password Entry Selected", "Please select an entry in the table.");
        }
    }

    @FXML
    private void handleDelete() {
        PasswordEntry selected = passwordTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Password Entry");
            alert.setContentText("Are you sure you want to delete the entry for '" + selected.getSite() + "'?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                dbManager.deleteEntry(selected.getId()); // Use ID for robust deletion
                passwordList.remove(selected); // Remove from ObservableList
                System.out.println("Entry with ID " + selected.getId() + " deleted.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No Password Entry Selected", "Please select an entry in the table.");
        }
    }

    // Helper method to show the Add/Edit dialog
    private Optional<PasswordEntry> showAddEditDialog(PasswordEntry entry) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/add_edit_dialog.fxml"));
            DialogPane dialogPane = loader.load();

            AddEditDialogController controller = loader.getController();
            controller.setPasswordEntry(entry); // Pass the entry to the controller

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Password Entry Details");
            dialog.initModality(Modality.APPLICATION_MODAL); // Make it a modal window

            // Set the result converter for the dialog
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    // When 'Save' is clicked, validate and return the populated PasswordEntry
                    if (controller.isValidInput()) {
                        controller.setOkClicked(true); // Indicate success for sanity, though not strictly needed here
                        return ButtonType.OK; // Return OK to close dialog
                    } else {
                        // If validation fails, do not close the dialog
                        return null; // Keep dialog open if invalid
                    }
                }
                return null; // For Cancel or other buttons, closes dialog with empty Optional
            });

            Optional<ButtonType> result = dialog.showAndWait();

            // If OK was clicked AND the input was valid (controller.isOkClicked() will be true),
            // then return the populated PasswordEntry from the controller.
            // Otherwise, return empty.
            if (result.isPresent() && result.get() == ButtonType.OK && controller.isOkClicked()) {
                return Optional.of(controller.getPasswordEntry()); // This now returns the modified 'entry' instance
            } else {
                return Optional.empty();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open dialog", "An error occurred while loading the entry dialog.");
            return Optional.empty();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}