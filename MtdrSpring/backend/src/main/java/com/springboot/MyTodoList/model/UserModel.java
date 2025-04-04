package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name = "USERS")
public class UserModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ID;

    @Column(name = "TELEGRAM_ID", nullable = true, length = 255)
    private Long telegramId;

    // @ManyToOne
    // @JoinColumn(name = "PROJECT_ID", nullable = false)
    // private ProjectModel project;

    @Column(name = "PROJECT_ID", nullable = false)
    private Integer projectId ;

    @Column(name = "FIRST_NAME", length = 255, nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", length = 255, nullable = false)
    private String lastName;

    @Column(name = "ROLE", length = 128, nullable = false)
    private String role;

    @Column(name = "TITLE", length = 255, nullable = true)
    private String title;

    public UserModel() {}

    public UserModel(Long telegramId, Integer projectId, String firstName, String lastName, String role, String title) {
        this.telegramId = telegramId;
        this.projectId = projectId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.title = title;
    }

    // Getters y Setters
    public int getID() { return ID; }
    public void setID(int id) { this.ID = id; }

    public Long getTelegramId() { return telegramId; }
    public void setTelegramId(Long telegramId) { this.telegramId = telegramId; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @Override
    public String toString() {
        return "UserModel{" +
                "ID=" + ID +
                ", telegramId=" + telegramId +
                ", project=" + projectId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role='" + role + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}