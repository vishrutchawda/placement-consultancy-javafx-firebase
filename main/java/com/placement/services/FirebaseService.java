package com.placement.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.IOException;
import java.io.InputStream;

public class FirebaseService {
    private static Firestore firestore;

    public static void initialize() throws IOException {
        try (InputStream serviceAccount = FirebaseService.class.getClassLoader().getResourceAsStream("firebase_config.json")) {
            if (serviceAccount == null) {
                throw new IOException("Firebase config file 'firebase_config.json' not found in resources.");
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            firestore = FirestoreClient.getFirestore();
            System.out.println("Firebase initialized successfully");
        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase: " + e.getMessage());
            throw e;
        }
    }

    public static Firestore getFirestore() {
        if (firestore == null) {
            throw new IllegalStateException("Firestore not initialized. Call initialize() first.");
        }
        return firestore;
    }
}