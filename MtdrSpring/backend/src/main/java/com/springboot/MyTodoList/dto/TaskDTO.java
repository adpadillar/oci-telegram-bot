package com.springboot.MyTodoList.dto;

public class TaskDTO {
  private String description;
  private String status;
  private Integer createdBy;
  private Integer assignedTo;
  private Double estimateHours;
  private Double realHours;
  private Integer sprint;
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
