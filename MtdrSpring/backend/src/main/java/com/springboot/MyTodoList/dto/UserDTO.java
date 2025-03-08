package com.springboot.MyTodoList.dto;

public class UserDTO {
    private String telegramId;
    private String firstName;
    private String lastName;
    private String role;
    
    // Default constructor
    public UserDTO() {}
    
    // Constructor with fields
    public UserDTO(String telegramId, String firstName, String lastName, String role) {
        this.telegramId = telegramId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
    
    // Getters and setters
    public String getTelegramId() { return telegramId; }
    public void setTelegramId(String telegramId) { this.telegramId = telegramId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}