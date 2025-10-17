package com.placement.controllers.candidate;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.WriteResult;
import com.placement.SceneManager;
import com.placement.models.Candidate;
import com.placement.services.AuthService;
import com.placement.services.FirebaseService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.Executors;

public class ProfileController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField marksField;
    @FXML private ComboBox<String> qualificationCombo;
    @FXML private Button logoutButton;
    @FXML private ProgressIndicator loadingIndicator; // Added loading indicator

    private Candidate candidate;
    private String userId;

    @FXML
    private void initialize() {
        qualificationCombo.getItems().addAll("B.Tech", "M.Tech", "BE", "BCA", "MCA", "Diploma in CS", "Diploma in IT", "Diploma in BIOMED", "Diploma in MECH", "B.COM", "M.COM", "CA", "BA", "BBA", "LLB", "B.Ed", "Other");
    }

    public void initializeData(String userId) {
        this.userId = userId;
        loadCandidateData();
    }

    private void loadCandidateData() {
        loadingIndicator.setVisible(true); // Show loading indicator
        ApiFuture<DocumentSnapshot> future = FirebaseService.getFirestore()
            .collection("candidates")
            .document(userId)
            .get();

        future.addListener(() -> {
            try {
                DocumentSnapshot document = future.get();
                candidate = document.toObject(Candidate.class);
                if (candidate != null) {
                    Platform.runLater(() -> {
                        nameField.setText(candidate.getName() != null ? candidate.getName() : "");
                        emailField.setText(candidate.getEmail() != null ? candidate.getEmail() : "");
                        marksField.setText(String.valueOf(candidate.getMarks()));
                        qualificationCombo.setValue(candidate.getQualification() != null ? candidate.getQualification() : "");
                        loadingIndicator.setVisible(false); // Hide loading indicator
                    });
                } else {
                    Platform.runLater(() -> {
                        candidate = new Candidate();
                        candidate.setId(userId);
                        loadingIndicator.setVisible(false); // Hide loading indicator
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to load profile: " + e.getMessage());
                    loadingIndicator.setVisible(false); // Hide even on error
                });
            }
        }, Executors.newFixedThreadPool(4));
    }

    @FXML
    private void handleUploadCV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                String base64CV = Base64.getEncoder().encodeToString(fileContent);

                if (candidate == null) candidate = new Candidate();
                candidate.setId(userId);
                candidate.setCvUrl(base64CV);

                loadingIndicator.setVisible(true); // Show loading indicator
                ApiFuture<WriteResult> future = FirebaseService.getFirestore()
                    .collection("candidates")
                    .document(userId)
                    .update("cvUrl", base64CV);

                future.addListener(() -> {
                    try {
                        WriteResult result = future.get();
                        Platform.runLater(() -> {
                            showAlert("Success", "CV uploaded successfully! At " + result.getUpdateTime());
                            loadingIndicator.setVisible(false); // Hide loading indicator
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showAlert("Error", "Error uploading CV: " + e.getMessage());
                            loadingIndicator.setVisible(false); // Hide even on error
                        });
                    }
                }, Executors.newFixedThreadPool(4));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Error uploading CV: " + e.getMessage()));
            }
        }
    }

    @FXML
    private void handleSaveProfile() {
        try {
            String marksText = marksField.getText().trim();
            if (marksText.isEmpty()) {
                showAlert("Error", "Marks cannot be empty");
                return;
            }

            double marks;
            try {
                marks = Double.parseDouble(marksText);
                if (marks < 0 || marks > 100) {
                    showAlert("Error", "Marks must be between 0 and 100");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid marks format");
                return;
            }

            if (candidate == null) candidate = new Candidate();
            candidate.setId(userId);
            candidate.setName(nameField.getText());
            candidate.setEmail(emailField.getText());
            candidate.setMarks(marks);
            candidate.setQualification(qualificationCombo.getValue());

            loadingIndicator.setVisible(true); // Show loading indicator
            ApiFuture<WriteResult> future = FirebaseService.getFirestore()
                .collection("candidates")
                .document(userId)
                .set(candidate);

            future.addListener(() -> {
                try {
                    WriteResult result = future.get();
                    Platform.runLater(() -> {
                        showAlert("Success", "Profile saved successfully! At " + result.getUpdateTime());
                        try {
                            SceneManager.loadCandidateDashboard(userId);
                        } catch (IOException e) {
                            showAlert("Error", "Failed to load dashboard: " + e.getMessage());
                        }
                        loadingIndicator.setVisible(false); // Hide loading indicator
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert("Error", "Error saving profile: " + e.getMessage());
                        loadingIndicator.setVisible(false); // Hide even on error
                    });
                }
            }, Executors.newFixedThreadPool(4));
        } catch (Exception e) {
            showAlert("Error", "Error saving profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            AuthService.logout();
            SceneManager.logout();
        } catch (IOException e) {
            showAlert("Error", "Logout failed: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if ("Error".equals(title)) alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}