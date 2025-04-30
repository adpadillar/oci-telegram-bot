// src/components/__mocks__/mockData.ts

// Define the role type to match the API
type UserRole = "developer" | "manager" | "user-pending-activation";

// Mock data with correct types matching the API client
export const mockTasks = [
  {
    id: 1,
    description: "Fix login bug",
    status: "done",
    estimateHours: 4 as number | null,
    realHours: 5 as number | null,
    createdById: 1,
    assignedToId: 1 as number | null,
    sprintId: 1 as number | null,
    projectId: 1,
    createdAt: new Date(),
    category: "bug" as string | null,
    dueDate: null as Date | null,
  },
  {
    id: 2,
    description: "Implement UI",
    status: "in-progress",
    estimateHours: 6 as number | null,
    realHours: null,
    createdById: 2,
    assignedToId: 2 as number | null,
    sprintId: 1 as number | null,
    projectId: 1,
    createdAt: new Date(),
    category: "feature" as string | null,
    dueDate: null as Date | null,
  },
];

export const mockUsers = [
  {
    id: 1,
    firstName: "Ana",
    lastName: "Ramírez",
    role: "developer" as UserRole,
    title: null as string | null,
  },
  {
    id: 2,
    firstName: "Luis",
    lastName: "Hernández",
    role: "developer" as UserRole,
    title: null as string | null,
  },
];

export const mockSprints = [
  {
    id: 1,
    name: "Sprint 1",
    description: "Initial sprint" as string | null,
    startedAt: new Date("2024-03-01"),
    endsAt: new Date("2024-03-07"),
    projectId: 1,
  },
];
