package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.OffsetDateTime;

/*
    representation of the TASKS table that exists in the database
 */
@Entity
@Table(name = "SPRINTS")
public class SprintModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;
    
    // // @ManyToOne
    // @JoinColumn(name = "PROJECT_ID", nullable = false)
    // private ProjectModel project;
    @Column(name = "PROJECT_ID", nullable = false)
    private Integer projectId ;

    @Column(name = "NAME", length = 255, nullable = false)
    private String name;
    
    @Column(name = "DESCRIPTION", length = 4000, nullable = true)
    private String description;

    @Column(name = "STARTED_AT", nullable = false)
    private OffsetDateTime startedAt;
    
    @Column(name = "ENDS_AT", nullable = false)
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
