package com.springboot.MyTodoList.controller;
import com.springboot.MyTodoList.dto.TaskDTO;
import com.springboot.MyTodoList.model.TaskModel;
import com.springboot.MyTodoList.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import java.util.Optional;

@RestController
@Tag(
    name = "Tasks",
    description = "Task management APIs - Core functionality for managing individual work items within sprints"
)
public class TaskApiController {
    @Autowired
    private TaskService taskService;

    @Operation(
        summary = "Get all tasks for a project",
        description = "Retrieves all tasks associated with the specified project. Tasks can be filtered by sprint, status, or assignee through the frontend.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of tasks retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TaskModel.class)),
                    examples = {
                        @ExampleObject(
                            name = "Sample Task List",
                            value = "["
                                + "{"
                                + "\"id\": 1,"
                                + "\"description\": \"Implement user authentication\","
                                + "\"status\": \"in-progress\","
                                + "\"createdById\": 1,"
                                + "\"assignedToId\": 2,"
                                + "\"estimateHours\": 8,"
                                + "\"realHours\": 6,"
                                + "\"sprintId\": 1,"
                                + "\"category\": \"feature\","
                                + "\"projectId\": 1,"
                                + "\"createdAt\": \"2025-01-01T10:00:00Z\""
                                + "}"
                                + "]"
                        )
                    }
                )
            )
        }
    )
    @GetMapping(value = "/api/{project}/tasks")
    public List<TaskModel> getAllToDoItems(
        @Parameter(description = "Project ID to fetch tasks from", required = true, example = "1")
        @PathVariable("project") int project
    ) {
        return taskService.findAllByProjectId(project);
    }

    @Operation(
        summary = "Create a new task",
        description = "Creates a new task in the specified project. Tasks are the fundamental unit of work and can be:\n"
            + "- Assigned to team members\n"
            + "- Categorized as bug/feature/issue\n"
            + "- Given time estimates\n"
            + "- Tracked through various status stages\n"
            + "- Associated with a sprint",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Task created successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input - Missing required fields or invalid values"
            )
        }
    )
    @PostMapping(value = "/api/{project}/tasks")
    public ResponseEntity<Object> addToDoItem(
        @Parameter(
            description = "Task details",
            required = true,
            schema = @Schema(implementation = TaskDTO.class),
            examples = {
                @ExampleObject(
                    name = "New Task Example",
                    value = "{"
                        + "\"description\": \"Implement login page\","
                        + "\"status\": \"created\","
                        + "\"createdBy\": 1,"
                        + "\"assignedTo\": 2,"
                        + "\"estimateHours\": 8,"
                        + "\"sprint\": 1,"
                        + "\"category\": \"feature\""
                        + "}"
                )
            }
        )
        @RequestBody TaskDTO todoItem,
        @Parameter(description = "Project ID to create task in", required = true, example = "1")
        @PathVariable("project") int project
    ) throws Exception {
        taskService.addTodoItemToProject(project, todoItem);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Get task by ID",
        description = "Retrieves detailed information about a specific task, including its status, assignments, time tracking, and sprint association",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Task found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TaskModel.class),
                    examples = {
                        @ExampleObject(
                            name = "Task Details",
                            value = "{"
                                + "\"id\": 1,"
                                + "\"description\": \"Implement user authentication\","
                                + "\"status\": \"in-progress\","
                                + "\"createdById\": 1,"
                                + "\"assignedToId\": 2,"
                                + "\"estimateHours\": 8,"
                                + "\"realHours\": 6,"
                                + "\"sprintId\": 1,"
                                + "\"category\": \"feature\","
                                + "\"projectId\": 1,"
                                + "\"createdAt\": \"2025-01-01T10:00:00Z\""
                                + "}"
                        )
                    }
                )
            ),
            @ApiResponse(responseCode = "404", description = "Task not found or task not in specified project")
        }
    )
    @GetMapping(value = "/api/{project}/tasks/{id}")
    public ResponseEntity<TaskModel> getToDoItemById(
        @Parameter(description = "Project ID", required = true, example = "1")
        @PathVariable("project") int project,
        @Parameter(description = "Task ID to retrieve", required = true, example = "1")
        @PathVariable("id") int id
    ) {
        Optional<TaskModel> maybeTask = taskService.getItemById(id);
        if(maybeTask.isPresent()){
            TaskModel task = maybeTask.get();
            if (task.getProjectId() != project){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(task, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(
        summary = "Update task",
        description = "Updates an existing task's details. Supports partial updates for:\n"
            + "- Status progression (created → in-progress → in-review → testing → done)\n"
            + "- Time tracking (estimate and real hours)\n"
            + "- Reassignment to different team members\n"
            + "- Moving between sprints\n"
            + "- Category changes",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Task updated successfully"
            ),
            @ApiResponse(responseCode = "404", description = "Task not found")
        }
    )
    @PatchMapping(value = "/api/{project}/tasks/{id}")
    public ResponseEntity<Object> updateToDoItem(
        @Parameter(
            description = "Updated task details. Only include fields that need to be updated.\n"
                + "Status values: created, in-progress, in-review, testing, done\n"
                + "Category values: bug, feature, issue",
            schema = @Schema(implementation = TaskDTO.class),
            examples = {
                @ExampleObject(
                    name = "Update Status Example",
                    value = "{\"status\": \"in-progress\"}"
                ),
                @ExampleObject(
                    name = "Update Assignment Example",
                    value = "{\"assignedTo\": 2, \"status\": \"in-progress\"}"
                ),
                @ExampleObject(
                    name = "Update Time Tracking Example",
                    value = "{\"realHours\": 6, \"status\": \"done\"}"
                )
            }
        )
        @RequestBody TaskDTO toDoItem,
        @Parameter(description = "Project ID", required = true, example = "1")
        @PathVariable("project") int project,
        @Parameter(description = "Task ID to update", required = true, example = "1")
        @PathVariable("id") int id
    ) {
        taskService.patchTaskOnProject(id, project, toDoItem);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Delete task",
        description = "Permanently deletes a task. This action cannot be undone.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found or task not in project"),
            @ApiResponse(responseCode = "500", description = "Internal server error while deleting task")
        }
    )
    @DeleteMapping(value = "/api/{project}/tasks/{id}")
    public ResponseEntity<Boolean> deleteToDoItem(
        @Parameter(description = "Project ID", required = true, example = "1")
        @PathVariable("project") int project,
        @Parameter(description = "Task ID to delete", required = true, example = "1")
        @PathVariable("id") int id
    ) {
        TaskModel task = taskService.getItemById(id).orElse(null);
        if(task == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (task.getProjectId() != project){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        boolean deleted = taskService.deleteToDoItem(id);
        if(deleted){
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
