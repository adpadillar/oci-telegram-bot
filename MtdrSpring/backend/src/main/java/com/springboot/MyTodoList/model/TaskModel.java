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
    private Integer ID;
    
    @Column(name = "PROJECT_ID", nullable = false)
    private Integer projectId ;

    // @ManyToOne
    // @JoinColumn(name = "PROJECT_ID", nullable = false)
    // private ProjectModel project;
    
    @Column(name = "DESCRIPTION", length = 4000, nullable = true)
    private String description;
    
    @Column(name = "CREATED_AT", nullable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "STATUS", length = 128, nullable = false)
    private String status;
    
    @Column(name = "CREATED_BY", nullable = false)
    private Integer createdById ;

    @Column(name = "ASSIGNED_TO", nullable = false)
    private Integer assignedToId ;

    // @ManyToOne
    // @JoinColumn(name = "CREATED_BY", nullable = false)
    // private UserModel createdBy;
    
    // @ManyToOne
    // @JoinColumn(name = "ASSIGNED_TO", nullable = true)
    // private UserModel assignedTo;
    
    @Column(name = "ESTIMATE_HOURS", nullable = true)
    private Double estimateHours;
    
    @Column(name = "REAL_HOURS", nullable = true)
    private Double realHours;
    
    // @ManyToOne
    // @JoinColumn(name = "SPRINT_ID", nullable = true)
    // private SprintModel sprint;

    @Column(name = "SPRINT_ID", nullable = false)
    private Integer sprintId ;
    
    @Column(name = "CATEGORY", length = 128, nullable = true)
    private String category;
    
    public TaskModel() {
    }
    
    public TaskModel(Integer ID, Integer projectId, String description, OffsetDateTime createdAt, 
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
    
    public Integer getID() {
        return ID;
    }
    
    public void setID(Integer ID) {
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
