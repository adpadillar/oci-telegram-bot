import { useQuery } from "@tanstack/react-query";
import { api } from "../utils/api/client";

export function useTasks() {
  return useQuery({
    queryFn: api.tasks.list,
    queryKey: ["tasks"],
  });
}
