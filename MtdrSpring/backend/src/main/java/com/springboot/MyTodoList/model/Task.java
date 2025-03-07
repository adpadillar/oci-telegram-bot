package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.OffsetDateTime;

/*
    representation of the TASKS table that exists in the database
 */
@Entity
@Table(name = "TASKS")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;
    
    @Column(name = "PROJECT_ID")
    private int projectId;
    
    @Column(name = "DESCRIPTION")
    private String description;
    
    @Column(name = "CREATED_AT")
    private OffsetDateTime createdAt;
    
    @Column(name = "STATUS")
    private String status;
    
    @Column(name = "CREATED_BY")
    private int createdBy;
    
    @Column(name = "ASSIGNED_TO")
    private int assignedTo;
    
    @Column(name = "ESTIMATE_HOURS")
    private Double estimateHours;
    
    @Column(name = "REAL_HOURS")
    private Double realHours;
    
    @Column(name = "SPRINT_ID")
    private int sprintId;
    
    @Column(name = "CATEGORY")
    private String category;
    
    public Task() {
    }
    
    public Task(int ID, int projectId, String description, OffsetDateTime createdAt, 
                String status, int createdBy, int assignedTo, Double estimateHours, 
                Double realHours, int sprintId, String category) {
        this.ID = ID;
        this.projectId = projectId;
        this.description = description;
        this.createdAt = createdAt;
        this.status = status;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
        this.estimateHours = estimateHours;
        this.realHours = realHours;
        this.sprintId = sprintId;
        this.category = category;
    }
    
    public int getID() {
        return ID;
    }
    
    public void setID(int ID) {
        this.ID = ID;
    }
    
    public int getProjectId() {
        return projectId;
    }
    
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
    
    public int getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(int assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public Double getEstimateHours() {
        return estimateHours;
    }
    
    public void setEstimateHours(Double estimateHours) {
        this.estimateHours = estimateHours;
    }
    
    public Double getRealHours() {
        return realHours;
    }
    
    public void setRealHours(Double realHours) {
        this.realHours = realHours;
    }
    
    public int getSprintId() {
        return sprintId;
    }
    
    public void setSprintId(int sprintId) {
        this.sprintId = sprintId;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    @Override
    public String toString() {
        return "ToDoItem{" +
                "ID=" + ID +
                ", projectId=" + projectId +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                ", createdBy=" + createdBy +
                ", assignedTo=" + assignedTo +
                ", estimateHours=" + estimateHours +
                ", realHours=" + realHours +
                ", sprintId=" + sprintId +
                ", category='" + category + '\'' +
                '}';
    }
}
