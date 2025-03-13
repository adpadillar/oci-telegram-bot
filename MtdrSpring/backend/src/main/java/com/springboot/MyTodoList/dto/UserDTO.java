package com.springboot.MyTodoList.dto;

public class UserDTO {
    private Long telegramId;
    private String firstName;
    private String lastName;
    private String role;
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