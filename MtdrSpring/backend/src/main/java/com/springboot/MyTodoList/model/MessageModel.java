package com.springboot.MyTodoList.model;

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
    private String description;
    
    @Column(name = "CONTENT")
    private String content;
    
    @Column(name = "USER_ID")
    private int userId;
    
    public MessageModel() {
    }
    
    public MessageModel(
            int ID,
            String messageType,
            String description,
            String content,
            int userId
    ) {
        this.ID = ID;
        this.messageType = messageType;
        this.description = description;
        this.content = content;
        this.userId = userId;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getContent() {
        return content;
    }   

    public void setContent(String content) {
        this.content = content;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    
    @Override
    public String toString() {
        return "Message{" +
                "ID=" + ID +
                ", messageType='" + messageType + '\'' +
                ", description='" + description + '\'' +
                ", content='" + content + '\'' +
                ", userId=" + userId +
                '}';
    }
}
