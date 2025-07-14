package com.moradipass;

import javafx.application.Application;
import javafx.scene.image.Image; // NEW Import
import javafx.stage.Stage;
import com.moradipass.controller.LoginController;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Set the application icon
        // Make sure "images" folder is directly under "resources"
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png"))); // NEW Line

        LoginController.showLoginOrSetup(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}