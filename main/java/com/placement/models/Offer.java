package com.placement.models;

import com.google.cloud.Timestamp;

public class Offer {
    private String id;
    private String candidateId;
    private String recruiterId;
    private String status;
    private Timestamp timestamp;
    private Double estimatedSalary; // New field for estimated salary

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }

    public String getRecruiterId() { return recruiterId; }
    public void setRecruiterId(String recruiterId) { this.recruiterId = recruiterId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public Double getEstimatedSalary() { return estimatedSalary; }
    public void setEstimatedSalary(Double estimatedSalary) { this.estimatedSalary = estimatedSalary; }
}