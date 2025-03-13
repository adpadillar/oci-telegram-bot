import { z } from "zod";

const userValidator = z.object({
  title: z.string(),
  role: z.enum(["developer", "manager", "user-pending-activation"]),
  firstName: z.string(),
  lastName: z.string(),
});

const sprintValidator = z.object({
  name: z.string(),
});

const projectValidator = z.object({});

const taskValidator = z.object({
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

export type Task = z.infer<typeof taskValidator>;

function createApiClient(baseUrl: string) {
  // for now we are hardcoding the project ID to 1
  const urlWithProject = `${baseUrl}/1`;

  return {
    tasks: {
      list: async () => {
        const response = await fetch(`${urlWithProject}/tasks`);
        const tasks = await response.json();

        const safeParsed = taskValidator.array().safeParse(tasks);

        if (!safeParsed.success) {
          console.error("Error", safeParsed.error);
          throw new Error("Invalid data");
        }

        console.log("Safe parsed", safeParsed.data);

        return safeParsed.data;
      },
    },
  };
}

export const api = createApiClient("http://localhost:8081/api");
