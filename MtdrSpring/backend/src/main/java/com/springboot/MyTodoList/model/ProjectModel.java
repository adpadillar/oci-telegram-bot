package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.OffsetDateTime;

/*
    representation of the PROJECTS table that exists in the database
 */
@Entity
@Table(name = "PROJECTS")
public class ProjectModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;
    
    @Column(name = "NAME", length = 255, nullable = false)
    private String name;
    
    @Column(name = "START_TIME", nullable = false)
    private OffsetDateTime startTime;
    
    @Column(name = "END_TIME", nullable = false)
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
