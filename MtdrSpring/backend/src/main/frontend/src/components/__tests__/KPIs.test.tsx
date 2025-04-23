import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, vi, expect } from "vitest";
import "@testing-library/jest-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import KPIs from "../KPIs";
import * as client from "../../utils/api/client";

// Mock HTMLCanvasElement
HTMLCanvasElement.prototype.getContext = vi.fn();

// ✅ Mocks dentro del vi.mock, para evitar hoisting error
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
      },
      users: {
        getDevelopers: vi.fn().mockResolvedValue(mockUsers),
      },
      sprints: {
        getSprints: vi.fn().mockResolvedValue(mockSprints),
      },
    },
  };
});

// Render helper
function renderKPIs() {
  const queryClient = new QueryClient();
  return render(
    <MemoryRouter>
      <QueryClientProvider client={queryClient}>
        <KPIs />
      </QueryClientProvider>
    </MemoryRouter>,
  );
}

describe("KPIs Component", () => {
  it("renders the KPI Dashboard title", async () => {
    renderKPIs();
    await waitFor(() => {
      expect(screen.getByText("KPI Dashboard")).toBeInTheDocument();
    });
  });

  it("renders task status chart with data", async () => {
    renderKPIs();
    await waitFor(() => {
      expect(screen.getByText("Tasks by Status")).toBeInTheDocument();
    });
  });

  it("displays total tasks and completion rate", async () => {
    renderKPIs();
    await waitFor(() => {
      expect(screen.getByText("Task Overview")).toBeInTheDocument();
      expect(screen.getByText("50%")).toBeInTheDocument();
    });
  });

  it("displays developer metrics including task count", async () => {
    renderKPIs();
    await waitFor(() => {
      expect(screen.getByText("Team Size")).toBeInTheDocument();
    });
    // Check for text "2" (developers), ideally refine this if there's conflict
    const twos = screen.getAllByText("2");
    expect(twos.length).toBeGreaterThan(0);
  });

  it("displays bar chart of hours per sprint", async () => {
    renderKPIs();
    await waitFor(() => {
      screen.getByText("Sprints").click();
    });
    await waitFor(() => {
      expect(screen.getByText("Hours per Sprint")).toBeInTheDocument();
    });
  });
});
