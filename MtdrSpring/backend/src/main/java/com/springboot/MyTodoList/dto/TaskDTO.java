package com.springboot.MyTodoList.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Data Transfer Object for creating and updating tasks"
)
public class TaskDTO {
    @Schema(
        description = "Detailed description of the task",
        example = "Implement JWT-based authentication",
        required = true
    )
    private String description;

    @Schema(
        description = "Current status of the task",
        example = "in-progress",
        allowableValues = {"created", "in-progress", "in-review", "testing", "done"},
        required = true
    )
    private String status;

    @Schema(
        description = "ID of the user who created the task",
        example = "1",
        required = true
    )
    private Integer createdBy;

    @Schema(
        description = "ID of the user assigned to complete this task",
        example = "2"
    )
    private Integer assignedTo;

    @Schema(
        description = "Estimated hours to complete the task",
        example = "8.0",
        minimum = "0"
    )
    private Double estimateHours;

    @Schema(
        description = "Actual hours spent on the task",
        example = "6.5",
        minimum = "0"
    )
    private Double realHours;

    @Schema(
        description = "ID of the sprint this task belongs to",
        example = "1"
    )
    private Integer sprint;

    @Schema(
        description = "Category of the task",
        example = "feature",
        allowableValues = {"bug", "feature", "issue"}
    )
    private String category;

    public TaskDTO() {}

    public TaskDTO(String description, String status, Integer createdBy, Integer assignedTo, Double estimateHours, Double realHours, Integer sprint, String category) {
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
        this.estimateHours = estimateHours;
        this.realHours = realHours;
        this.sprint = sprint;
        this.category = category;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public Integer getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Integer assignedTo) { this.assignedTo = assignedTo; }

    public Double getEstimateHours() { return estimateHours; }
    public void setEstimateHours(Double estimateHours) { this.estimateHours = estimateHours; }

    public Double getRealHours() { return realHours; }
    public void setRealHours(Double realHours) { this.realHours = realHours; }

    public Integer getSprint() { return sprint; }
    public void setSprint(Integer sprint) { this.sprint = sprint; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
