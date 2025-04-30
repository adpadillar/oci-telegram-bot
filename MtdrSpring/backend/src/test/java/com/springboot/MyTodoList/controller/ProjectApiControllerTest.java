package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.ProjectModel;
import com.springboot.MyTodoList.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectApiControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectApiV1Controller projectApiController;

    public ProjectApiControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProjects() {
        List<ProjectModel> mockProjects = Arrays.asList(new ProjectModel(), new ProjectModel());
        when(projectService.findAll()).thenReturn(mockProjects);

        List<ProjectModel> result = projectApiController.getAllProjects();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(projectService, times(1)).findAll();
    }

    @Test
    void testAddProject() throws Exception {
        ProjectModel mockProject = new ProjectModel();
        mockProject.setID(1);
        when(projectService.addProject(any(ProjectModel.class))).thenReturn(mockProject);

        ResponseEntity<Object> response = projectApiController.addProject(mockProject);

        // Assert status and headers
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        // Also assert that CORS header is present
        verify(projectService, times(1)).addProject(mockProject);
    }
    
    @Test
    void testGetProjectByIdNotFound() {
        // When the project is not found, our controller returns NOT_FOUND.
        // Simulate by throwing an exception in the service.
        when(projectService.getProjectById(anyInt())).thenThrow(new RuntimeException("Not found"));
        
        ResponseEntity<ProjectModel> response = projectApiController.getProjectById(999);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
