package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "PROJECTS")
@Schema(
    description = "Represents a project which is the top-level container for sprints, tasks, and team members. " +
                 "Projects help organize work and track overall progress across multiple sprints."
)
public class ProjectModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the project", example = "1")
    private int ID;
    
    @Column(name = "NAME", length = 255, nullable = false)
    @Schema(
        description = "Name of the project",
        example = "MyTodoList App",
        maxLength = 255,
        required = true
    )
    private String name;
    
    @Column(name = "START_TIME", nullable = false)
    @Schema(
        description = "Start date and time of the project",
        example = "2025-01-01T00:00:00Z",
        required = true
    )
    private OffsetDateTime startTime;
    
    @Column(name = "END_TIME", nullable = false)
    @Schema(
        description = "Planned end date and time of the project",
        example = "2025-12-31T23:59:59Z",
        required = true
    )
    private OffsetDateTime endTime;
    
    public ProjectModel() {
    }
    
    public ProjectModel(
            int ID,
            String name,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        this.ID = ID;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public int getID() {
        return ID;
    }
    
    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Project{" +
                "ID=" + ID +
                ", name='" + name + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
