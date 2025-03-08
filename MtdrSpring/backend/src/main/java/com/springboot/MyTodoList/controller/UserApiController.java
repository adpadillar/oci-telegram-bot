package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.UserDTO;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserApiController {

    @Autowired
    private UserService userService;

    @GetMapping("/api/{project}/users")
    public List<User> getAllUsers(@PathVariable("project") int project) {
        return userService.findUsersByProject(project);
    }

    @PostMapping("/api/{project}/users")
    public ResponseEntity<User> createUser(@PathVariable("project") int projectId, @RequestBody UserDTO userDTO) {
        // Convert DTO to User entity or pass both to service
        return ResponseEntity.ok(userService.createUser(userDTO, projectId));
    }

    @GetMapping("/api/{project}/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("project") int project, @PathVariable("id") int id) {
        User user = userService.findUserById(id);

        if (user.getProject().getID() != project) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @PatchMapping("/api/{project}/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("project") int project, @PathVariable int id, @RequestBody UserDTO userDetails) {
        User updatedUser = userService.patchUserOnProject(id, project, userDetails);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/api/{project}/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("project") int project, @PathVariable int id) {
        User user = userService.findUserById(id);

        if (user.getProject().getID() != project) {
            return ResponseEntity.notFound().build();
        }

        return userService.deleteUser(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
