package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "USERS")
public class UserModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;

    @Column(name = "TELEGRAM_ID", nullable = true, length = 255)
    private String telegramId;

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private ProjectModel project;

    @Column(name = "FIRST_NAME", length = 255, nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", length = 255, nullable = false)
    private String lastName;

    @Column(name = "ROLE", length = 128, nullable = false)
    private String role;

    public UserModel() {}

    public UserModel(String telegramId, ProjectModel project, String firstName, String lastName, String role) {
        this.telegramId = telegramId;
        this.project = project;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Getters y Setters
    public int getID() { return ID; }
    public void setID(int id) { this.ID = id; }

    public String getTelegramId() { return telegramId; }
    public void setTelegramId(String telegramId) { this.telegramId = telegramId; }

    public ProjectModel getProject() { return project; }
    public void setProject(ProjectModel project) { this.project = project; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}