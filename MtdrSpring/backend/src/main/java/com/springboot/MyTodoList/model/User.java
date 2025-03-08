package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "USERS")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "TELEGRAM_ID", unique = true, nullable = false)
    private int telegramId;


    //Tentativo por que todavia no existe el project model LOL
    @ManyToOne
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Project project;

    @Column(name = "FIRST_NAME", length = 255, nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", length = 255, nullable = false)
    private String lastName;

    @Column(name = "ROLE", length = 128, nullable = false)
    private String role;

    public User() {}

    public User(int telegramId, Project project, String firstName, String lastName, String role) {
        this.telegramId = telegramId;
        this.project = project;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Getters y Setters
    public int getId() { return id; }

    public int getTelegramId() { return telegramId; }
    public void setTelegramId(int telegramId) { this.telegramId = telegramId; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
