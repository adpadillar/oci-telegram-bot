import { describe, it, expect, beforeEach } from "vitest";
import { api } from "../../main/frontend/src/utils/api/client";

describe("Telegram Sprint View Integration", () => {
  const mockSprint = {
    id: 1,
    name: "Sprint 1 - Q1 2024",
    startDate: "2024-01-01T00:00:00Z",
    endDate: "2024-01-15T23:59:59Z",
    status: "active",
  };

  beforeEach(() => {
    // Setup test environment
  });

  it("should list all available sprints", async () => {
    const sprints = await api.sprints.getAll();
    expect(sprints).toBeDefined();
    expect(Array.isArray(sprints)).toBe(true);
    expect(sprints[0]).toHaveProperty("name");
    expect(sprints[0]).toHaveProperty("status");
  });

  it("should retrieve tasks for a specific sprint", async () => {
    const sprintTasks = await api.sprints.getTasks(mockSprint.id);
    expect(sprintTasks).toBeDefined();
    expect(Array.isArray(sprintTasks)).toBe(true);
    expect(sprintTasks[0]).toHaveProperty("description");
  });

  it("should handle empty sprints gracefully", async () => {
    const emptySprintTasks = await api.sprints.getTasks(999);
    expect(emptySprintTasks).toHaveLength(0);
  });

  it("should validate sprint date ranges", () => {
    const startDate = new Date(mockSprint.startDate);
    const endDate = new Date(mockSprint.endDate);
    expect(endDate.getTime()).toBeGreaterThan(startDate.getTime());
  });
});
