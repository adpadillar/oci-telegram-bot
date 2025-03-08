package com.springboot.MyTodoList.dto;

import java.time.OffsetDateTime;

public class SprintDTO {
  private String name;
  private String description;
  private OffsetDateTime startedAt;
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
