package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.service.MessageService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ToDoItemBotControllerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private UserService userService;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private ToDoItemBotController toDoItemBotController;

    public ToDoItemBotControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleAddTask() {
        // Call the method with a dummy chat id
        //toDoItemBotController.handleAddTask(12345L);
        // Verify that a message is saved via messageService (adjust as per your controller logic)
        //verify(messageService, times(1)).saveMessage(any());
    }
}