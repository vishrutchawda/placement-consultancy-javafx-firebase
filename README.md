# Placement Consultancy JavaFX System [![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/) [![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)](https://openjfx.io/) [![Firebase](https://img.shields.io/badge/Firebase-Firestore-red)](https://firebase.google.com/) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A feature-rich desktop application for placement consultancies, built with JavaFX for intuitive GUI and Firebase Firestore for scalable cloud storage. This project demonstrates full-stack Java development with real-time data syncing, secure authentication (SHA-256 hashing), and role-specific interfaces for candidates, recruiters, and admins, ideal for academic portfolios in GUI programming and cloud services.[file:1]

## Table of Contents
- [About the Project](#about-the-project)
- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Security Considerations](#security-considerations)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## About the Project
Designed to streamline job placements in educational settings, this app enables candidates to manage profiles (including Base64-encoded CV uploads) and respond to offers, while recruiters filter by marks/qualification and send proposals with estimated salaries. Using JavaFX for cross-platform desktop UI and Firebase for NoSQL backend, it avoids local databases for cloud-native scalability. Motivation: Automate manual processes with real-time updates, showcasing Java event-driven programming and async Firestore queries.[file:1]

Key aspects:
- Real-time offer notifications via Firestore listeners.
- Desktop-focused with scene management for navigation.
- Extensible model classes (Candidate, Offer, Recruiter) for data binding.

## Features
- **Authentication Module**: Login/signup with email/password (hashed via SHA-256), role detection (candidate/recruiter), and logout; forgot password dialog for resets.
- **Role-Based Dashboards**: Candidates view/accept/reject offers in TableView, edit profiles; Recruiters filter candidates (min marks, qualification), send hires via dialog, view offers status; No admin yet (extendable).
- **Profile & CV Management**: Edit name/email/marks/qualification; Upload PDF CVs converted to Base64 for Firestore storage, downloadable via temp files.
- **Offer System**: Create pending offers with salary/timestamp; Update status (PENDING/ACCEPTED/REJECTED) atomically; Fetch company/candidate names on-the-fly.
- **UI Enhancements**: Responsive JavaFX scenes with CSS styling, loading indicators, alerts for errors/success, icons for buttons.[file:1]

## Architecture
Monolithic desktop app with MVC pattern:
- **Frontend**: JavaFX FXML for views (login, dashboard, profile), controllers for event handling (e.g., DashboardController loads offers async).
- **Backend Logic**: Services (AuthService, FirebaseService) for Firestore ops using ApiFuture for non-blocking queries; Models (Offer with status, timestamp) for data objects.
- **Database**: Firebase Firestore collections (users, candidates, recruiters, offers) with document IDs as user IDs; Real-time via listeners on get().
- **Flow**: User input → Controller validation → Service async call (Firestore get/add/update) → UI update via Platform.runLater(); SceneManager handles navigation.[file:1]

Data flow: Event → Async Firebase → Bind to ObservableList<TableView> → Render.

## Tech Stack
- **Core**: Java 17+ (lambdas, records optional), JavaFX 21 (FXML, CSS for gradients/icons).
- **Database/Cloud**: Firebase Admin SDK (Firestore for NoSQL, GoogleCredentials for auth), Google Cloud APIs (ApiFuture for async).
- **Utilities**: Base64 for CV encoding, Executors for threading, SHA-256 for hashing (no external libs beyond JDK).
- **Build/Run**: Maven/Gradle optional (plain javac for simplicity); Resources include FXML/CSS/images.
- **Dependencies**: Firebase BOM (com.google.cloud:google-cloud-firestore), JavaFX modules (javafx-controls, -fxml, -graphics).[file:1]

## Prerequisites
- Java 17+ JDK (Oracle/OpenJDK).
- JavaFX 21 SDK (download from Gluon, set PATH).
- Firebase project (console.firebase.google.com): Enable Firestore, add service account key as "firebaseconfig.json" in resources/.
- IDE: IntelliJ/Eclipse for FXML preview; Git for version control.
- Basic Firestore rules (allow read/write for authenticated users).[file:1]

## Installation
1. **Clone or Download**:
  git clone https://github.com/vishrutchawda/placement-consultancy-javafx-firebase.git
  cd placement-consultancy-javafx-firebase

2. **Firebase Setup**:
- Create Firebase project, enable Firestore in test mode.
- Generate service account JSON: Project Settings > Service Accounts > Generate New Private Key > Save as "resources/firebaseconfig.json".
- Add Firestore indexes if needed (default suffices for queries).

3. **Build & Run**:
- Compile: `javac -d bin src/com/placement/*.java` (add --module-path for JavaFX).
- Or in IDE: Add JavaFX to module path, run Main.java.
- Command-line (with JavaFX): `java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp bin com.placement.Main`.
- First run initializes Firebase; Register users (candidates/recruiters), test offers.[file:1]

4. **Testing**:
- Signup as candidate/recruiter, upload CV, send/accept offer.
- Verify Firestore docs in console.

## Usage
- **Launch**: Run Main.java → Login screen.
- **Candidate Flow**: Signup/Login → Dashboard (view offers table) → Edit Profile (upload CV) → Accept/Reject via buttons.
- **Recruiter Flow**: Login → Dashboard (filter table, hire dialog with salary) → View offers status.
- **Navigation**: Buttons load scenes (e.g., handleLogout → login.fxml); Alerts for feedback.[file:1]
Example: Offer update uses Firestore .update(status, timestamp) with WriteResult.

## Security Considerations
- Passwords hashed with SHA-256 before Firestore storage; No salting (recommend BCrypt for prod).
- Async queries prevent UI blocking but use Executors for threading; Validate inputs (e.g., marks 0-100).
- CVs Base64-encoded (no direct file upload to Firestore); Temp files for viewing (deleteOnExit).
- Firebase rules: Default test mode insecure—set to auth.uid == resource.data.uid for prod.
- Vulnerabilities: No encryption for CV data; Add token auth via Firebase Auth for enhanced security.[file:1]

## Roadmap
- Integrate Firebase Auth for token-based login over custom hashing.
- Add admin dashboard for user management.
- Support image icons (current placeholders); Export reports as PDF.
- Modularize with Maven; Unit tests (JUnit) for services.
- Deploy as JAR (jpackage); Android port via Gluon Mobile.

## Contributing
Fork, create branch (`git checkout -b feature/EnhanceOffers`), commit (`git commit -m 'Add real-time listeners'`), PR. Follow Java conventions, async patterns; Test on Java 21+; No breaking Firebase config changes.[file:1]

1. Fork the Project
2. Create Feature Branch
3. Commit Changes
4. Push Branch
5. Open PR

## License
MIT License - See LICENSE.

## Contact
Vishrut Chawda - CS Student @ A.V Parekh Technical Institute, Rajkot  
Email: vishrutchawda@gmail.com  
LinkedIn: [www.linkedin.com/in/gp-avpti-comp-vishrut-chawda-s236020307230](https://www.linkedin.com/in/gp-avpti-comp-vishrut-chawda-s236020307230)  
Project: [https://github.com/vishrutchawda/placement-consultancy-javafx-firebase](https://github.com/vishrutchawda/placement-consultancy-javafx-firebase)[file:1]
