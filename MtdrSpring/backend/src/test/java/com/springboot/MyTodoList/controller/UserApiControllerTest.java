package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.controller.utils.TestUtils;
import com.springboot.MyTodoList.model.UserModel;
import com.springboot.MyTodoList.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserApiControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserApiController userApiController;

    public UserApiControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserById() {
        UserModel mockUser = TestUtils.createUserModel();
        when(userService.findUserById(1)).thenReturn(mockUser);

        ResponseEntity<UserModel> response = userApiController.getUserById(1, 1);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        // Assert that project's id in the body matches
        assertEquals(1, response.getBody().getProject().getID());
        verify(userService, times(1)).findUserById(1);
    }
    
    @Test
    void testGetUserByIdNotFoundOnProjectMismatch() {
        // Create a user whose project id = 1 but request with project id 2.
        UserModel mockUser = TestUtils.createUserModel();
        when(userService.findUserById(1)).thenReturn(mockUser);
        
        // Expect not found response when project mismatch
        ResponseEntity<UserModel> response = userApiController.getUserById(2, 1);
        assertEquals(404, response.getStatusCodeValue());
    }
}