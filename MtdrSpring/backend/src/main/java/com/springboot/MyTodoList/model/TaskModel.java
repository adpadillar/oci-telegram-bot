package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.OffsetDateTime;

/*
    representation of the TASKS table that exists in the database
 */
@Entity
@Table(name = "TASKS")
public class TaskModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;
    
    @ManyToOne
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private ProjectModel project;
    
    @Column(name = "DESCRIPTION", length = 4000, nullable = true)
    private String description;
    
    @Column(name = "CREATED_AT", nullable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "STATUS", length = 128, nullable = false)
    private String status;
    
    @ManyToOne
    @JoinColumn(name = "CREATED_BY", nullable = false)
    private UserModel createdBy;
    
    @ManyToOne
    @JoinColumn(name = "ASSIGNED_TO", nullable = true)
    private UserModel assignedTo;
    
    @Column(name = "ESTIMATE_HOURS", nullable = true)
    private Double estimateHours;
    
    @Column(name = "REAL_HOURS", nullable = true)
    private Double realHours;
    
    @ManyToOne
    @JoinColumn(name = "SPRINT_ID", nullable = true)
    private SprintModel sprint;
    
    @Column(name = "CATEGORY", length = 128, nullable = true)
    private String category;
    
    public TaskModel() {
    }
    
    public TaskModel(int ID, ProjectModel project, String description, OffsetDateTime createdAt, 
                String status, UserModel createdBy, UserModel assignedTo, Double estimateHours, 
                Double realHours, SprintModel sprint, String category) {
        this.ID = ID;
        this.project = project;
        this.description = description;
        this.createdAt = createdAt;
        this.status = status;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
        this.estimateHours = estimateHours;
        this.realHours = realHours;
        this.sprint = sprint;
        this.category = category;
    }
    
    public int getID() {
        return ID;
    }
    
    public void setID(int ID) {
        this.ID = ID;
    }
    
    public ProjectModel getProject() {
        return project;
    }
    
    public void setProject(ProjectModel project) {
        this.project = project;
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
    
    public UserModel getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(UserModel createdBy) {
        this.createdBy = createdBy;
    }
    
    public UserModel getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(UserModel assignedTo) {
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
    
    public SprintModel getSprint() {
        return sprint;
    }
    
    public void setSprint(SprintModel sprint) {
        this.sprint = sprint;
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
                ", project=" + (project != null ? project.getID() : null) +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                ", createdBy=" + (createdBy != null ? createdBy.getID() : null) +
                ", assignedTo=" + (assignedTo != null ? assignedTo.getID() : null) +
                ", estimateHours=" + estimateHours +
                ", realHours=" + realHours +
                ", sprint=" + (sprint != null ? sprint.getID() : null) +
                ", category='" + category + '\'' +
                '}';
    }
}
