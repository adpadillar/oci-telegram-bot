package com.springboot.MyTodoList.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Data Transfer Object for creating and updating user information"
)
public class UserDTO {
    @Schema(
        description = "Optional Telegram ID for notifications",
        example = "12345678"
    )
    private Long telegramId;

    @Schema(
        description = "User's first name",
        example = "John",
        required = true
    )
    private String firstName;

    @Schema(
        description = "User's last name",
        example = "Doe",
        required = true
    )
    private String lastName;

    @Schema(
        description = "User's role in the team",
        example = "developer",
        allowableValues = {"developer", "manager", "tester", "designer"},
        required = true
    )
    private String role;

    @Schema(
        description = "User's job title",
        example = "Senior Software Engineer"
    )
    private String title;

    // Default constructor
    public UserDTO() {}
    
    // Constructor with fields
    public UserDTO(Long telegramId, String firstName, String lastName, String role, String title) {
        this.telegramId = telegramId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.title = title;
    }
    
    // Getters and setters
    public Long getTelegramId() { return telegramId; }
    public void setTelegramId(Long telegramId) { this.telegramId = telegramId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}