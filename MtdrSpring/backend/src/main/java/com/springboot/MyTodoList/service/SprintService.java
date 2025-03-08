package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.repository.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SprintService {

    @Autowired
    private SprintRepository sprintRepository;
    public List<Sprint> findAll(){
        List<Sprint> sprints = sprintRepository.findAll();
        return sprints;
    }
    public ResponseEntity<Sprint> getSprintById(int id){
        Optional<Sprint> todoData = sprintRepository.findById(id);
        if (todoData.isPresent()){
            return new ResponseEntity<>(todoData.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    public Sprint addSprint(Sprint sprint){
        return sprintRepository.save(sprint);
    }

    public boolean deletesprint(int id){
        try{
            sprintRepository.deleteById(id);
            return true;
        }catch(Exception e){
            return false;
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

    public Sprint updateSprint(int id, Sprint spr){
        Optional<Sprint> sprintData = sprintRepository.findById(id);
        if(sprintData.isPresent()){
            Sprint sprint = sprintData.get();
            if (spr.getProjectId() != 0) {
                sprint.setProjectId(spr.getProjectId());
            }
            if (spr.getName() != null) {
                sprint.setName(spr.getName());
            }
            if (spr.getDescription() != null) {
                sprint.setDescription(spr.getDescription());
            }
            if (spr.getStartedAt() != null) {
                sprint.setStartedAt(spr.getStartedAt());
            }
            if (spr.getEndsAt() != null) {
                sprint.setEndsAt(spr.getEndsAt());
            }
            return sprintRepository.save(sprint);
        }else{
            return null;
        }
    }
}

