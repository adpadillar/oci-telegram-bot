package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.OffsetDateTime;

/*
    representation of the TASKS table that exists in the database
 */
@Entity
@Table(name = "MESSAGES")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;
    
    @Column(name = "MANAGER_ID")
    private int managerId;
    
    @Column(name = "NAME")
    private String name;
    
    @Column(name = "START_TIME")
    private OffsetDateTime startTime;
    
    @Column(name = "END_TIME")
    private OffsetDateTime endTime;
    
    public Project() {
    }
    
    public Project(
            int ID,
            int managerId,
            String name,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        this.ID = ID;
        this.managerId = managerId;
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
    
    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
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
                ", managerId=" + managerId +
                ", name='" + name + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
