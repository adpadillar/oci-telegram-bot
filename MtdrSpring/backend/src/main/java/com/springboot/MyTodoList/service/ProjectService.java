package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.ProjectModel;
import com.springboot.MyTodoList.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    public List<ProjectModel> findAll(){
        List<ProjectModel> projects = projectRepository.findAll();
        return projects;
    }
    public ResponseEntity<ProjectModel> getProjectById(int id){
        Optional<ProjectModel> projectData = projectRepository.findById(id);
        if (projectData.isPresent()){
            return new ResponseEntity<>(projectData.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    public ProjectModel addProject(ProjectModel project){
        return projectRepository.save(project);
    }

    public boolean deleteProject(int id){
        try{
            projectRepository.deleteById(id);
            return true;
        }catch(Exception e){
            return false;
        }
    }
}

