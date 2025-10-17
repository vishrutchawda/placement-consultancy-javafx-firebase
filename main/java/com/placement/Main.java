package com.placement;

import javafx.application.Application;
import javafx.stage.Stage;
import com.placement.services.FirebaseService;
import java.io.IOException;


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            FirebaseService.initialize();
            SceneManager.setPrimaryStage(primaryStage);
            SceneManager.loadLoginPage();
        } catch (IOException e) {
            System.err.println("Failed to initialize application: " + e.getMessage());
            throw e;
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}