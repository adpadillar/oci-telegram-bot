package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/*
    representation of the TASKS table that exists in the database
 */
@Entity
@Table(name = "TASKS")
@Schema(description = "Represents a work item that can be assigned to team members and tracked through various stages")
public class TaskModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the task", example = "1")
    private int ID;
    
    @Column(name = "PROJECT_ID", nullable = false)
    @Schema(description = "ID of the project this task belongs to", example = "1", required = true)
    private Integer projectId;
    
    @Column(name = "DESCRIPTION", length = 4000, nullable = true)
    @Schema(
        description = "Detailed description of what needs to be done",
        example = "Implement user authentication using JWT tokens",
        maxLength = 4000
    )
    private String description;
    
    @Column(name = "CREATED_AT", nullable = false)
    @Schema(
        description = "Timestamp when the task was created",
        example = "2025-01-01T10:00:00Z",
        required = true
    )
    private OffsetDateTime createdAt;
    
    @Column(name = "STATUS", length = 128, nullable = false)
    @Schema(
        description = "Current status of the task",
        example = "in-progress",
        allowableValues = {"created", "in-progress", "in-review", "testing", "done"},
        required = true
    )
    private String status;
    
    @Column(name = "CREATED_BY", nullable = false)
    @Schema(description = "ID of the user who created the task", example = "1", required = true)
    private Integer createdById;

    @Column(name = "ASSIGNED_TO", nullable = false)
    @Schema(description = "ID of the user assigned to complete the task", example = "2")
    private Integer assignedToId;
    
    @Column(name = "ESTIMATE_HOURS", nullable = true)
    @Schema(
        description = "Estimated hours to complete the task",
        example = "8.0",
        minimum = "0"
    )
    private Double estimateHours;
    
    @Column(name = "REAL_HOURS", nullable = true)
    @Schema(
        description = "Actual hours spent on the task",
        example = "6.5",
        minimum = "0"
    )
    private Double realHours;

    @Column(name = "SPRINT_ID", nullable = true)
    @Schema(description = "ID of the sprint this task is assigned to", example = "1")
    private Integer sprintId;
    
    @Column(name = "CATEGORY", length = 128, nullable = true)
    @Schema(
        description = "Category of the task",
        example = "feature",
        allowableValues = {"bug", "feature", "issue"}
    )
    private String category;
    
    public TaskModel() {
    }
    
    public TaskModel(int ID, Integer projectId, String description, OffsetDateTime createdAt, 
                String status, Integer createdById, Integer assignedToId, Double estimateHours, 
                Double realHours, Integer sprintId, String category) {
        this.ID = ID;
        this.projectId = projectId;
        this.description = description;
        this.createdAt = createdAt;
        this.status = status;
        this.createdById = createdById;
        this.assignedToId = assignedToId;
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
    
    public Integer getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Integer projectId) {
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
    
    public Integer getCreatedById() {
        return createdById;
    }
    
    public void setCreatedById(Integer createdById) {
        this.createdById = createdById;
    }
    
    public Integer getAssignedToId() {
        return assignedToId;
    }
    
    public void setAssignedTo(Integer assignedToId) {
        this.assignedToId = assignedToId;
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
    
    public Integer getSprintId() {
        return sprintId;
    }
    
    public void setSprintId(Integer sprintId) {
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
                ", project=" + (projectId != null ? projectId : null) +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                ", createdBy=" + (createdById != null ? createdById : null) +
                ", assignedTo=" + (assignedToId != null ? assignedToId : null) +
                ", estimateHours=" + estimateHours +
                ", realHours=" + realHours +
                ", sprint=" + (sprintId != null ? sprintId : null) +
                ", category='" + category + '\'' +
                '}';
    }
}
