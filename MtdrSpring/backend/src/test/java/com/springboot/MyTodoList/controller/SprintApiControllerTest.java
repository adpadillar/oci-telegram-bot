package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.controller.utils.TestUtils;
import com.springboot.MyTodoList.model.SprintModel;
import com.springboot.MyTodoList.service.SprintService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SprintApiControllerTest {

    @Mock
    private SprintService sprintService;

    @InjectMocks
    private SprintApiV1Controller sprintApiController;

    public SprintApiControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSprintById() {
        SprintModel mockSprint = TestUtils.createSprintModel();
        when(sprintService.getSprintById(1)).thenReturn(Optional.of(mockSprint));

        ResponseEntity<SprintModel> response = sprintApiController.getSprintById(1, 1);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        // Assert that the returned object matches our condition:
        assertEquals(1, response.getBody().getID());
        verify(sprintService, times(1)).getSprintById(1);
    }
    
    @Test
    void testGetSprintByIdBadProject() {
        // Create a sprint with project id 1, but request with project id 2.
        SprintModel mockSprint = TestUtils.createSprintModel(); 
        when(sprintService.getSprintById(1)).thenReturn(Optional.of(mockSprint));

        ResponseEntity<SprintModel> response = sprintApiController.getSprintById(2, 1);
        assertEquals(400, response.getStatusCodeValue());
    }
    
    @Test
    void testGetSprintByIdNotFound() {
        when(sprintService.getSprintById(999)).thenReturn(Optional.empty());
        ResponseEntity<SprintModel> response = sprintApiController.getSprintById(1, 999);
        assertEquals(404, response.getStatusCodeValue());
    }
}
