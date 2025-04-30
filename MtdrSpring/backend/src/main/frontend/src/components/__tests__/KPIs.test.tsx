import {
  render,
  screen,
  waitFor,
  fireEvent,
  within,
} from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import KPIs from "../KPIs";
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
      tasks: { list: vi.fn().mockResolvedValue(mockTasks) },
      users: { getUsers: vi.fn().mockResolvedValue(mockUsers) },
      sprints: { getSprints: vi.fn().mockResolvedValue(mockSprints) },
    },
  };
});

// Helper to render KPIs with React Query context
function renderKpis() {
  const queryClient = new QueryClient();
  return render(
    <QueryClientProvider client={queryClient}>
      <KPIs />
    </QueryClientProvider>,
  );
}

// Stub ResizeObserver for Chart.js
// @vitest-environment jsdom
global.ResizeObserver = class {
  observe() {}
  unobserve() {}
  disconnect() {}
};

describe("KPIs Component", () => {
  it("shows team metrics per sprint", async () => {
    renderKpis();
    // Wait for the dashboard heading
    await waitFor(() => {
      expect(
        screen.getByRole("heading", { name: /KPI Dashboard/i }),
      ).toBeInTheDocument();
    });
    // Switch to Sprints view
    fireEvent.click(screen.getByRole("button", { name: /Sprints/i }));

    // Wait for team metrics table
    await waitFor(() => {
      expect(screen.getByText("Team Metrics per Sprint")).toBeInTheDocument();
    });

    // Check the data row for Sprint 1
    const row = screen.getByText("Sprint 1").closest("tr");
    expect(row).toBeTruthy();
    if (row) {
      // Total tasks = 2
      expect(within(row).getByText("2")).toBeInTheDocument();
      // Completed tasks = 1
      expect(within(row).getByText("1")).toBeInTheDocument();
      // Hours worked = 5h
      expect(within(row).getByText("5h")).toBeInTheDocument();
      // Completion rate = 50%
      expect(within(row).getByText("50%")).toBeInTheDocument();
    }
  });

  it("shows developer metrics per sprint", async () => {
    renderKpis();
    // Wait for dashboard heading
    await waitFor(() => {
      expect(
        screen.getByRole("heading", { name: /KPI Dashboard/i }),
      ).toBeInTheDocument();
    });
    // Switch to Developers view
    fireEvent.click(screen.getByRole("button", { name: /Developers/i }));

    // Wait for team performance table
    await waitFor(() => {
      expect(screen.getByText("Team Performance")).toBeInTheDocument();
    });

    // Check the row for Ana Ramírez
    const anaRow = screen.getByText("Ana Ramírez").closest("tr");
    expect(anaRow).toBeTruthy();
    if (anaRow) {
      const cells = within(anaRow).getAllByRole("cell");
      // cells[0] = Developer, [1]=totalTasks, [2]=completed, [3]=hours, [4]=rate
      expect(cells[1]).toHaveTextContent("1");
      expect(cells[2]).toHaveTextContent("1");
      expect(cells[3]).toHaveTextContent("5h");
      expect(cells[4]).toHaveTextContent(/100%/);
    }
  });
});
