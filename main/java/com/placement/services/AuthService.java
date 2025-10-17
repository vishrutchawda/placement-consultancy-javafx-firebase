package com.placement.services;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.WriteResult;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AuthService {
    public static String authenticateUser(String email, String password) {
        try {
            Firestore db = FirebaseService.getFirestore();
            String hashedPassword = hashPassword(password);

            List<QueryDocumentSnapshot> users = db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", hashedPassword)
                .get()
                .get()
                .getDocuments();

            return users.isEmpty() ? null : users.get(0).getId();
        } catch (InterruptedException | ExecutionException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String registerUser(String name, String email, String password, String role) {
        try {
            Firestore db = FirebaseService.getFirestore();
            String hashedPassword = hashPassword(password);

            if (userExists(email)) {
                System.err.println("User with email " + email + " already exists.");
                return null;
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("password", hashedPassword);
            userData.put("role", role.toLowerCase());

            String userId = db.collection("users")
                .add(userData)
                .get()
                .getId();

            if ("candidate".equals(role.toLowerCase())) {
                Map<String, Object> candidateData = new HashMap<>();
                candidateData.put("userId", userId);
                candidateData.put("email", email);
                candidateData.put("name", name);
                candidateData.put("cv_url", null);
                db.collection("candidates").document(userId).set(candidateData).get();
            }

            return userId;
        } catch (InterruptedException | ExecutionException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String registerRecruiter(String name, String email, String password, String companyName) {
        try {
            Firestore db = FirebaseService.getFirestore();
            String hashedPassword = hashPassword(password);

            if (userExists(email)) {
                System.err.println("User with email " + email + " already exists.");
                return null;
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("password", hashedPassword);
            userData.put("role", "recruiter");

            Map<String, Object> recruiterData = new HashMap<>();
            recruiterData.put("userId", "");
            recruiterData.put("email", email);
            recruiterData.put("name", name);
            recruiterData.put("companyName", companyName);

            String userId = db.collection("users").add(userData).get().getId();
            recruiterData.put("userId", userId);
            db.collection("recruiters").document(userId).set(recruiterData).get();

            System.out.println("Successfully created recruiter with ID: " + userId);
            return userId;
        } catch (InterruptedException | ExecutionException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean resetPassword(String email, String newPassword) {
        try {
            Firestore db = FirebaseService.getFirestore();
            String hashedPassword = hashPassword(newPassword);

            // Check if user exists
            List<QueryDocumentSnapshot> users = db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .get()
                .getDocuments();

            if (users.isEmpty()) {
                return false; // User not found
            }

            // Update password
            String userId = users.get(0).getId();
            WriteResult result = db.collection("users")
                .document(userId)
                .update("password", hashedPassword)
                .get();

            System.out.println("Password updated for user: " + email + " at " + result.getUpdateTime());
            return true;
        } catch (InterruptedException | ExecutionException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean userExists(String email) throws InterruptedException, ExecutionException {
        Firestore db = FirebaseService.getFirestore();
        List<QueryDocumentSnapshot> users = db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .get()
            .getDocuments();
        return !users.isEmpty();
    }

    public static void logout() {
        System.out.println("User logged out");
    }

    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();

        for (byte b : encodedHash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }
}