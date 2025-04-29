package com.springboot.MyTodoList.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.springboot.MyTodoList.model.ProjectModel;
import com.springboot.MyTodoList.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@Tag(name = "Projects", description = "Project management APIs - Core entity that groups sprints, tasks and team members")
public class ProjectApiController {
    @Autowired
    private ProjectService projectService;

    @Operation(
        summary = "Get all projects",
        description = "Retrieves a list of all projects with their basic information including name, start time, and end time",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of projects retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProjectModel.class),
                    examples = {
                        @ExampleObject(
                            name = "Sample Project List",
                            value = "[{\"id\": 1, \"name\": \"MyTodoList App\", \"startTime\": \"2025-01-01T00:00:00Z\", \"endTime\": \"2025-12-31T23:59:59Z\"}]"
                        )
                    }
                )
            )
        }
    )
    @GetMapping(value = "/api/projects")
    public List<ProjectModel> getAllProjects() {
        return projectService.findAll();
    }

    @Operation(
        summary = "Create a new project",
        description = "Creates a new project with the specified name, start time, and end time. The project ID will be auto-generated.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Project created successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input - Missing required fields or invalid date format"
            )
        }
    )
    @PostMapping(value = "/api/projects")
    public ResponseEntity<Object> addProject(
        @Parameter(
            description = "Project details",
            required = true,
            schema = @Schema(implementation = ProjectModel.class),
            examples = {
                @ExampleObject(
                    name = "New Project Example",
                    value = "{\"name\": \"New Project\", \"startTime\": \"2025-01-01T00:00:00Z\", \"endTime\": \"2025-12-31T23:59:59Z\"}"
                )
            }
        ) 
        @RequestBody ProjectModel project
    ) throws Exception {
        projectService.addProject(project);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Get project by ID",
        description = "Retrieves detailed information about a specific project, including its name, start time, and end time",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Project found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProjectModel.class),
                    examples = {
                        @ExampleObject(
                            name = "Project Details",
                            value = "{\"id\": 1, \"name\": \"MyTodoList App\", \"startTime\": \"2025-01-01T00:00:00Z\", \"endTime\": \"2025-12-31T23:59:59Z\"}"
                        )
                    }
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Project not found"
            )
        }
    )
    @GetMapping(value = "/api/projects/{id}")
    public ResponseEntity<ProjectModel> getProjectById(
        @Parameter(description = "Project ID to retrieve", example = "1") 
        @PathVariable int id
    ) {
        try {
            ProjectModel project = projectService.getProjectById(id);
            return new ResponseEntity<>(project, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
