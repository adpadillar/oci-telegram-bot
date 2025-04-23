package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/*
    representation of the TASKS table that exists in the database
 */
@Entity
@Table(name = "SPRINTS")
@Schema(
    description = "Represents a time-boxed iteration of work containing multiple tasks. " +
                 "Typically spans 1-4 weeks and helps track team progress and velocity."
)
public class SprintModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the sprint", example = "1")
    private int ID;
    
    @Column(name = "PROJECT_ID", nullable = false)
    @Schema(description = "ID of the project this sprint belongs to", example = "1", required = true)
    private Integer projectId;

    @Column(name = "NAME", length = 255, nullable = false)
    @Schema(
        description = "Name of the sprint. Often includes a number or theme",
        example = "Sprint 1 - Authentication Implementation",
        maxLength = 255,
        required = true
    )
    private String name;
    
    @Column(name = "DESCRIPTION", length = 4000, nullable = true)
    @Schema(
        description = "Detailed description of the sprint's goals and objectives",
        example = "Implement core authentication features including login, registration, and password reset",
        maxLength = 4000
    )
    private String description;

    @Column(name = "STARTED_AT", nullable = false)
    @Schema(
        description = "Start date and time of the sprint",
        example = "2025-01-01T00:00:00Z",
        required = true
    )
    private OffsetDateTime startedAt;
    
    @Column(name = "ENDS_AT", nullable = false)
    @Schema(
        description = "End date and time of the sprint",
        example = "2025-01-14T23:59:59Z",
        required = true
    )
    private OffsetDateTime endsAt;

    // @OneToMany(mappedBy = "sprint", fetch = FetchType.LAZY)
    // private List<TaskModel> tasks;
    
    public SprintModel() {
    }
    
    public SprintModel(int ID, Integer projectId,  String name, String description, OffsetDateTime startedAt, OffsetDateTime endsAt) {
        this.ID = ID;
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.startedAt = startedAt;
        this.endsAt = endsAt;
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
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public OffsetDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getEndsAt() {
        return endsAt;
    }
    
    public void setEndsAt(OffsetDateTime endsAt) {
        this.endsAt = endsAt;
    }

    // public List<TaskModel> getTasks() {
    //     return tasks;
    // }

    // public void setTasks(List<TaskModel> tasks) {
    //     this.tasks = tasks;
    // }
    
    @Override
    public String toString() {
        return "Sprint{" +
                "ID=" + ID +
                ", project=" + projectId.toString() +
                ", name='" + name +
                ", description='" + description + '\'' +
                ", startedAt=" + startedAt + '\'' +
                ", endsAt=" + endsAt + '\'' +
                '}';
    }
}
