package com.moradipass.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneLoader {
    public static void loadScene(Stage stage, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource(fxmlPath));
            Parent root = loader.load();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
