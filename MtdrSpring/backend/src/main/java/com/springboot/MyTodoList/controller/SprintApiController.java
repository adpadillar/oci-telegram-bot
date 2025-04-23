package com.springboot.MyTodoList.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.springboot.MyTodoList.dto.SprintDTO;
import com.springboot.MyTodoList.model.SprintModel;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.model.TaskModel;

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
@Tag(name = "Sprints", description = "Sprint management APIs - Manage time-boxed iterations of work containing tasks")
public class SprintApiController {
    @Autowired
    private SprintService sprintService;

    @Operation(
        summary = "Get all sprints for a project",
        description = "Retrieves all sprints associated with the specified project. Each sprint includes its name, description, start date, end date, and completion metrics",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "List of sprints retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = SprintModel.class)),
                    examples = {
                        @ExampleObject(
                            name = "Sample Sprint List",
                            value = "[{\"id\": 1, \"name\": \"Sprint 1\", \"description\": \"Initial Development Sprint\", \"startedAt\": \"2025-01-01T00:00:00Z\", \"endsAt\": \"2025-01-14T23:59:59Z\", \"projectId\": 1}]"
                        )
                    }
                )
            )
        }
    )
    @GetMapping("/api/{project}/sprints")
    public List<SprintModel> getAllSprints(
        @Parameter(description = "Project ID to fetch sprints from", required = true, example = "1")
        @PathVariable("project") int project
    ) {
        return sprintService.findByProjectId(project);
    }

    @Operation(
        summary = "Create a new sprint",
        description = "Creates a new sprint in the specified project. A sprint represents a time-boxed iteration of work, typically 1-4 weeks long.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Sprint created successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input - Either missing required fields, invalid date format, or end date before start date"
            )
        }
    )
    @PostMapping("/api/{project}/sprints")
    public ResponseEntity<Object> addSprint(
        @Parameter(description = "Project ID to create sprint in", required = true, example = "1")
        @PathVariable("project") int project,
        @Parameter(
            description = "Sprint details", 
            required = true,
            schema = @Schema(implementation = SprintDTO.class),
            examples = {
                @ExampleObject(
                    name = "New Sprint Example",
                    value = "{\"name\": \"Sprint 1\", \"description\": \"Initial Development Sprint\", \"startedAt\": \"2025-01-01T00:00:00Z\", \"endsAt\": \"2025-01-14T23:59:59Z\"}"
                )
            }
        )
        @RequestBody SprintDTO sprint
    ) throws Exception {
        sprintService.addSprintToProject(project, sprint);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Get sprint by ID",
        description = "Retrieves detailed information about a specific sprint, including its tasks and progress metrics",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Sprint found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SprintModel.class),
                    examples = {
                        @ExampleObject(
                            name = "Sprint Details",
                            value = "{\"id\": 1, \"name\": \"Sprint 1\", \"description\": \"Initial Development Sprint\", \"startedAt\": \"2025-01-01T00:00:00Z\", \"endsAt\": \"2025-01-14T23:59:59Z\", \"projectId\": 1}"
                        )
                    }
                )
            ),
            @ApiResponse(responseCode = "404", description = "Sprint not found"),
            @ApiResponse(responseCode = "400", description = "Sprint not in specified project")
        }
    )
    @GetMapping("/api/{project}/sprints/{id}")
    public ResponseEntity<SprintModel> getSprintById(
        @Parameter(description = "Project ID", required = true, example = "1")
        @PathVariable("project") int project,
        @Parameter(description = "Sprint ID to retrieve", required = true, example = "1")
        @PathVariable("id") int id
    ) {
        Optional<SprintModel> sprint = sprintService.getSprintById(id);
        if(sprint.isPresent()){
            SprintModel s = sprint.get();
            if (s.getProjectId() == project) {
                return new ResponseEntity<>(sprint.get(), HttpStatus.OK);
            }
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @Operation(
        summary = "Update sprint",
        description = "Updates an existing sprint's details. Can modify name, description, start date, and end date. Cannot change project association.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Sprint updated successfully",
                content = @Content(schema = @Schema(implementation = SprintModel.class))
            ),
            @ApiResponse(responseCode = "404", description = "Sprint not found")
        }
    )
    @PatchMapping("/api/{project}/sprints/{id}")
    public ResponseEntity<Object> updateSprint(
        @Parameter(description = "Project ID", required = true, example = "1")
        @PathVariable("project") int project,
        @Parameter(
            description = "Updated sprint details. Only include fields that need to be updated",
            schema = @Schema(implementation = SprintDTO.class),
            examples = {
                @ExampleObject(
                    name = "Update Sprint Example",
                    value = "{\"name\": \"Sprint 1 - Updated\", \"description\": \"Updated sprint description\"}"
                )
            }
        )
        @RequestBody SprintDTO sprint,
        @Parameter(description = "Sprint ID to update", required = true, example = "1")
        @PathVariable("id") int id
    ) {
        try {
            SprintModel sprint1 = sprintService.patchSprintFromProject(id, project, sprint);
            return new ResponseEntity<>(sprint1, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
        summary = "Get tasks for sprint",
        description = "Retrieves all tasks associated with a specific sprint, including their status, assignees, and time tracking information",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Tasks retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TaskModel.class)),
                    examples = {
                        @ExampleObject(
                            name = "Sprint Tasks Example",
                            value = "[{\"id\": 1, \"description\": \"Implement login\", \"status\": \"in-progress\", \"estimateHours\": 8, \"realHours\": 6, \"category\": \"feature\"}]"
                        )
                    }
                )
            ),
            @ApiResponse(responseCode = "404", description = "Sprint not found or sprint not in project")
        }
    )
    @GetMapping("/api/{project}/sprints/{id}/tasks")
    public ResponseEntity<List<TaskModel>> getTasksForSprint(
        @Parameter(description = "Project ID", required = true, example = "1")
        @PathVariable("project") int project,
        @Parameter(description = "Sprint ID", required = true, example = "1")
        @PathVariable("id") int id
    ) {
        try {
            List<TaskModel> tasks = sprintService.getTasksBySprintId(id, project);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
