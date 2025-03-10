package com.springboot.MyTodoList.model;

import java.time.OffsetDateTime;

import javax.persistence.*;

/*
    representation of the TASKS table that exists in the database
 */
@Entity
@Table(name = "MESSAGES")
public class MessageModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;
    
    @Column(name = "MESSAGE_TYPE")
    private String messageType;
    
    @Column(name = "ROLE")
    private String role;
    
    @Column(name = "CONTENT")
    private String content;
    
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "CREATED_AT")
    private OffsetDateTime createdAt;
    
    public MessageModel() {
    }
    
    public MessageModel(
            int ID,
            String messageType,
            String role,
            String content,
            Long userId,
            OffsetDateTime createdAt
    ) {
        this.ID = ID;
        this.messageType = messageType;
        this.role = role;
        this.content = content;
        this.userId = userId;
        this.createdAt = createdAt;
    }
    
    
    public int getID() {
        return ID;
    }
    
    public void setID(int ID) {
        this.ID = ID;
    }
    
    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getContent() {
        return content;
    }   

    public void setContent(String content) {
        this.content = content;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    
    @Override
    public String toString() {
        return "Message{" +
                "ID=" + ID +
                ", messageType='" + messageType + '\'' +
                ", role='" + role + '\'' +
                ", content='" + content + '\'' +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                '}';
    }
}
