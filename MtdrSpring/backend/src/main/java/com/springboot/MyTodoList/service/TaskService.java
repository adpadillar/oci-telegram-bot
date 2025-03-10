package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.TaskDTO;
import com.springboot.MyTodoList.model.ProjectModel;
import com.springboot.MyTodoList.model.SprintModel;
import com.springboot.MyTodoList.model.TaskModel;
import com.springboot.MyTodoList.model.UserModel;
import com.springboot.MyTodoList.repository.ProjectRepository;
import com.springboot.MyTodoList.repository.SprintRepository;
import com.springboot.MyTodoList.repository.TaskRepository;
import com.springboot.MyTodoList.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository toDoItemRepository;

    @Autowired 
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SprintRepository sprintRepository;
    
    public List<TaskModel> findAll(){
        List<TaskModel> todoItems = toDoItemRepository.findAll();
        return todoItems;
    }

    public List<TaskModel> findAllByProjectId(int projectId){
        List<TaskModel> todoItems = toDoItemRepository.findByProject_ID(projectId);
        return todoItems;
    }

    public TaskModel addTodoItemToProject(int projectId, TaskDTO taskDTO) throws RuntimeException {
        TaskModel task = new TaskModel();

        Optional<ProjectModel> maybeProject = projectRepository.findById(projectId);
        if (!maybeProject.isPresent()){
            throw new RuntimeException("Project not found");
        }

        ProjectModel project = maybeProject.get();

        task.setProject(project);
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus());
        task.setEstimateHours(taskDTO.getEstimateHours());
        task.setRealHours(taskDTO.getRealHours());

        Optional<UserModel> maybeCreatedBy = userRepository.findById(taskDTO.getCreatedBy());

        if (!maybeCreatedBy.isPresent()) {
            throw new RuntimeException("User not found");
        }

        UserModel createdBy = maybeCreatedBy.get();

        if (taskDTO.getAssignedTo() != null) {
            Optional<UserModel> maybeAssignedTo = userRepository.findById(taskDTO.getAssignedTo());
            if (!maybeAssignedTo.isPresent()) {
                throw new RuntimeException("User not found");
            }
            UserModel assignedTo = maybeAssignedTo.get();
            task.setAssignedTo(assignedTo);
        }

        if (taskDTO.getSprint() != null) {
            Optional<SprintModel> maybeSprint = sprintRepository.findById(taskDTO.getSprint());
            if (!maybeSprint.isPresent()){
                throw new RuntimeException("Sprint not found");
            }

            SprintModel sprint = maybeSprint.get();
            task.setSprint(sprint);
        }

        task.setCreatedBy(createdBy);
        task.setCategory(taskDTO.getCategory());
        task.setCreatedAt(OffsetDateTime.now());
        task.setStatus(taskDTO.getStatus());

        return toDoItemRepository.save(task);
    }

    public Optional<TaskModel> getItemById(int id){
        Optional<TaskModel> todoData = toDoItemRepository.findById(id);
        return todoData;
    }
    public TaskModel addToDoItem(TaskModel toDoItem){
        return toDoItemRepository.save(toDoItem);
    }

    public boolean deleteToDoItem(int id){
        try{
            toDoItemRepository.deleteById(id);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public TaskModel patchTaskOnProject(int id, int projectId, TaskDTO newValues) throws RuntimeException {
        Optional<TaskModel> optionalTask = toDoItemRepository.findById(id);
        if (optionalTask.isPresent()) {
            TaskModel task = optionalTask.get();
            if (task.getProject().getID() == projectId) {
                if (newValues.getDescription() != null) {
                    task.setDescription(newValues.getDescription());
                }
                
                if (newValues.getStatus() != null) {
                    task.setStatus(newValues.getStatus());
                }
                
                if (newValues.getAssignedTo() != null) {
                    Optional<UserModel> maybeAssignedTo = userRepository.findById(newValues.getAssignedTo());
                    if (!maybeAssignedTo.isPresent()) {
                        throw new RuntimeException("User not found");
                    }
                    UserModel assignedTo = maybeAssignedTo.get();
                    task.setAssignedTo(assignedTo);
                }
                
                if (newValues.getEstimateHours() != null) {
                    task.setEstimateHours(newValues.getEstimateHours());
                }
                
                if (newValues.getRealHours() != null) {
                    task.setRealHours(newValues.getRealHours());
                }
                
                if (newValues.getSprint() != null) {
                    Optional<SprintModel> maybeSprint = sprintRepository.findById(newValues.getSprint());
                    if (!maybeSprint.isPresent()){
                        throw new RuntimeException("Sprint not found");
                    }
                    SprintModel sprint = maybeSprint.get();
                    task.setSprint(sprint);
                }
                
                if (newValues.getCategory() != null) {
                    task.setCategory(newValues.getCategory());
                }
                
                return toDoItemRepository.save(task);
            }
        }
        return null;
    }

    public TaskModel updateToDoItem(int id, TaskModel td){
        Optional<TaskModel> toDoItemData = toDoItemRepository.findById(id);
        if(toDoItemData.isPresent()){
            TaskModel toDoItem = toDoItemData.get();
            toDoItem.setID(id);
            toDoItem.setCreatedAt(td.getCreatedAt());
            toDoItem.setDescription(td.getDescription());
            toDoItem.setStatus(td.getStatus());
            return toDoItemRepository.save(toDoItem);
        }else{
            return null;
        }
    }

}
