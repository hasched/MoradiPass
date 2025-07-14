package com.moradipass.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.moradipass.util.SceneLoader;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if ("admin".equals(user) && "1234".equals(pass)) {
            SceneLoader.loadScene((Stage) usernameField.getScene().getWindow(), "/fxml/dashboard.fxml", "Dashboard");
        } else {
            errorLabel.setText("Invalid credentials");
        }
    }
}
