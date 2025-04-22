package com.springboot.MyTodoList.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @GetMapping("/api/me")
    public ResponseEntity<Map<String, String>> getCurrentToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        System.out.println("Authorization Header: " + authHeader); // Debugging line
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        
        return ResponseEntity.ok(response);
    }
}