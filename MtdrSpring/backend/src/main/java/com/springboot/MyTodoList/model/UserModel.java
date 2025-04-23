package com.springboot.MyTodoList.model;

import javax.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "USERS")
@Schema(
    description = "Represents a team member who can create, be assigned to, and manage tasks"
)
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the user", example = "1")
    private int ID;

    @Column(name = "TELEGRAM_ID", nullable = true, length = 255)
    @Schema(
        description = "Optional Telegram ID for notifications",
        example = "12345678"
    )
    private Long telegramId;

    @Column(name = "PROJECT_ID", nullable = false)
    @Schema(description = "ID of the project this user belongs to", example = "1", required = true)
    private Integer projectId;

    @Column(name = "FIRST_NAME", length = 255, nullable = false)
    @Schema(
        description = "User's first name",
        example = "John",
        maxLength = 255,
        required = true
    )
    private String firstName;

    @Column(name = "LAST_NAME", length = 255, nullable = false)
    @Schema(
        description = "User's last name",
        example = "Doe",
        maxLength = 255,
        required = true
    )
    private String lastName;

    @Column(name = "ROLE", length = 128, nullable = false)
    @Schema(
        description = "User's role in the team",
        example = "developer",
        allowableValues = {"developer", "manager", "tester", "designer"},
        maxLength = 128,
        required = true
    )
    private String role;

    @Column(name = "TITLE", length = 255, nullable = true)
    @Schema(
        description = "User's job title",
        example = "Senior Software Engineer",
        maxLength = 255
    )
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