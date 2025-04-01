import { describe, it, expect, beforeEach } from "vitest";
import { api } from "../../main/frontend/src/utils/api/client";

describe("Telegram Task Creation Integration", () => {
  const mockTask = {
    title: "Implement user authentication",
    description: "Add OAuth2 authentication flow",
    priority: "High",
    category: "Backend",
    status: "To Do",
    estimateHours: 16,
    realHours: 0,
    deadline: "2024-02-28T23:59:59Z",
    assignedTo: {
      firstName: "Jane",
      lastName: "Smith",
      role: "Developer",
    },
  };

  beforeEach(() => {
    // Setup test environment
  });

  it("should create a new task with all required fields", async () => {
    const createdTask = await api.tasks.create(mockTask);
    expect(createdTask).toBeDefined();
    expect(createdTask.title).toBe(mockTask.title);
    expect(createdTask.description).toBe(mockTask.description);
    expect(createdTask.status).toBe(mockTask.status);
  });

  it("should validate required fields before task creation", async () => {
    const invalidTask = { ...mockTask, title: "" };
    await expect(api.tasks.create(invalidTask)).rejects.toThrow();
  });

  it("should handle developer assignment correctly", async () => {
    const task = await api.tasks.create(mockTask);
    expect(task.assignedTo).toBeDefined();
    expect(task.assignedTo.firstName).toBe(mockTask.assignedTo.firstName);
  });

  it("should format dates correctly", () => {
    const task = mockTask;
    expect(new Date(task.deadline).toISOString()).toBe(task.deadline);
  });
});
