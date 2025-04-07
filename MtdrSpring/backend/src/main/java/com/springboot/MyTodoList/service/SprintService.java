package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.SprintModel;
import com.springboot.MyTodoList.model.TaskModel;
import com.springboot.MyTodoList.dto.SprintDTO;

import java.util.List;
import java.util.Optional;

public interface SprintService {
    List<SprintModel> findByProjectId(int projectId);
    List<SprintModel> findAll();
    Optional<SprintModel> getSprintById(int id);
    SprintModel addSprint(SprintModel sprint);
    SprintModel addSprintToProject(int projectId, SprintDTO sprint);
    boolean deletesprint(int id);
    SprintModel patchSprintFromProject(int id, int projectid, SprintDTO newValues);
    List<TaskModel> getTasksBySprintId(int sprintId, int projectId);
    boolean deleteSprintByName(Integer projectId, String sprintName);
}

