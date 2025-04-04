package com.springboot.MyTodoList.controller.utils;

import com.springboot.MyTodoList.model.ProjectModel;
import com.springboot.MyTodoList.model.SprintModel;
import com.springboot.MyTodoList.model.TaskModel;
import com.springboot.MyTodoList.model.UserModel;

public class TestUtils {
    public static ProjectModel createProjectModel() {
        ProjectModel project = new ProjectModel();
        project.setID(1); // Set a valid ID
        return project;
    }

    public static SprintModel createSprintModel() {
        SprintModel sprint = new SprintModel();
        sprint.setID(1); // Set a valid ID
        sprint.setProjectId(createProjectModel().getID()); // Associate with a valid ProjectModel ID
        sprint.setName("Sprint 1");
        sprint.setDescription("First sprint");
        return sprint;
    }

    public static TaskModel createTaskModel() {
        TaskModel task = new TaskModel();
        task.setID(1); // Set a valid ID
        task.setProjectId(createProjectModel().getID()); // Associate with a valid ProjectModel ID
        return task;
    }

    public static UserModel createUserModel() {
        UserModel user = new UserModel();
        user.setID(1); // Set a valid ID
        user.setProjectId(createProjectModel().getID()); // Associate with a valid ProjectModel ID
        return user;
    }
}