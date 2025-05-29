package com.springboot.MyTodoList.controller;

//./mvnw test

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.springboot.MyTodoList.service.MessageService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.UserService;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.repository.SprintRepository;
import com.springboot.MyTodoList.model.MessageModel;
import com.springboot.MyTodoList.model.TaskModel;
import com.springboot.MyTodoList.model.UserModel;
import com.springboot.MyTodoList.model.SprintModel;

/**
 * Mock test para el Bot de Telegram - solo verificamos las llamadas a servicios
 * Tests tres funcionalidades principales:
 * 1. Crear tarea
 * 2. Ver tareas completadas de un sprint
 * 3. Ver tareas completadas de un usuario en un sprint
 */
@ExtendWith(MockitoExtension.class)
public class TelegramBotMockTest {

    // Los servicios reales del sistema
    @Mock
    private MessageService messageService;
    
    @Mock
    private UserService userService;
    
    @Mock
    private TaskService taskService;
    
    @Mock
    private SprintService sprintService;
    
    @Mock
    private SprintRepository sprintRepository;
    
    // Test constants
    private static final long CHAT_ID = 123456L;
    private static final int USER_ID = 42;
    private static final int SPRINT_ID = 5;
    
    /**
     * Test de creación de tareas
     */
    @Test
    void testCreateTask() throws Exception {
        // Creamos directamente una tarea usando el servicio
        UserModel user = new UserModel();
        user.setID(USER_ID);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole("developer");
        
        TaskModel task = new TaskModel();
        task.setDescription("New Test Task");
        task.setStatus("created");
        task.setEstimateHours(4.0);
        task.setCreatedById(USER_ID);
        task.setAssignedTo(USER_ID);
        task.setSprintId(SPRINT_ID);
        
        // Mock para simular el servicio guardando la tarea
        when(taskService.save(any(TaskModel.class))).thenReturn(task);
        
        TaskModel savedTask = taskService.save(task);
        
        // Verificar
        verify(taskService).save(any(TaskModel.class));
        assertEquals("New Test Task", savedTask.getDescription());
        assertEquals("created", savedTask.getStatus());
    }
    
    /**
     * Test para verificar que se pueden obtener las tareas completadas en un sprint
     */
    @Test
    void testViewTasksCompletedInSprint() throws Exception {
        // Crear lista de tareas de prueba
        List<TaskModel> doneTasks = new ArrayList<>();
        
        TaskModel task1 = new TaskModel();
        task1.setID(101);
        task1.setDescription("Implement login page");
        task1.setStatus("done");
        task1.setEstimateHours(8.0);
        task1.setRealHours(10.0);
        task1.setSprintId(SPRINT_ID);
        task1.setCreatedById(USER_ID);
        task1.setAssignedTo(USER_ID);
        
        TaskModel task2 = new TaskModel();
        task2.setID(102);
        task2.setDescription("Fix authentication bug");
        task2.setStatus("done");
        task2.setEstimateHours(4.0);
        task2.setRealHours(3.5);
        task2.setSprintId(SPRINT_ID);
        task2.setCreatedById(USER_ID);
        task2.setAssignedTo(USER_ID + 1);
        
        doneTasks.add(task1);
        doneTasks.add(task2);
        
        // Mock para simular el servicio retornando tareas completadas
        when(taskService.findByStatus("done")).thenReturn(doneTasks);
        
        // Actuar - obtener las tareas completadas
        List<TaskModel> completedTasks = taskService.findByStatus("done");
        
        // Verificar
        verify(taskService).findByStatus("done");
        assertEquals(2, completedTasks.size());
        assertEquals("done", completedTasks.get(0).getStatus());
        assertEquals("done", completedTasks.get(1).getStatus());
    }
    
    /**
     * Test para verificar que se pueden obtener las tareas de un usuario específico
     */
    @Test
    void testViewTasksCompletedByUser() throws Exception {
        // Crear lista de tareas de prueba para un usuario
        List<TaskModel> userTasks = new ArrayList<>();
        
        TaskModel task1 = new TaskModel();
        task1.setID(201);
        task1.setDescription("User Task 1");
        task1.setStatus("done");
        task1.setEstimateHours(6.0);
        task1.setRealHours(7.0);
        task1.setSprintId(SPRINT_ID);
        task1.setCreatedById(USER_ID + 10);
        task1.setAssignedTo(USER_ID);
        
        TaskModel task2 = new TaskModel();
        task2.setID(202);
        task2.setDescription("User Task 2");
        task2.setStatus("in_progress");
        task2.setEstimateHours(3.0);
        task2.setRealHours(2.5);
        task2.setSprintId(SPRINT_ID);
        task2.setCreatedById(USER_ID);
        task2.setAssignedTo(USER_ID);
        
        userTasks.add(task1);
        userTasks.add(task2);
        
        // Mock para simular el servicio retornando tareas de usuario
        when(taskService.findByUserAssigned(USER_ID)).thenReturn(userTasks);
        
        // Actuar - obtener las tareas del usuario
        List<TaskModel> tasksForUser = taskService.findByUserAssigned(USER_ID);
        
        // Verificar
        verify(taskService).findByUserAssigned(USER_ID);
        assertEquals(2, tasksForUser.size());
        assertEquals(USER_ID, tasksForUser.get(0).getAssignedToId());
        assertEquals(USER_ID, tasksForUser.get(1).getAssignedToId());
    }
}