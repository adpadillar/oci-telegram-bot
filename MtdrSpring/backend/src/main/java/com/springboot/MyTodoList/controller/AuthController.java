package com.springboot.MyTodoList.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.springboot.MyTodoList.model.UserModel;
import com.springboot.MyTodoList.service.UserService;
import com.springboot.MyTodoList.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {
    
    private final UserService userService;
    private final ToDoItemBotController botController;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${auth.mastercode}")
    private String masterCode;

    public AuthController(UserService userService, ToDoItemBotController botController, JwtService jwtService) {
        this.userService = userService;
        this.botController = botController;
        this.jwtService = jwtService;
    }

    @PostMapping("/api/{project}/request-code")
    public ResponseEntity<Void> requestLoginCode(@PathVariable("project") int projectId) {
        UserModel manager = userService.findManagerByProject(projectId);
        if (manager == null || manager.getTelegramId() == null) {
            return ResponseEntity.notFound().build();
        }

        int code = 100000 + secureRandom.nextInt(900000);
        boolean sent = botController.sendLoginCode(manager.getTelegramId(), String.valueOf(code));
        
        return sent ? ResponseEntity.ok().build() : ResponseEntity.internalServerError().build();
    }

    @PostMapping("/api/{project}/validate-code")
    public ResponseEntity<Map<String, String>> validateCode(
            @PathVariable("project") int projectId,
            @RequestBody Map<String, String> payload,
            HttpServletResponse response) {
        
        String code = payload.get("code");
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        boolean isValid = masterCode.equals(code) || botController.validateLoginCode(code);
        
        if (!isValid) {
            return ResponseEntity.status(401).build();
        }

        // Get the manager's ID for the JWT
        UserModel manager = userService.findManagerByProject(projectId);
        if (manager == null) {
            return ResponseEntity.status(401).build();
        }

        // Generate JWT
        String token = jwtService.generateToken(manager.getID(), projectId);
        
        // Create a secure cookie
        Cookie cookie = new Cookie("auth_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Enable in production
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 24 hours
        
        // Add the cookie to the response
        response.addCookie(cookie);
        
        // Return the token in the response body as well
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("token", token);
        
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("auth_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Immediately expire the cookie
        
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @CookieValue(name = "auth_token", required = false) String token,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        
        // Try to get token from cookie first, then from Authorization header
        String jwtToken = token;
        if (jwtToken == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        }

        if (jwtToken == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Integer userId = jwtService.getUserIdFromToken(jwtToken);
            Integer projectId = jwtService.getProjectIdFromToken(jwtToken);
            
            UserModel user = userService.findUserById(userId);
            if (user == null || !user.getProjectId().equals(projectId)) {
                return ResponseEntity.status(401).build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("projectId", projectId);
            response.put("role", user.getRole());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}