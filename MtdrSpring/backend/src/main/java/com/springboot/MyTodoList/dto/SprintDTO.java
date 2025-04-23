package com.springboot.MyTodoList.dto;

import java.time.OffsetDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Data Transfer Object for creating and updating sprints"
)
public class SprintDTO {
    @Schema(
        description = "Name of the sprint",
        example = "Sprint 1 - Authentication",
        required = true
    )
    private String name;

    @Schema(
        description = "Detailed description of sprint goals",
        example = "Implement core authentication features including login, registration, and password reset"
    )
    private String description;

    @Schema(
        description = "Start date and time of the sprint",
        example = "2025-01-01T00:00:00Z",
        required = true
    )
    private OffsetDateTime startedAt;

    @Schema(
        description = "End date and time of the sprint",
        example = "2025-01-14T23:59:59Z",
        required = true
    )
    private OffsetDateTime endsAt;

    public SprintDTO() {
    }

    public SprintDTO(String name, String description, OffsetDateTime startedAt, OffsetDateTime endsAt) {
        this.name = name;
        this.description = description;
        this.startedAt = startedAt;
        this.endsAt = endsAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(OffsetDateTime endsAt) {
        this.endsAt = endsAt;
    }
}
