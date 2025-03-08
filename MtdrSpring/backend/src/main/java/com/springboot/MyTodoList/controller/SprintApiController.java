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

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.service.SprintService;

import java.net.URI;
import java.util.List;

@RestController
public class SprintApiController {
    @Autowired
    private SprintService sprintService;
    //@CrossOrigin
    @GetMapping(value = "/sprints")
    public List<Sprint> getAllSprints(){
        return sprintService.findAll();
    }

    //@CrossOrigin
    @PostMapping(value = "/sprints")
    public ResponseEntity addSprint(@RequestBody Sprint sprint) throws Exception{
        Sprint sp = sprintService.addSprint(sprint);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location",""+sp.getID());
        responseHeaders.set("Access-Control-Expose-Headers","location");
        //URI location = URI.create(""+sp.getID())

        return ResponseEntity.ok()
                .headers(responseHeaders).build();
    }
    
    //@CrossOrigin
    @GetMapping(value = "/sprints/{id}")
    public ResponseEntity<Sprint> getSprintById(@PathVariable int id){
        try{
            ResponseEntity<Sprint> responseEntity = sprintService.getSprintById(id);
            return new ResponseEntity<Sprint>(responseEntity.getBody(), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    //@CrossOrigin
    @PatchMapping(value = "sprints/{id}")
    public ResponseEntity updateSprint(@RequestBody Sprint sprint, @PathVariable int id){
        try{
            Sprint sprint1 = sprintService.updateSprint(id, sprint);
            System.out.println(sprint1.toString());
            return new ResponseEntity<>(sprint1,HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }    

}
