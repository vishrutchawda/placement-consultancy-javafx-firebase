package com.placement.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Candidate {
    private String id;
    private String name;
    private String email;
    private double marks;
    private String qualification;
    private String cvUrl; // Store URL instead of bytes

    private final StringProperty nameProperty = new SimpleStringProperty();
    private final DoubleProperty marksProperty = new SimpleDoubleProperty();

    public Candidate() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        nameProperty.set(name);
    }

    public StringProperty nameProperty() { return nameProperty; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getMarks() { return marks; }
    public void setMarks(double marks) {
        this.marks = marks;
        marksProperty.set(marks);
    }

    public DoubleProperty marksProperty() { return marksProperty; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getCvUrl() { return cvUrl; }
    public void setCvUrl(String cvUrl) { this.cvUrl = cvUrl; }
}