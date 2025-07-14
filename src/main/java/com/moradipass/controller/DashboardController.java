package com.moradipass.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DashboardController {
    @FXML private TableView<PasswordEntry> passwordTable;
    @FXML private TableColumn<PasswordEntry, String> siteColumn;
    @FXML private TableColumn<PasswordEntry, String> usernameColumn;
    @FXML private TableColumn<PasswordEntry, String> passwordColumn;

    private final ObservableList<PasswordEntry> passwordList = FXCollections.observableArrayList(
            new PasswordEntry("gmail.com", "myemail", "password123"),
            new PasswordEntry("github.com", "myuser", "ghp_xxx")
    );

    @FXML
    private void initialize() {
        siteColumn.setCellValueFactory(cell -> cell.getValue().siteProperty());
        usernameColumn.setCellValueFactory(cell -> cell.getValue().usernameProperty());
        passwordColumn.setCellValueFactory(cell -> cell.getValue().passwordProperty());

        passwordTable.setItems(passwordList);
    }

    @FXML private void handleAdd() {
        // to be implemented
        System.out.println("Add clicked");
    }

    @FXML private void handleEdit() {
        // to be implemented
        System.out.println("Edit clicked");
    }

    @FXML private void handleDelete() {
        PasswordEntry selected = passwordTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            passwordList.remove(selected);
        }
    }
}
