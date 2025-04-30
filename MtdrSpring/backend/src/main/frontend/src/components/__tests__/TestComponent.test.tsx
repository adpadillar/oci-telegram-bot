import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import "@testing-library/jest-dom";
import { useExtraSmallScreen } from "../KPIs";

// npx vitest run
// Simple component for testing the hook
const TestComponent = () => {
  const isXs = useExtraSmallScreen();
  return <div data-testid="xs-flag">{isXs ? "true" : "false"}</div>;
};

describe("useExtraSmallScreen hook", () => {
  it("returns true when window.innerWidth is less than 640px", async () => {
    // Simulate extra-small viewport
    window.innerWidth = 500;
    window.dispatchEvent(new Event("resize"));

    render(<TestComponent />);
    await waitFor(() => {
      expect(screen.getByTestId("xs-flag")).toHaveTextContent("true");
    });
  });

  it("returns false when window.innerWidth is greater than or equal to 640px", async () => {
    // Simulate larger viewport
    window.innerWidth = 800;
    window.dispatchEvent(new Event("resize"));

    render(<TestComponent />);
    await waitFor(() => {
      expect(screen.getByTestId("xs-flag")).toHaveTextContent("false");
    });
  });
});
