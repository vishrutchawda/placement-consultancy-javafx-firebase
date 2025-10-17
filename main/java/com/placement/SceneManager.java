package com.placement;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.placement.controllers.candidate.ProfileController;
import java.io.IOException;

public class SceneManager {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Placement Consultancy System");
    }

    public static void loadLoginPage() throws IOException {
        loadScene("/views/login.fxml", "Login - Placement Consultancy System");
    }

    public static void loadSignUpPage() throws IOException {
        loadScene("/views/signup.fxml", "Sign Up - Placement Consultancy System");
    }

    public static void loadCandidateDashboard(String userId) throws IOException {
        loadDashboard("/views/candidate/dashboard.fxml", "Candidate Dashboard", userId);
    }

    public static void loadRecruiterDashboard(String userId) throws IOException {
        loadDashboard("/views/recruiter/dashboard.fxml", "Recruiter Dashboard", userId);
    }

    public static void loadCandidateProfile(String userId) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/views/candidate/profile.fxml"));
        Parent root = loader.load();

        ProfileController controller = loader.getController();
        if (controller != null) {
            controller.initializeData(userId);
        } else {
            System.err.println("Controller is null for profile");
        }

        Scene scene = new Scene(root, 1520, 800);
        scene.getStylesheets().add(SceneManager.class.getResource("/styles/main.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Edit Profile - Placement Consultancy System");
        primaryStage.show();
    }

    // New logout method
    public static void logout() throws IOException {
        loadLoginPage();
    }

    private static void loadScene(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1520, 800);
        scene.getStylesheets().add(SceneManager.class.getResource("/styles/main.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.show();
    }

    private static void loadDashboard(String fxmlPath, String title, String userId) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();

        Dashboard controller = loader.getController();
        if (controller != null) {
            controller.initializeData(userId);
        } else {
            System.err.println("Controller is null for dashboard");
        }

        Scene scene = new Scene(root, 1520, 800);
        scene.getStylesheets().add(SceneManager.class.getResource("/styles/main.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle(title + " - Placement Consultancy System");
        primaryStage.show();
    }
}