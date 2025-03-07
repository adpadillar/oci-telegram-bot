package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository toDoItemRepository;
    public List<Task> findAll(){
        List<Task> todoItems = toDoItemRepository.findAll();
        return todoItems;
    }
    public ResponseEntity<Task> getItemById(int id){
        Optional<Task> todoData = toDoItemRepository.findById(id);
        if (todoData.isPresent()){
            return new ResponseEntity<>(todoData.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    public Task addToDoItem(Task toDoItem){
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
    public Task updateToDoItem(int id, Task td){
        Optional<Task> toDoItemData = toDoItemRepository.findById(id);
        if(toDoItemData.isPresent()){
            Task toDoItem = toDoItemData.get();
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
