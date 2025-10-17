package com.placement.controllers.recruiter;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.placement.Dashboard;
import com.placement.SceneManager;
import com.placement.models.Candidate;
import com.placement.models.Offer;
import com.placement.services.AuthService;
import com.placement.services.FirebaseService;
import com.google.cloud.Timestamp;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class RecruiterDashboardController implements Dashboard {

    @FXML private TableView<Candidate> candidatesTable;
    @FXML private TableColumn<Candidate, String> nameColumn;
    @FXML private TableColumn<Candidate, Double> marksColumn;
    @FXML private TableColumn<Candidate, String> qualificationColumn;
    @FXML private TableColumn<Candidate, Void> cvColumn;
    @FXML private TextField minMarksField;
    @FXML private ComboBox<String> qualificationFilter;
    @FXML private TableView<Offer> offersTable;
    @FXML private TableColumn<Offer, String> candidateNameColumn;
    @FXML private TableColumn<Offer, String> offerStatusColumn;
    @FXML private TableColumn<Offer, Double> salaryOfferColumn;
    @FXML private Button logoutButton;
    @FXML private ProgressIndicator loadingIndicator; // Added loading indicator

    private ObservableList<Candidate> candidatesData = FXCollections.observableArrayList();
    private ObservableList<Candidate> filteredCandidates = FXCollections.observableArrayList();
    private ObservableList<Offer> offersData = FXCollections.observableArrayList();
    private String recruiterId;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        marksColumn.setCellValueFactory(cellData -> cellData.getValue().marksProperty().asObject());
        qualificationColumn.setCellValueFactory(new PropertyValueFactory<>("qualification"));

        cvColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewCvButton = new Button("View CV");

            {
                viewCvButton.setOnAction(event -> {
                    Candidate candidate = getTableView().getItems().get(getIndex());
                    viewCandidateCV(candidate);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewCvButton);
            }
        });

        qualificationFilter.getItems().addAll("All", "B.Tech", "M.Tech", "BE", "BCA", "MCA", "Diploma in CS", "Diploma in IT", "Diploma in BIOMED", "Diploma in MECH", "B.COM", "M.COM", "CA", "BA", "BBA", "LLB", "B.Ed", "Other");
        qualificationFilter.setValue("All");

        candidatesTable.setItems(filteredCandidates);

        candidateNameColumn.setCellValueFactory(cellData -> {
            Offer offer = cellData.getValue();
            return new SimpleStringProperty(getCandidateName(offer.getCandidateId()));
        });
        offerStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        salaryOfferColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedSalary"));

        offersTable.setItems(offersData);
    }

    @Override
    public void initializeData(String userId) {
        this.recruiterId = userId;
        if (recruiterId == null) {
            showAlert("Error", "Recruiter ID is null. Please log in again.");
            return;
        }
        loadCandidates();
        loadOffers();
    }

    private void loadCandidates() {
        loadingIndicator.setVisible(true); // Show loading indicator
        ApiFuture<QuerySnapshot> future = FirebaseService.getFirestore()
            .collection("candidates")
            .get();

        future.addListener(() -> {
            try {
                QuerySnapshot snapshot = future.get();
                List<QueryDocumentSnapshot> documents = snapshot.getDocuments();
                Platform.runLater(() -> {
                    candidatesData.setAll(documents.stream()
                        .map(doc -> {
                            Candidate candidate = doc.toObject(Candidate.class);
                            candidate.setId(doc.getId());
                            return candidate;
                        })
                        .collect(Collectors.toList()));
                    filterCandidates();
                    loadingIndicator.setVisible(false); // Hide loading indicator
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Error loading candidates: " + e.getMessage());
                    loadingIndicator.setVisible(false); // Hide even on error
                });
            }
        }, Executors.newFixedThreadPool(4));
    }

    private void loadOffers() {
        loadingIndicator.setVisible(true); // Show loading indicator
        ApiFuture<QuerySnapshot> future = FirebaseService.getFirestore()
            .collection("offers")
            .whereEqualTo("recruiterId", recruiterId)
            .get();

        future.addListener(() -> {
            try {
                QuerySnapshot snapshot = future.get();
                List<QueryDocumentSnapshot> documents = snapshot.getDocuments();
                Platform.runLater(() -> {
                    offersData.setAll(documents.stream()
                        .map(doc -> {
                            Offer offer = doc.toObject(Offer.class);
                            offer.setId(doc.getId());
                            return offer;
                        })
                        .collect(Collectors.toList()));
                    loadingIndicator.setVisible(false); // Hide loading indicator
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Error loading offers: " + e.getMessage());
                    loadingIndicator.setVisible(false); // Hide even on error
                });
            }
        }, Executors.newFixedThreadPool(4));
    }

    private void filterCandidates() {
        double minMarks = minMarksField.getText().isEmpty() ? 0 : Double.parseDouble(minMarksField.getText());
        String qualification = qualificationFilter.getValue();

        filteredCandidates.setAll(candidatesData.stream()
            .filter(candidate -> candidate.getMarks() >= minMarks &&
                (qualification.equals("All") || candidate.getQualification().equals(qualification)))
            .collect(Collectors.toList()));

        candidatesTable.setItems(filteredCandidates);
    }

    @FXML
    private void handleFilter() {
        try {
            filterCandidates();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid minimum marks value");
        }
    }

    @FXML
    private void handleHire() {
        Candidate selected = candidatesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a candidate first");
            return;
        }

        if (recruiterId == null) {
            showAlert("Error", "Recruiter ID is not set. Please log in again.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Hire Candidate");
        dialog.setHeaderText("Enter Estimated Salary for " + selected.getName());
        dialog.setContentText("Estimated Salary (in currency units):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double estimatedSalary = Double.parseDouble(result.get());
                if (estimatedSalary <= 0) {
                    showAlert("Error", "Salary must be a positive number");
                    return;
                }

                Offer offer = new Offer();
                offer.setCandidateId(selected.getId());
                offer.setRecruiterId(recruiterId);
                offer.setStatus("PENDING");
                offer.setTimestamp(Timestamp.now());
                offer.setEstimatedSalary(estimatedSalary);

                loadingIndicator.setVisible(true); // Show loading indicator
                ApiFuture<com.google.cloud.firestore.DocumentReference> future = FirebaseService.getFirestore()
                    .collection("offers")
                    .add(offer);

                future.addListener(() -> {
                    try {
                        future.get();
                        Platform.runLater(() -> {
                            showAlert("Success", "Hiring request sent for " + selected.getName() + " with salary: " + estimatedSalary);
                            loadOffers(); // Refresh the offers table
                            loadingIndicator.setVisible(false); // Hide loading indicator
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showAlert("Error", "Error sending hire request: " + e.getMessage());
                            loadingIndicator.setVisible(false); // Hide even on error
                        });
                    }
                }, Executors.newFixedThreadPool(4));
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid salary amount");
            }
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

    private void viewCandidateCV(Candidate candidate) {
        String base64Cv = candidate.getCvUrl();
        if (base64Cv == null || base64Cv.trim().isEmpty()) {
            showAlert("Error", "No CV available for " + candidate.getName());
            return;
        }

        try {
            byte[] pdfBytes = Base64.getDecoder().decode(base64Cv);
            File tempFile = File.createTempFile("candidate_cv_" + candidate.getId(), ".pdf");
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdfBytes);
            }

            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(tempFile);
            } else {
                showAlert("Error", "Desktop operations are not supported on this system.");
            }
        } catch (Exception e) {
            showAlert("Error", "Unable to open CV for " + candidate.getName() + ": " + e.getMessage());
        }
    }

    private String getCandidateName(String candidateId) {
        if (candidateId == null || candidateId.isEmpty()) return "Unknown Candidate";
        try {
            DocumentSnapshot document = FirebaseService.getFirestore()
                .collection("candidates")
                .document(candidateId)
                .get()
                .get();
            String name = document.getString("name");
            return name != null ? name : "Unknown Candidate";
        } catch (Exception e) {
            System.err.println("Error fetching candidate name: " + e.getMessage());
            return "Error Loading Name";
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