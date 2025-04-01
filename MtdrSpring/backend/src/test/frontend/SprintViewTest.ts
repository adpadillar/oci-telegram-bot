import { describe, it, expect, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import SprintView from "../../main/frontend/src/components/SprintView";
import { api } from "../../main/frontend/src/utils/api/client";

describe("Sprint View Component", () => {
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

  it("should render sprint list correctly", async () => {
    render(<SprintView />);
    expect(await screen.findByText(mockSprint.name)).toBeInTheDocument();
    expect(screen.getByText(/View Tasks/i)).toBeInTheDocument();
  });

  it("should display tasks when View Tasks button is clicked", async () => {
    render(<SprintView />);
    const viewTasksButton = screen.getByText(/View Tasks/i);
    fireEvent.click(viewTasksButton);

    expect(await screen.findByRole("dialog")).toBeInTheDocument();
    expect(
      screen.getByText(/Implement user authentication/i)
    ).toBeInTheDocument();
  });

  it("should handle empty sprints gracefully", async () => {
    // Mock API to return empty sprint list
    vi.spyOn(api.sprints, "getAll").mockResolvedValue([]);
    render(<SprintView />);

    expect(
      await screen.findByText(/No sprints available/i)
    ).toBeInTheDocument();
  });

  it("should show loading state while fetching data", () => {
    render(<SprintView />);
    expect(screen.getByText(/Loading.../i)).toBeInTheDocument();
  });
});
