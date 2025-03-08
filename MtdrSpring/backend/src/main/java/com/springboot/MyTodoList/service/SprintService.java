package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.SprintDTO;
import com.springboot.MyTodoList.model.Project;
import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.repository.ProjectRepository;
import com.springboot.MyTodoList.repository.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SprintService {

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public List<Sprint> findByProjectId(int projectId){
        List<Sprint> sprints = sprintRepository.findByProject_ID(projectId);
        return sprints;
    }

    public List<Sprint> findAll(){
        List<Sprint> sprints = sprintRepository.findAll();
        return sprints;
    }
    public Optional<Sprint> getSprintById(int id){
        Optional<Sprint> todoData = sprintRepository.findById(id);
        
        return todoData;
    }
    public Sprint addSprint(Sprint sprint){
        return sprintRepository.save(sprint);
    }

    public Sprint addSprintToProject(int projectId, SprintDTO sprint){
        Optional<Project> maybeProject = projectRepository.findById(projectId);

        if (maybeProject.isPresent()) {
            Project project = maybeProject.get();
            Sprint newSprint = new Sprint();
            newSprint.setProject(project);
            newSprint.setName(sprint.getName());
            newSprint.setDescription(sprint.getDescription());
            newSprint.setStartedAt(sprint.getStartedAt());
            newSprint.setEndsAt(sprint.getEndsAt());
            return sprintRepository.save(newSprint);
        } else {
            throw new RuntimeException("Project not found");
        }
    }

    public boolean deletesprint(int id){
        try{
            sprintRepository.deleteById(id);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public Sprint patchSprintFromProject(int id, int projectid, SprintDTO newValues) throws RuntimeException {
        Optional<Sprint> maybeSprint = sprintRepository.findById(id);
        if (maybeSprint.isPresent()) {
            Sprint sprint = maybeSprint.get();
            if (sprint.getProject().getID() == projectid) {
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
    // public Sprint updateSprint(int id, Sprint spr){
    //     Optional<Sprint> sprintData = sprintRepository.findById(id);
    //     if(sprintData.isPresent()){
    //         Sprint sprint = sprintData.get();
    //         sprint.setID(id);
    //         sprint.setProjectId(spr.getProjectId());
    //         sprint.setName(spr.getName());
    //         sprint.setDescription(spr.getDescription());
    //         sprint.setStartedAt(spr.getStartedAt());
    //         sprint.setEndsAt(spr.getEndsAt());
    //         return sprintRepository.save(sprint);
    //     }else{
    //         return null;
    //     }
    // }

    // public Sprint updateSprint(int id, Sprint spr){
        // Optional<Sprint> sprintData = sprintRepository.findById(id);
        // if(sprintData.isPresent()){
        //     Sprint sprint = sprintData.get();
        //     if (spr.getProjectId() != 0) {
        //         sprint.setProjectId(spr.getProjectId());
        //     }
        //     if (spr.getName() != null) {
        //         sprint.setName(spr.getName());
        //     }
        //     if (spr.getDescription() != null) {
        //         sprint.setDescription(spr.getDescription());
        //     }
        //     if (spr.getStartedAt() != null) {
        //         sprint.setStartedAt(spr.getStartedAt());
        //     }
        //     if (spr.getEndsAt() != null) {
        //         sprint.setEndsAt(spr.getEndsAt());
        //     }
        //     return sprintRepository.save(sprint);
        // }else{
        //     return null;
        // }
    // }
}

