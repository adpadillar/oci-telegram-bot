package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.controller.utils.TestUtils;
import com.springboot.MyTodoList.model.TaskModel;
import com.springboot.MyTodoList.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskApiControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskApiV1Controller taskApiController;

    public TaskApiControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetToDoItemById() {
        TaskModel mockTask = TestUtils.createTaskModel();
        when(taskService.getItemById(1)).thenReturn(Optional.of(mockTask));

        ResponseEntity<TaskModel> response = taskApiController.getToDoItemById(1, 1);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        // Assert that the task belongs to the expected project
        assertEquals(1, response.getBody().getProjectId());
        verify(taskService, times(1)).getItemById(1);
    }
    
    @Test
    void testGetToDoItemByIdNotFoundWhenProjectMismatch() {
        TaskModel mockTask = TestUtils.createTaskModel();
        // Simulate a task whose project id is 1 but queried with project 2.
        when(taskService.getItemById(1)).thenReturn(Optional.of(mockTask));
        
        ResponseEntity<TaskModel> response = taskApiController.getToDoItemById(2, 1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    void testGetToDoItemByIdNotFoundWhenAbsent() {
        when(taskService.getItemById(999)).thenReturn(Optional.empty());
        ResponseEntity<TaskModel> response = taskApiController.getToDoItemById(1, 999);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}