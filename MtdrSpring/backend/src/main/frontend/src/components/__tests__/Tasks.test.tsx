import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { describe, it, vi, expect } from "vitest";
import "@testing-library/jest-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import Tasks from "../Tasks";
import * as client from "../../utils/api/client";

// npx vitest run
// Mock the API client
vi.mock("../../utils/api/client", async () => {
  const original = await vi.importActual<typeof client>(
    "../../utils/api/client",
  );

  const mockUsers = [
    { id: 1, firstName: "Ana", lastName: "Ramírez", role: "developer" },
    { id: 2, firstName: "Luis", lastName: "Hernández", role: "developer" },
  ];

  const mockTasks = [
    {
      id: 1,
      description: "Fix login bug",
      status: "done",
      estimateHours: 4,
      realHours: 5,
      createdById: 1,
      assignedToId: 1,
      sprintId: 1,
      projectId: 1,
      createdAt: new Date().toISOString(),
      category: "bug",
    },
    {
      id: 2,
      description: "Implement UI",
      status: "in-progress",
      estimateHours: 6,
      realHours: null,
      createdById: 2,
      assignedToId: 2,
      sprintId: 1,
      projectId: 1,
      createdAt: new Date().toISOString(),
      category: "feature",
    },
  ];

  const mockSprints = [
    {
      id: 1,
      name: "Sprint 1",
      description: "Initial sprint",
      startedAt: new Date("2024-03-01").toISOString(),
      endsAt: new Date("2024-03-07").toISOString(),
      projectId: 1,
    },
  ];

  return {
    ...original,
    api: {
      tasks: {
        list: vi.fn().mockResolvedValue(mockTasks),
        create: vi.fn().mockResolvedValue({ ...mockTasks[0], id: 3 }),
        patch: vi
          .fn()
          .mockResolvedValue({ ...mockTasks[0], description: "Updated task" }),
        delete: vi.fn().mockResolvedValue({}),
      },
      users: {
        getUsers: vi.fn().mockResolvedValue(mockUsers),
      },
      sprints: {
        getSprints: vi.fn().mockResolvedValue(mockSprints),
      },
    },
  };
});

// Render helper
function renderTasks() {
  const queryClient = new QueryClient();
  return render(
    <MemoryRouter>
      <QueryClientProvider client={queryClient}>
        <Tasks />
      </QueryClientProvider>
    </MemoryRouter>,
  );
}

describe("Tasks Component", () => {
  it("renders the Tasks title", async () => {
    renderTasks();
    await waitFor(() => {
      expect(screen.getByText("Tasks")).toBeInTheDocument();
    });
  });

  it("displays the correct number of tasks", async () => {
    renderTasks();
    await waitFor(() => {
      expect(screen.getByText("2 tasks")).toBeInTheDocument();
    });
  });

  it("renders task cards with correct information", async () => {
    renderTasks();
    await waitFor(() => {
      expect(screen.getByText("Fix login bug")).toBeInTheDocument();
      expect(screen.getByText("Implement UI")).toBeInTheDocument();
    });
  });

  it("searches tasks by description", async () => {
    renderTasks();
    await waitFor(() => {
      const searchInput = screen.getByPlaceholderText("Search tasks...");
      fireEvent.change(searchInput, { target: { value: "login" } });

      expect(screen.getByText("Fix login bug")).toBeInTheDocument();
      expect(screen.queryByText("Implement UI")).not.toBeInTheDocument();
    });
  });

  it("displays task status correctly", async () => {
    renderTasks();
    await waitFor(() => {
      expect(screen.getByText("Done")).toBeInTheDocument();
      expect(screen.getByText("In Progress")).toBeInTheDocument();
    });
  });

  it("shows task estimates and actual hours", async () => {
    renderTasks();
    await waitFor(() => {
      expect(screen.getByText("4h")).toBeInTheDocument();
      expect(screen.getByText("5h")).toBeInTheDocument();
      expect(screen.getByText("6h")).toBeInTheDocument();
    });
  });

  it("allows editing task description (state change)", async () => {
    renderTasks();
    // Wait for edit buttons to appear
    await waitFor(() => {
      expect(screen.getAllByTitle("Edit task")[0]).toBeInTheDocument();
    });
    // Open edit modal for first task
    fireEvent.click(screen.getAllByTitle("Edit task")[0]);
    // Change description
    const descriptionInput = screen.getByLabelText("Task Description") as HTMLInputElement;
    fireEvent.change(descriptionInput, { target: { value: "Updated task" } });
    // Submit update
    expect(descriptionInput.value).toBe("Updated task");
    fireEvent.click(screen.getByRole("button", { name: /Update Task/i }));
    // Assert input value updated and patch API called with correct payload
    await waitFor(() => {
      expect(client.api.tasks.patch).toHaveBeenCalledWith(
        1,
        expect.objectContaining({ description: "Updated task" })
      );
    });
  });

  it("marks a task as completed", async () => {
    renderTasks();
    // Wait for edit buttons to appear
    await waitFor(() => {
      expect(screen.getAllByTitle("Edit task")[1]).toBeInTheDocument();
    });
    // Open edit modal for second task (in-progress)
    fireEvent.click(screen.getAllByTitle("Edit task")[1]);
    // Change status to Done
    const statusSelect = screen.getByLabelText("Status") as HTMLSelectElement;
    fireEvent.change(statusSelect, { target: { value: "done" } });
    // Submit update
    fireEvent.click(screen.getByRole("button", { name: /Update Task/i }));
    // Assert patch API called with correct args
    await waitFor(() => {
      expect(client.api.tasks.patch).toHaveBeenCalledWith(
        2,
        expect.objectContaining({ status: "done" })
      );
    });
  });

  it("matches Tasks component snapshot", async () => {
    const { container } = renderTasks();
    await waitFor(() => {
      expect(screen.getByText("Tasks")).toBeInTheDocument();
    });
    expect(container).toMatchSnapshot();
  });
});
