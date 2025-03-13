import { z } from "zod";

export const userValidator = z.object({
  id: z.number(),
  title: z.string().nullable(),
  role: z.enum(["developer", "manager", "user-pending-activation"]),
  firstName: z.string(),
  lastName: z.string(),
});

export const sprintValidator = z.object({
  id: z.number(),
  name: z.string(),
});

export const projectValidator = z.object({
  id: z.number(),
  name: z.string(),
});

export const taskResponseValidator = z.object({
  id: z.number(),
  description: z.string(),
  createdAt: z.string().transform((value) => new Date(value)),
  status: z.string(),
  createdBy: userValidator,
  assignedTo: userValidator.nullable(),
  estimateHours: z.number().nullable(),
  realHours: z.number().nullable(),
  sprint: sprintValidator.nullable(),
  project: projectValidator,
  category: z.string().nullable(),
});

export const taskRequestValidator = z.object({
  description: z.string(),
  status: z.enum(["created", "in-progress", "in-review", "testing", "done"]),
  createdBy: z.number(),
  assignedTo: z.number().nullable(),
  estimateHours: z.number().nullable(),
  realHours: z.number().nullable(),
  sprint: z.number().nullable(),
  category: z.enum(["feature", "bug", "issue"]).nullable(),
});

export type TaskResponse = z.infer<typeof taskResponseValidator>;
export type TaskRequest = z.infer<typeof taskRequestValidator>;

function createApiClient(baseUrl: string) {
  // for now we are hardcoding the project ID to 1
  const urlWithProject = `${baseUrl}/1`;

  async function getProjectManager() {
    const response = await fetch(`${urlWithProject}/users`);
    const responseJson = await response.json();

    const safeParsed = userValidator.array().safeParse(responseJson);

    if (!safeParsed.success) {
      console.error("Error", safeParsed.error);
      throw new Error("Invalid data");
    }

    console.log("Safe parsed", safeParsed.data);

    const manager = safeParsed.data.find((user) => user.role === "manager");

    if (!manager) {
      throw new Error("No manager found");
    }

    return manager;
  }

  async function getProjectDevelopers() {
    const response = await fetch(`${urlWithProject}/users`);
    const responseJson = await response.json();

    const safeParsed = userValidator.array().safeParse(responseJson);

    if (!safeParsed.success) {
      console.error("Error", safeParsed.error);
      throw new Error("Invalid data");
    }

    console.log("Safe parsed", safeParsed.data);

    return safeParsed.data.filter((user) => user.role === "developer");
  }

  async function listTasks() {
    const response = await fetch(`${urlWithProject}/tasks`);
    const tasks = await response.json();

    const safeParsed = taskResponseValidator.array().safeParse(tasks);

    if (!safeParsed.success) {
      console.error("Error", safeParsed.error);
      throw new Error("Invalid data");
    }

    console.log("Safe parsed", safeParsed.data);

    return safeParsed.data.sort(
      (a, b) => b.createdAt.getTime() - a.createdAt.getTime()
    );
  }

  async function createTask(task: Omit<TaskRequest, "createdBy">) {
    const manager = await getProjectManager();

    return fetch(`${urlWithProject}/tasks`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        ...task,
        createdBy: manager.id, // updated to use the manager's ID
      }),
    });
  }

  async function patchTask(id: number, task: Partial<TaskRequest>) {
    return fetch(`${urlWithProject}/tasks/${id}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(task),
    });
  }

  async function deleteTask(id: number) {
    return fetch(`${urlWithProject}/tasks/${id}`, {
      method: "DELETE",
    });
  }

  return {
    tasks: {
      list: listTasks,
      create: createTask,
      patch: patchTask,
      delete: deleteTask,
    },
    users: {
      getManager: getProjectManager,
      getDevelopers: getProjectDevelopers,
    },
  };
}

export const api = createApiClient("http://localhost:8081/api");
