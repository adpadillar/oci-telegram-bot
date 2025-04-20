// src/components/__mocks__/mockData.ts

export const mockUsers = [
    { id: 1, firstName: "Ana", lastName: "Ramírez", role: "developer" },
    { id: 2, firstName: "Luis", lastName: "Hernández", role: "developer" },
  ];
  
  export const mockTasks = [
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
  
  export const mockSprints = [
    {
      id: 1,
      name: "Sprint 1",
      description: "Initial sprint",
      startedAt: new Date("2024-03-01").toISOString(),
      endsAt: new Date("2024-03-07").toISOString(),
      projectId: 1,
    },
  ];
  