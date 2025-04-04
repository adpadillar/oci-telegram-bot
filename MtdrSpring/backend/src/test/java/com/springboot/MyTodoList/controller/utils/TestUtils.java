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
        sprint.setProject(createProjectModel()); // Associate with a valid ProjectModel
        sprint.setName("Sprint 1");
        sprint.setDescription("First sprint");
        return sprint;
    }

    public static TaskModel createTaskModel() {
        TaskModel task = new TaskModel();
        task.setID(1); // Set a valid ID
        task.setProject(createProjectModel()); // Associate with a valid ProjectModel
        return task;
    }

    public static UserModel createUserModel() {
        UserModel user = new UserModel();
        user.setID(1); // Set a valid ID
        user.setProject(createProjectModel()); // Associate with a valid ProjectModel
        return user;
    }
}