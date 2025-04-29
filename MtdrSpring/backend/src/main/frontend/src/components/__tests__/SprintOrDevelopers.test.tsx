import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, vi, expect } from "vitest";
import "@testing-library/jest-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import Tasks from "../Tasks";
import * as client from "../../utils/api/client";

// npx vitest run
// Mock the API client
vi.mock("../../utils/api/client", async () => {
  const original = await vi.importActual<typeof client>(
    "../../utils/api/client"
  );

  // Define mock data for this test
  const mockUsers = [
    { id: 3, firstName: "Marcela", lastName: "de la Rosa", role: "developer" },
  ];

  const mockTasks = [
    {
      id: 101,
      description: "Marcela Sprint1 Task",
      status: "done",
      estimateHours: 2,
      realHours: 3,
      createdById: 3,
      assignedToId: 3,
      sprintId: 1,
      projectId: 1,
      createdAt: new Date().toISOString(),
      category: "feature",
    },
    {
      id: 102,
      description: "Marcela Sprint2 Task",
      status: "in-progress",
      estimateHours: 5,
      realHours: null,
      createdById: 3,
      assignedToId: 3,
      sprintId: 2,
      projectId: 1,
      createdAt: new Date().toISOString(),
      category: "bug",
    },
  ];

  const mockSprints = [
    { id: 1, name: "Sprint 1", description: null, startedAt: new Date().toISOString(), endsAt: new Date().toISOString(), projectId: 1 },
    { id: 2, name: "Sprint 2", description: null, startedAt: new Date().toISOString(), endsAt: new Date().toISOString(), projectId: 1 },
  ];

  return {
    ...original,
    api: {
      users: { getDevelopers: vi.fn().mockResolvedValue(mockUsers), getUsers: vi.fn().mockResolvedValue(mockUsers) },
      tasks: { list: vi.fn().mockResolvedValue(mockTasks) },
      sprints: { getSprints: vi.fn().mockResolvedValue(mockSprints) },
    },
  };
});

describe("Developer-to-Tasks Integration", () => {
  it("filters tasks by developer and sprint via query params", async () => {
    const queryClient = new QueryClient();
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={["/tasks?assignee=3&sprint=2"]}>
          <Routes>
            <Route path="/tasks" element={<Tasks />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );

    // Wait for tasks page load
    await waitFor(() => {
      expect(screen.getByPlaceholderText("Search tasks...")).toBeInTheDocument();
    });

    // Sprint2 task should be present, Sprint1 task hidden
    expect(screen.getByText("Marcela Sprint2 Task")).toBeInTheDocument();
    expect(
      screen.queryByText("Marcela Sprint1 Task")
    ).not.toBeInTheDocument();
  });
}); 