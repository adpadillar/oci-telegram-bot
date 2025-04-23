package com.springboot.MyTodoList.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.springboot.MyTodoList.model.UserModel;
import com.springboot.MyTodoList.service.UserService;
import com.springboot.MyTodoList.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
@Tag(
    name = "Authentication",
    description = "Authentication APIs for project managers and team members using Telegram-based two-factor authentication"
)
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

    @Operation(
        summary = "Request login verification code",
        description = "Sends a 6-digit verification code to the project manager's Telegram account for two-factor authentication",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Verification code sent successfully"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Project manager not found or Telegram ID not configured"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Error sending verification code through Telegram"
            )
        }
    )
    @PostMapping("/api/{project}/request-code")
    public ResponseEntity<Void> requestLoginCode(
        @Parameter(description = "Project ID to authenticate for", required = true, example = "1")
        @PathVariable("project") int projectId
    ) {
        UserModel manager = userService.findManagerByProject(projectId);
        if (manager == null || manager.getTelegramId() == null) {
            return ResponseEntity.notFound().build();
        }

        int code = 100000 + secureRandom.nextInt(900000);
        boolean sent = botController.sendLoginCode(manager.getTelegramId(), String.valueOf(code));
        
        return sent ? ResponseEntity.ok().build() : ResponseEntity.internalServerError().build();
    }

    @Operation(
        summary = "Validate login code",
        description = "Validates the provided verification code and issues a JWT token upon successful authentication",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Code validated successfully, returns JWT token",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                        example = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}"
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid or missing verification code"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid verification code or project manager not found"
            )
        }
    )
    @PostMapping("/api/{project}/validate-code")
    public ResponseEntity<Map<String, String>> validateCode(
        @Parameter(description = "Project ID to authenticate for", required = true, example = "1")
        @PathVariable("project") int projectId,
        @Parameter(
            description = "Verification code received via Telegram",
            required = true,
            schema = @Schema(example = "{\"code\": \"123456\"}")
        )
        @RequestBody Map<String, String> payload,
        HttpServletResponse response
    ) {
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
        
        // Create a cookie
        Cookie cookie = new Cookie("auth_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Temporarily disable for non-HTTPS environments
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 24 hours
        
        // Add the cookie to the response
        response.addCookie(cookie);
        
        // Return the token in the response body as well
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("token", token);
        
        return ResponseEntity.ok(responseBody);
    }

    @Operation(
        summary = "Logout user",
        description = "Invalidates the user's authentication by clearing their auth cookie",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully logged out"
            )
        }
    )
    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("auth_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Temporarily disable for non-HTTPS environments
        cookie.setPath("/");
        cookie.setMaxAge(0); // Immediately expire the cookie
        
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Get current user info",
        description = "Retrieves information about the currently authenticated user based on their JWT token",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User information retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                        example = "{\"userId\": 1, \"projectId\": 1, \"role\": \"manager\"}"
                    )
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid or expired token, or user not found"
            )
        }
    )
    @GetMapping("/api/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
        @Parameter(description = "Authentication token from cookie", required = false)
        @CookieValue(name = "auth_token", required = false) String token,
        @Parameter(description = "Authentication token from Authorization header", required = false)
        @RequestHeader(name = "Authorization", required = false) String authHeader
    ) {
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