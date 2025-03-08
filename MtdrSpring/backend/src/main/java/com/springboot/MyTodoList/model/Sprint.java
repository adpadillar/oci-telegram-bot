package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.OffsetDateTime;

/*
    representation of the TASKS table that exists in the database
 */
@Entity
@Table(name = "SPRINTS")
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;
    
    @Column(name = "PROJECT_ID")
    private int projectId;
    
    @Column(name = "NAME")
    private String name;
    
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "STARTED_AT")
    private OffsetDateTime startedAt;
    
    @Column(name = "ENDS_AT")
    private OffsetDateTime endsAt;
    
    public Sprint() {
    }
    
    public Sprint(int ID, int projectId,  String name, String description, OffsetDateTime startedAt, OffsetDateTime endsAt) {
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
    
    public int getProjectId() {
        return projectId;
    }
    
    public void setProjectId(int projectId) {
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
    

    @Override
    public String toString() {
        return "Sprint{" +
                "ID=" + ID +
                ", projectId=" + projectId +
                ", name='" + name +
                ", description='" + description + '\'' +
                ", startedAt=" + startedAt + '\'' +
                ", endsAt=" + endsAt + '\'' +
                '}';
    }
}
