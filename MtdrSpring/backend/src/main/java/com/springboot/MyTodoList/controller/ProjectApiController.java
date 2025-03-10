package com.springboot.MyTodoList.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.MyTodoList.model.ProjectModel;
import com.springboot.MyTodoList.service.ProjectService;
import java.util.List;

@RestController
public class ProjectApiController {
    @Autowired
    private ProjectService projectService;
    //@CrossOrigin
    @GetMapping(value = "/api/projects")
    public List<ProjectModel> getAllProjects(){
        return projectService.findAll();
    }

    //@CrossOrigin
    @PostMapping(value = "/api/projects")
    public ResponseEntity addProject(@RequestBody ProjectModel project) throws Exception{
        ProjectModel pr = projectService.addProject(project);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location",""+pr.getID());
        responseHeaders.set("Access-Control-Expose-Headers","location");
        //URI location = URI.create(""+sp.getID())

        return ResponseEntity.ok()
                .headers(responseHeaders).build();
    }
    
    //@CrossOrigin
    @GetMapping(value = "/api/projects/{id}")
    public ResponseEntity<ProjectModel> getSprintById(@PathVariable int id){
        try{
            ProjectModel project = projectService.getProjectById(id);
            return new ResponseEntity<ProjectModel>(project, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
 

}
