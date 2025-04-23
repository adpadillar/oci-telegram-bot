package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.UserDTO;
import com.springboot.MyTodoList.model.UserModel;
import com.springboot.MyTodoList.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@Tag(
    name = "Users",
    description = "User management APIs for handling project team members, including developers and managers"
)
public class UserApiController {

    private final UserService userService;
    private final ToDoItemBotController botController;

    public UserApiController(UserService userService, ToDoItemBotController botController) {
        this.userService = userService;
        this.botController = botController;
    }

    @Operation(
        summary = "Get all users in a project",
        description = "Retrieves all users (developers and managers) associated with the specified project",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of users retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserModel.class)),
                    examples = {
                        @ExampleObject(
                            name = "Sample User List",
                            value = "[" +
                                "{" +
                                "\"id\": 1," +
                                "\"firstName\": \"John\"," +
                                "\"lastName\": \"Doe\"," +
                                "\"telegramId\": 123456789," +
                                "\"role\": \"manager\"," +
                                "\"projectId\": 1" +
                                "}" +
                                "]"
                        )
                    }
                )
            )
        }
    )
    @GetMapping("/api/{project}/users")
    public List<UserModel> getAllUsers(
        @Parameter(description = "Project ID to fetch users from", required = true, example = "1")
        @PathVariable("project") int project
    ) {
        return userService.findUsersByProject(project);
    }

    @Operation(
        summary = "Create new user",
        description = "Creates a new user in the specified project. New users start with 'user-pending-activation' role until approved by a manager",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserModel.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input - Missing required fields or invalid values"
            )
        }
    )
    @PostMapping("/api/{project}/users")
    public ResponseEntity<UserModel> createUser(
        @Parameter(description = "Project ID to create user in", required = true, example = "1")
        @PathVariable("project") int projectId,
        @Parameter(
            description = "User details",
            required = true,
            schema = @Schema(implementation = UserDTO.class),
            examples = {
                @ExampleObject(
                    name = "New User Example",
                    value = "{" +
                        "\"firstName\": \"John\"," +
                        "\"lastName\": \"Doe\"," +
                        "\"telegramId\": 123456789" +
                        "}"
                )
            }
        )
        @RequestBody UserDTO userDTO
    ) {
        return ResponseEntity.ok(userService.createUser(userDTO, projectId));
    }

    @Operation(
        summary = "Get user by ID",
        description = "Retrieves detailed information about a specific user in the project",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserModel.class),
                    examples = {
                        @ExampleObject(
                            name = "User Details",
                            value = "{" +
                                "\"id\": 1," +
                                "\"firstName\": \"John\"," +
                                "\"lastName\": \"Doe\"," +
                                "\"telegramId\": 123456789," +
                                "\"role\": \"developer\"," +
                                "\"projectId\": 1" +
                                "}"
                        )
                    }
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found or user not in specified project"
            )
        }
    )
    @GetMapping("/api/{project}/users/{id}")
    public ResponseEntity<UserModel> getUserById(
        @Parameter(description = "Project ID", required = true, example = "1")
        @PathVariable("project") int project,
        @Parameter(description = "User ID to retrieve", required = true, example = "1")
        @PathVariable("id") int id
    ) {
        UserModel user = userService.findUserById(id);

        if (user.getProjectId() != project) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @Operation(
        summary = "Update user",
        description = "Updates a user's details. Can be used to activate pending users or change roles. When a pending user is activated, they receive a Telegram notification",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User updated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserModel.class)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found or user not in specified project"
            )
        }
    )
    @PatchMapping("/api/{project}/users/{id}")
    public ResponseEntity<UserModel> updateUser(
        @Parameter(description = "Project ID", required = true, example = "1")
        @PathVariable("project") int project,
        @Parameter(description = "User ID to update", required = true, example = "1")
        @PathVariable("id") int id,
        @Parameter(
            description = "Updated user details. Only include fields that need to be updated",
            required = true,
            schema = @Schema(implementation = UserDTO.class),
            examples = {
                @ExampleObject(
                    name = "Activate User Example",
                    value = "{\"role\": \"developer\"}"
                ),
                @ExampleObject(
                    name = "Update Telegram ID Example",
                    value = "{\"telegramId\": 987654321}"
                )
            }
        )
        @RequestBody UserDTO userDetails
    ) {
        UserModel user = userService.findUserById(id);

        if (user.getProjectId() != project) {
            return ResponseEntity.notFound().build();
        }

        if (user.getRole().equals("user-pending-activation") && !userDetails.getRole().equals("user-pending-activation")) {
            // Notify user via Telegram when they are activated
            if (user.getTelegramId() != null) {
                botController.sendActivationNotification(user.getTelegramId());
            }
        }

        UserModel updatedUser = userService.patchUserOnProject(id, project, userDetails);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Delete user",
        description = "Permanently removes a user from the project. This action cannot be undone",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User deleted successfully"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found or user not in specified project"
            )
        }
    )
    @DeleteMapping("/api/{project}/users/{id}")
    public ResponseEntity<Void> deleteUser(
        @Parameter(description = "Project ID", required = true, example = "1")
        @PathVariable("project") int project,
        @Parameter(description = "User ID to delete", required = true, example = "1")
        @PathVariable("id") int id
    ) {
        UserModel user = userService.findUserById(id);

        if (user.getProjectId() != project) {
            return ResponseEntity.notFound().build();
        }

        return userService.deleteUser(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
