package com.placement.controllers;

import com.placement.SceneManager;
import com.placement.services.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class SignUpController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private TextField companyField;  // Added company field
    @FXML private VBox companyBox;        // Added company container

    @FXML
    private void initialize() {
        roleCombo.getItems().addAll("Candidate", "Recruiter");
        roleCombo.setValue("Candidate");

        // Initially hide company field
        if (companyBox != null) {
            companyBox.setVisible(false);
        }

        // Show/hide company field based on role
        roleCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isRecruiter = "Recruiter".equals(newVal);
            if (companyBox != null) {
                companyBox.setVisible(isRecruiter);
            }
            if (isRecruiter && companyField != null) {
                companyField.setText("");
            }
        });
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleCombo.getValue();

        // Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match");
            return;
        }

        if ("Recruiter".equals(role) && (companyField == null || companyField.getText().trim().isEmpty())) {
            showAlert("Error", "Company name is required for recruiters");
            return;
        }

        new Thread(() -> {
            String userId;
            if ("Recruiter".equals(role)) {
                userId = AuthService.registerRecruiter(
                    name,
                    email,
                    password,
                    companyField.getText().trim()
                );
            } else {
                userId = AuthService.registerUser(
                    name,
                    email,
                    password,
                    role.toLowerCase()
                );
            }

            Platform.runLater(() -> {
                if (userId != null) {
                    showAlert("Success", "Registration successful! Please log in.");
                    try {
                        SceneManager.loadLoginPage();
                    } catch (IOException e) {
                        showAlert("Error", "Error loading login page: " + e.getMessage());
                    }
                } else {
                    showAlert("Error", "Registration failed. Email may already be in use.");
                }
            });
        }).start();
    }

    @FXML
    private void handleLoginRedirect(ActionEvent event) {
        try {
            SceneManager.loadLoginPage();
        } catch (IOException e) {
            showAlert("Error", "Error loading login page: " + e.getMessage());
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