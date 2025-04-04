package com.springboot.MyTodoList.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.MyTodoList.dto.SprintDTO;
import com.springboot.MyTodoList.model.SprintModel;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.model.TaskModel;

import java.util.List;
import java.util.Optional;

@RestController
public class SprintApiController {
    @Autowired
    private SprintService sprintService;

    //@CrossOrigin
    @GetMapping("/api/{project}/sprints")
    public List<SprintModel> getAllSprints(@PathVariable("project") int project){
        return sprintService.findByProjectId(project);
    }

    //@CrossOrigin
    @PostMapping("/api/{project}/sprints")
    public ResponseEntity<Object> addSprint(@PathVariable("project") int project, @RequestBody SprintDTO sprint) throws Exception{
        sprintService.addSprintToProject(project, sprint);
        HttpHeaders responseHeaders = new HttpHeaders();

        return ResponseEntity.ok()
                .headers(responseHeaders).build();
    }
    
    //@CrossOrigin
    @GetMapping("/api/{project}/sprints/{id}")
    public ResponseEntity<SprintModel> getSprintById(@PathVariable("project") int project, @PathVariable("id") int id){
        Optional<SprintModel> sprint = sprintService.getSprintById(id);

        if(sprint.isPresent()){
            SprintModel s = sprint.get();

            if (s.getProjectId() == project) {
                return new ResponseEntity<>(sprint.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }   
        }else{
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    //@CrossOrigin
    @PatchMapping("/api/{project}/sprints/{id}")
    public ResponseEntity<Object> updateSprint(@PathVariable("project") int project, @RequestBody SprintDTO sprint, @PathVariable("id") int id){
        try{
            SprintModel sprint1 = sprintService.patchSprintFromProject(id, project,  sprint);
            System.out.println(sprint1.toString());
            return new ResponseEntity<>(sprint1,HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/api/{project}/sprints/{id}/tasks")
    public ResponseEntity<List<TaskModel>> getTasksForSprint(@PathVariable("project") int project, @PathVariable("id") int id) {
        try {
            List<TaskModel> tasks = sprintService.getTasksBySprintId(id, project);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
