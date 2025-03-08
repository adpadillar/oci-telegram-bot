package com.springboot.MyTodoList.controller;
import com.springboot.MyTodoList.dto.TaskDTO;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class TaskApiController {
    @Autowired
    private TaskService taskService;
    //@CrossOrigin
    @GetMapping(value = "/api/{project}/tasks")
    public List<Task> getAllToDoItems(@PathVariable("project") int project){
        return taskService.findAllByProjectId(project);
    }

    //@CrossOrigin
    @PostMapping(value = "/api/{project}/tasks")
    public ResponseEntity<Object> addToDoItem(@RequestBody TaskDTO todoItem, @PathVariable("project") int project) throws Exception {
        Task task = taskService.addTodoItemToProject(project, todoItem);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location",""+task.getID());
        responseHeaders.set("Access-Control-Expose-Headers","location");
        //URI location = URI.create(""+td.getID())

        return ResponseEntity.ok()
                .headers(responseHeaders).build();
    }

    //@CrossOrigin
    @GetMapping(value = "/api/{project}/tasks/{id}")
    public ResponseEntity<Task> getToDoItemById(@PathVariable("project") int project, @PathVariable("id") int id){
        Optional<Task> maybeTask = taskService.getItemById(id);

        if(maybeTask.isPresent()){
            Task task = maybeTask.get();

            if (task.getProject().getID() != project){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(task, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    //@CrossOrigin
    @PatchMapping(value = "/api/{project}/tasks/{id}")
    public ResponseEntity<Object> updateToDoItem(@RequestBody TaskDTO toDoItem, @PathVariable("project") int project, @PathVariable("id") int id){
       Task task = taskService.patchTaskOnProject(id, project, toDoItem);
       HttpHeaders responseHeaders = new HttpHeaders();
       responseHeaders.set("location",""+task.getID());
       responseHeaders.set("Access-Control-Expose-Headers","location");
       return ResponseEntity.ok()
               .headers(responseHeaders).build();
    }
    //@CrossOrigin
    @DeleteMapping(value = "/api/{project}/tasks/{id}")
    public ResponseEntity<Boolean> deleteToDoItem(@PathVariable("project") int project, @PathVariable("id") int id){
        Task task = taskService.getItemById(id).orElse(null);
        if(task == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (task.getProject().getID() != project){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        boolean deleted = taskService.deleteToDoItem(id);
        if(deleted){
            return new ResponseEntity<>(true, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
