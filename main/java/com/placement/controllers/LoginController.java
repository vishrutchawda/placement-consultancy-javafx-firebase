package com.placement.controllers;

import com.placement.SceneManager;
import com.placement.services.AuthService;
import com.placement.services.FirebaseService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    private void initialize() {
        System.out.println("LoginController initialized");
        try {
            FirebaseService.initialize();
        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase: " + e.getMessage());
            showAlert("Error", "Failed to initialize Firebase: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both email and password.");
            return;
        }

        new Thread(() -> {
            String userId = AuthService.authenticateUser(email, password);
            Platform.runLater(() -> {
                if (userId != null) {
                    String role = fetchRoleFromDatabase(email, userId);
                    if (role == null) {
                        showAlert("Error", "Could not determine user role.");
                        return;
                    }
                    try {
                        switch (role.toLowerCase()) {
                            case "candidate":
                                SceneManager.loadCandidateDashboard(userId);
                                break;
                            case "recruiter":
                                SceneManager.loadRecruiterDashboard(userId);
                                break;
                            default:
                                showAlert("Error", "Unknown role: " + role);
                        }
                    } catch (IOException e) {
                        showAlert("Error", "Error loading dashboard: " + e.getMessage());
                    }
                } else {
                    showAlert("Error", "Invalid credentials");
                }
            });
        }).start();
    }

    @FXML
    private void handleForgotPassword() {
        // Create dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset your password");

        // Create input fields
        TextField emailInput = new TextField();
        emailInput.setPromptText("Enter your registered email");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailInput, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        ButtonType resetButtonType = new ButtonType("Reset", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(resetButtonType, ButtonType.CANCEL);

        // Enable/disable reset button based on input validation
        Button resetButton = (Button) dialog.getDialogPane().lookupButton(resetButtonType);
        resetButton.setDisable(true);

        // Validation listener
        Runnable updateButtonState = () -> {
            String email = emailInput.getText().trim();
            String newPass = newPasswordField.getText();
            String confirmPass = confirmPasswordField.getText();
            resetButton.setDisable(
                email.isEmpty() ||
                newPass.isEmpty() ||
                confirmPass.isEmpty() ||
                !newPass.equals(confirmPass)
            );
        };

        emailInput.textProperty().addListener((obs, old, newVal) -> updateButtonState.run());
        newPasswordField.textProperty().addListener((obs, old, newVal) -> updateButtonState.run());
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> updateButtonState.run());

        // Handle dialog result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == resetButtonType) {
            String email = emailInput.getText().trim();
            String newPassword = newPasswordField.getText();

            new Thread(() -> {
                try {
                    boolean success = AuthService.resetPassword(email, newPassword);
                    Platform.runLater(() -> {
                        if (success) {
                            showAlert("Success", "Password has been successfully reset");
                        } else {
                            showAlert("Error", "Failed to reset password. Email not found.");
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() ->
                        showAlert("Error", "Error resetting password: " + e.getMessage())
                    );
                }
            }).start();
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        try {
            SceneManager.loadSignUpPage();
        } catch (IOException e) {
            showAlert("Error", "Failed to load signup page: " + e.getMessage());
        }
    }

    private String fetchRoleFromDatabase(String email, String userId) {
        try {
            Firestore db = FirebaseService.getFirestore();
            ApiFuture<QuerySnapshot> query = db.collection("users")
                .whereEqualTo("email", email)
                .get();
            QuerySnapshot result = query.get();
            if (!result.isEmpty()) {
                return result.getDocuments().get(0).getString("role");
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching role: " + e.getMessage());
            return null;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if ("Error".equals(title)) {
            alert.setAlertType(Alert.AlertType.ERROR);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}