package com.placement.controllers.candidate;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.Timestamp;
import com.placement.Dashboard;
import com.placement.SceneManager;
import com.placement.models.Offer;
import com.placement.services.AuthService;
import com.placement.services.FirebaseService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DashboardController implements Dashboard {

    @FXML private TableView<Offer> offersTable;
    @FXML private TableColumn<Offer, String> recruiterColumn;
    @FXML private TableColumn<Offer, String> statusColumn;
    @FXML private TableColumn<Offer, Double> salaryColumn;
    @FXML private TableColumn<Offer, Void> actionsColumn;
    @FXML private Text welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private ProgressIndicator loadingIndicator; // Added loading indicator

    private ObservableList<Offer> offersData = FXCollections.observableArrayList();
    private String userId;

    @Override
    public void initializeData(String userId) {
        this.userId = userId;
        loadCandidateName(userId);
        loadOffers();
    }

    private void loadCandidateName(String userId) {
        loadingIndicator.setVisible(true); // Show loading indicator
        ApiFuture<DocumentSnapshot> future = FirebaseService.getFirestore()
            .collection("users")
            .document(userId)
            .get();

        future.addListener(() -> {
            try {
                DocumentSnapshot document = future.get();
                String name = document.getString("name");
                Platform.runLater(() -> {
                    welcomeLabel.setText("Welcome, " + (name != null ? name : "Candidate"));
                    loadingIndicator.setVisible(false); // Hide loading indicator
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    welcomeLabel.setText("Welcome, Candidate");
                    System.err.println("Error loading candidate name: " + e.getMessage());
                    loadingIndicator.setVisible(false); // Hide even on error
                });
            }
        }, Executors.newFixedThreadPool(4));
    }

    private void loadOffers() {
        loadingIndicator.setVisible(true); // Show loading indicator
        ApiFuture<QuerySnapshot> future = FirebaseService.getFirestore()
            .collection("offers")
            .whereEqualTo("candidateId", userId)
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
                    offersTable.setItems(offersData);
                    loadingIndicator.setVisible(false); // Hide loading indicator
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Error loading offers: " + e.getMessage());
                    showAlert("Error", "Failed to load offers: " + e.getMessage());
                    loadingIndicator.setVisible(false); // Hide even on error
                });
            }
        }, Executors.newFixedThreadPool(4));
    }

    @FXML
    private void initialize() {
        recruiterColumn.setCellValueFactory(cellData -> {
            Offer offer = cellData.getValue();
            return new SimpleStringProperty(getCompanyName(offer.getRecruiterId()));
        });

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedSalary"));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button acceptButton = new Button("Accept");
            private final Button rejectButton = new Button("Reject");
            private final HBox pane = new HBox(10, acceptButton, rejectButton);

            {
                acceptButton.setOnAction(event -> {
                    Offer offer = getTableView().getItems().get(getIndex());
                    handleOfferAction(offer, "ACCEPTED");
                });

                rejectButton.setOnAction(event -> {
                    Offer offer = getTableView().getItems().get(getIndex());
                    handleOfferAction(offer, "REJECTED");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        offersTable.setItems(offersData);
    }

    private String getCompanyName(String recruiterId) {
        if (recruiterId == null || recruiterId.isEmpty()) return "No Company Specified";
        try {
            DocumentSnapshot document = FirebaseService.getFirestore()
                .collection("recruiters")
                .document(recruiterId)
                .get()
                .get();
            String companyName = document.getString("companyName");
            return companyName != null && !companyName.isEmpty() ? companyName : "No Company Name";
        } catch (Exception e) {
            System.err.println("Error fetching company name: " + e.getMessage());
            return "Error Loading Company";
        }
    }

    private void handleOfferAction(Offer offer, String newStatus) {
        if (offer.getId() == null) {
            showAlert("Error", "Offer ID is missing. Cannot update status.");
            return;
        }

        loadingIndicator.setVisible(true); // Show loading indicator
        offer.setStatus(newStatus);
        offer.setTimestamp(Timestamp.now());

        ApiFuture<com.google.cloud.firestore.WriteResult> future = FirebaseService.getFirestore()
            .collection("offers")
            .document(offer.getId())
            .update("status", newStatus, "timestamp", Timestamp.now());

        future.addListener(() -> {
            try {
                future.get();
                Platform.runLater(() -> {
                    showAlert("Success", "Offer " + newStatus.toLowerCase() + " successfully.");
                    loadOffers(); // Refresh the table
                    loadingIndicator.setVisible(false); // Hide loading indicator
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to update offer status: " + e.getMessage());
                    System.err.println("Error updating offer: " + e.getMessage());
                    loadingIndicator.setVisible(false); // Hide even on error
                });
            }
        }, Executors.newFixedThreadPool(4));
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
        try {
            SceneManager.loadCandidateProfile(userId);
        } catch (IOException e) {
            System.err.println("Error loading candidate profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            AuthService.logout();
            SceneManager.logout();
        } catch (IOException e) {
            System.err.println("Error during logout: " + e.getMessage());
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