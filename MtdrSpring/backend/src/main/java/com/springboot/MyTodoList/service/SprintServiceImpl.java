package com.springboot.MyTodoList.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.MyTodoList.dto.SprintDTO;
import com.springboot.MyTodoList.model.SprintModel;
import com.springboot.MyTodoList.model.TaskModel;
import com.springboot.MyTodoList.repository.SprintRepository;
import com.springboot.MyTodoList.repository.TaskRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SprintServiceImpl implements SprintService {

    private static final Logger logger = LoggerFactory.getLogger(SprintServiceImpl.class);

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public List<SprintModel> findByProjectId(int projectId) {
        return sprintRepository.findByProjectId(projectId);
    }

    @Override
    public List<SprintModel> findAll() {
        return sprintRepository.findAll();
    }

    @Override
    public Optional<SprintModel> getSprintById(int id) {
        return sprintRepository.findById(id);
    }

    @Override
    public SprintModel addSprint(SprintModel sprint) {
        return sprintRepository.save(sprint);
    }

    @Override
    public SprintModel addSprintToProject(int projectId, SprintDTO sprint) {
        SprintModel newSprint = new SprintModel();
        newSprint.setProjectId(projectId);
        newSprint.setName(sprint.getName());
        newSprint.setDescription(sprint.getDescription());
        newSprint.setStartedAt(sprint.getStartedAt());
        newSprint.setEndsAt(sprint.getEndsAt());
        return sprintRepository.save(newSprint);
    }

    @Override
    public boolean deletesprint(int id) {
        try {
            sprintRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting sprint", e);
            return false;
        }
    }

    @Override
    public SprintModel patchSprintFromProject(int id, int projectid, SprintDTO newValues) {
        Optional<SprintModel> maybeSprint = sprintRepository.findById(id);
        if (maybeSprint.isPresent()) {
            SprintModel sprint = maybeSprint.get();
            if (sprint.getProjectId() == projectid) {
                if (newValues.getName() != null) {
                    sprint.setName(newValues.getName());
                }
                if (newValues.getDescription() != null) {
                    sprint.setDescription(newValues.getDescription());
                }
                if (newValues.getStartedAt() != null) {
                    sprint.setStartedAt(newValues.getStartedAt());
                }
                if (newValues.getEndsAt() != null) {
                    sprint.setEndsAt(newValues.getEndsAt());
                }
                return sprintRepository.save(sprint);
            } else {
                throw new RuntimeException("Sprint not found");
            }
        } else {
            throw new RuntimeException("Sprint not found");
        }
    }

    @Override
    public List<TaskModel> getTasksBySprintId(int sprintId, int projectId) {
        Optional<SprintModel> maybeSprint = sprintRepository.findById(sprintId);
        if (maybeSprint.isPresent()) {
            SprintModel sprint = maybeSprint.get();
            if (sprint.getProjectId() != projectId) {
                throw new RuntimeException("Sprint does not belong to project");
            }
            return taskRepository.findBySprintId(sprintId);
        } else {
            throw new RuntimeException("Sprint not found");
        }
    }

    @Override
    public boolean deleteSprintByName(Integer projectId, String sprintName) {
        try {
            SprintModel sprint = sprintRepository.findByProjectIdAndName(projectId, sprintName);
            if (sprint != null) {
                sprintRepository.delete(sprint);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting sprint by name", e);
            return false;
        }
    }
} 