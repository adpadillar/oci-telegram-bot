import React, { useState, useMemo } from "react";
import {
  Plus,
  Filter,
  Bug,
  Sparkles,
  LayoutGrid,
  List,
  Pencil,
  Trash2,
  Loader2,
} from "lucide-react";
import { api, TaskRequest, TaskResponse } from "../utils/api/client";
import { useMutation, useQuery } from "@tanstack/react-query";
import { queryClient } from "../utils/query/query-client";

const Tasks: React.FC = () => {
  const [showAddModal, setShowAddModal] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [viewMode, setViewMode] = useState<"kanban" | "table">("table");
  const [taskToEdit, setTaskToEdit] = useState<TaskResponse | null>(null);

  // Add filter state
  const [categoryFilter, setCategoryFilter] = useState<string>("");
  const [statusFilter, setStatusFilter] = useState<string>("");
  const [assigneeFilter, setAssigneeFilter] = useState<number | null>(null);

  const { data: tasks } = useQuery({
    queryFn: api.tasks.list,
    queryKey: ["tasks"],
  });

  const { data: users } = useQuery({
    queryFn: api.users.getUsers,
    queryKey: ["users"],
  });

  // Filter tasks using the defined filters
  const filteredTasks = useMemo(() => {
    if (!tasks) return [];

    return tasks.filter((task) => {
      // Category filter
      if (categoryFilter && task.category !== categoryFilter) {
        return false;
      }

      // Status filter
      if (statusFilter && task.status !== statusFilter) {
        return false;
      }

      // Assignee filter
      if (assigneeFilter !== null) {
        if (!task.assignedTo || task.assignedTo.id !== assigneeFilter) {
          return false;
        }
      }

      return true;
    });
  }, [tasks, categoryFilter, statusFilter, assigneeFilter]);

  const TaskCard = ({ task }: { task: TaskResponse }) => (
    <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition">
      <div className="flex justify-between items-start mb-2">
        <h3 className="text-lg font-medium text-gray-900">
          {task.description}
        </h3>
        <span
          className={`
          px-2 py-1 rounded-full text-xs font-medium flex items-center gap-1
          ${
            task.category === "bug"
              ? "bg-red-100 text-red-800"
              : task.category === "issue"
              ? "bg-yellow-100 text-yellow-800"
              : "bg-purple-100 text-purple-800"
          }
        `}
        >
          {task.category === "bug" ? (
            <Bug size={12} />
          ) : task.category === "issue" ? (
            <Filter size={12} />
          ) : (
            <Sparkles size={12} />
          )}
          {task.category === "bug"
            ? "Bug"
            : task.category === "issue"
            ? "Issue"
            : "Feature"}
        </span>
      </div>
      <div className="flex flex-wrap gap-2 text-sm text-gray-600">
        <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded-full">
          {task.sprint?.name ?? "N/A"}
        </span>
        <span>
          Created:{" "}
          {task.createdAt.toLocaleDateString("en-MX", {
            year: "numeric",
            month: "short",
            day: "numeric",
          })}
        </span>
        {task.assignedTo && (
          <span className="flex items-center gap-1">
            <img
              src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${task.assignedTo.id}`}
              alt={`${task.assignedTo.firstName} ${task.assignedTo.lastName}'s avatar`}
              className="w-4 h-4 rounded-full"
            />
            <span>
              {task.assignedTo.firstName} {task.assignedTo.lastName}
            </span>
          </span>
        )}
      </div>
    </div>
  );

  const AddTaskModal = ({ onClose }: { onClose: () => void }) => {
    const [description, setDescription] = useState("");
    const [category, setCategory] = useState<
      "bug" | "feature" | "issue" | null
    >("feature");
    const [sprint, setSprint] = useState<number | null>(1);
    const [assignedTo, setAssignedTo] = useState<number | null>(null);
    const [status, setStatus] = useState<
      "created" | "in-progress" | "in-review" | "testing" | "done"
    >("created");
    const [estimateHours, setEstimateHours] = useState<number | null>(null);
    const [realHours, setRealHours] = useState<number | null>(null);

    const createTaskMutation = useMutation({
      mutationFn: (taskData: Omit<TaskRequest, "createdBy">) => {
        return api.tasks.create(taskData);
      },
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ["tasks"] });
        onClose();
      },
      onError: (error) => {
        console.error("Failed to create task:", error);
        // Could add error handling UI here
      },
    });

    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault();

      createTaskMutation.mutate({
        description,
        status,
        assignedTo,
        estimateHours,
        realHours,
        sprint,
        category,
      });
    };

    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold text-gray-800">Add New Task</h2>
            <button
              onClick={onClose}
              className="text-gray-600 hover:text-gray-900"
            >
              ✕
            </button>
          </div>
          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label className="block text-gray-700">Task Description</label>
              <input
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full px-3 py-2 border rounded-md"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Category</label>
              <select
                value={category || ""}
                onChange={(e) =>
                  setCategory(
                    e.target.value as "bug" | "feature" | "issue" | null
                  )
                }
                className="w-full px-3 py-2 border rounded-md"
              >
                <option value="feature">Feature</option>
                <option value="bug">Bug</option>
                <option value="issue">Issue</option>
                <option value="">None</option>
              </select>
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Sprint</label>
              <input
                type="number"
                value={sprint === null ? "" : sprint}
                onChange={(e) =>
                  setSprint(e.target.value ? Number(e.target.value) : null)
                }
                className="w-full px-3 py-2 border rounded-md"
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Estimate (hours)</label>
              <input
                type="number"
                value={estimateHours === null ? "" : estimateHours}
                onChange={(e) =>
                  setEstimateHours(
                    e.target.value ? Number(e.target.value) : null
                  )
                }
                className="w-full px-3 py-2 border rounded-md"
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Real (hours)</label>
              <input
                type="number"
                value={realHours === null ? "" : realHours}
                onChange={(e) =>
                  setRealHours(e.target.value ? Number(e.target.value) : null)
                }
                className="w-full px-3 py-2 border rounded-md"
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Assigned To</label>
              <select
                value={assignedTo === null ? "" : assignedTo}
                onChange={(e) =>
                  setAssignedTo(e.target.value ? Number(e.target.value) : null)
                }
                className="w-full px-3 py-2 border rounded-md"
              >
                <option value="">Select User</option>
                {users?.map((dev) => (
                  <option key={dev.id} value={dev.id}>
                    {dev.firstName} {dev.lastName}
                  </option>
                ))}
              </select>
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Status</label>
              <select
                value={status}
                onChange={(e) =>
                  setStatus(
                    e.target.value as
                      | "created"
                      | "in-progress"
                      | "in-review"
                      | "testing"
                      | "done"
                  )
                }
                className="w-full px-3 py-2 border rounded-md"
              >
                <option value="created">Created</option>
                <option value="in-progress">In Progress</option>
                <option value="in-review">In Review</option>
                <option value="testing">Testing</option>
                <option value="done">Done</option>
              </select>
            </div>
            <div className="flex justify-end">
              <button
                type="submit"
                className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600"
                disabled={createTaskMutation.isPending}
              >
                {createTaskMutation.isPending ? "Adding..." : "Add Task"}
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  const EditTaskModal = ({
    task,
    onClose,
  }: {
    task: TaskResponse;
    onClose: () => void;
  }) => {
    const [description, setDescription] = useState(task.description);
    const [category, setCategory] = useState<
      "bug" | "feature" | "issue" | null
    >(task.category as "bug" | "feature" | "issue" | null);
    const [sprint, setSprint] = useState<number | null>(
      task.sprint?.id || null
    );
    const [assignedTo, setAssignedTo] = useState<number | null>(
      task.assignedTo?.id || null
    );
    const [status, setStatus] = useState<
      "created" | "in-progress" | "in-review" | "testing" | "done"
    >(
      task.status as
        | "created"
        | "in-progress"
        | "in-review"
        | "testing"
        | "done"
    );
    const [estimateHours, setEstimateHours] = useState<number | null>(
      task.estimateHours
    );

    const [realHours, setRealHours] = useState<number | null>(task.realHours);

    const updateTaskMutation = useMutation({
      mutationFn: (taskData: Partial<TaskRequest>) => {
        return api.tasks.patch(task.id, taskData);
      },
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ["tasks"] });
        onClose();
      },
      onError: (error) => {
        console.error("Failed to update task:", error);
        // Could add error handling UI here
      },
    });

    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault();

      updateTaskMutation.mutate({
        description,
        status,
        assignedTo,
        estimateHours,
        realHours,
        sprint,
        category,
      });
    };

    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold text-gray-800">Edit Task</h2>
            <button
              onClick={onClose}
              className="text-gray-600 hover:text-gray-900"
            >
              ✕
            </button>
          </div>
          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label className="block text-gray-700">Task Description</label>
              <input
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full px-3 py-2 border rounded-md"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Category</label>
              <select
                value={category || ""}
                onChange={(e) =>
                  setCategory(
                    e.target.value as "bug" | "feature" | "issue" | null
                  )
                }
                className="w-full px-3 py-2 border rounded-md"
              >
                <option value="feature">Feature</option>
                <option value="bug">Bug</option>
                <option value="issue">Issue</option>
                <option value="">None</option>
              </select>
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Sprint</label>
              <input
                type="number"
                value={sprint === null ? "" : sprint}
                onChange={(e) =>
                  setSprint(e.target.value ? Number(e.target.value) : null)
                }
                className="w-full px-3 py-2 border rounded-md"
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Estimate (hours)</label>
              <input
                type="number"
                value={estimateHours === null ? "" : estimateHours}
                onChange={(e) =>
                  setEstimateHours(
                    e.target.value ? Number(e.target.value) : null
                  )
                }
                className="w-full px-3 py-2 border rounded-md"
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Real (hours)</label>
              <input
                type="number"
                value={realHours === null ? "" : realHours}
                onChange={(e) =>
                  setRealHours(e.target.value ? Number(e.target.value) : null)
                }
                className="w-full px-3 py-2 border rounded-md"
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Assigned To</label>
              <select
                value={assignedTo === null ? "" : assignedTo}
                onChange={(e) =>
                  setAssignedTo(e.target.value ? Number(e.target.value) : null)
                }
                className="w-full px-3 py-2 border rounded-md"
              >
                <option value="">Select Developer</option>
                {users?.map((dev) => (
                  <option key={dev.id} value={dev.id}>
                    {dev.firstName} {dev.lastName}
                  </option>
                ))}
              </select>
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Status</label>
              <select
                value={status}
                onChange={(e) =>
                  setStatus(
                    e.target.value as
                      | "created"
                      | "in-progress"
                      | "in-review"
                      | "testing"
                      | "done"
                  )
                }
                className="w-full px-3 py-2 border rounded-md"
              >
                <option value="created">Created</option>
                <option value="in-progress">In Progress</option>
                <option value="in-review">In Review</option>
                <option value="testing">Testing</option>
                <option value="done">Done</option>
              </select>
            </div>
            <div className="flex justify-end">
              <button
                type="submit"
                className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600"
                disabled={updateTaskMutation.isPending}
              >
                {updateTaskMutation.isPending ? "Updating..." : "Update Task"}
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  const FilterMenu = () => {
    const [tempCategoryFilter, setTempCategoryFilter] =
      useState(categoryFilter);
    const [tempStatusFilter, setTempStatusFilter] = useState(statusFilter);
    const [tempAssigneeFilter, setTempAssigneeFilter] = useState<number | null>(
      assigneeFilter
    );

    const applyFilters = () => {
      setCategoryFilter(tempCategoryFilter);
      setStatusFilter(tempStatusFilter);
      setAssigneeFilter(tempAssigneeFilter);
      setShowFilters(false);
    };

    const clearFilters = () => {
      setTempCategoryFilter("");
      setTempStatusFilter("");
      setTempAssigneeFilter(null);
    };

    return (
      <div className="absolute right-0 top-16 mt-2 w-64 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 z-10 p-4">
        <h3 className="text-sm font-medium text-gray-900 mb-3">Filters</h3>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm mb-1">Category</label>
          <select
            value={tempCategoryFilter}
            onChange={(e) => setTempCategoryFilter(e.target.value)}
            className="w-full px-3 py-2 border rounded-md text-sm"
          >
            <option value="">All Categories</option>
            <option value="feature">Feature</option>
            <option value="bug">Bug</option>
            <option value="issue">Issue</option>
          </select>
        </div>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm mb-1">Status</label>
          <select
            value={tempStatusFilter}
            onChange={(e) => setTempStatusFilter(e.target.value)}
            className="w-full px-3 py-2 border rounded-md text-sm"
          >
            <option value="">All Statuses</option>
            <option value="created">Created</option>
            <option value="in-progress">In Progress</option>
            <option value="in-review">In Review</option>
            <option value="testing">Testing</option>
            <option value="done">Done</option>
          </select>
        </div>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm mb-1">
            Assigned To
          </label>
          <select
            value={tempAssigneeFilter === null ? "" : tempAssigneeFilter}
            onChange={(e) =>
              setTempAssigneeFilter(
                e.target.value ? Number(e.target.value) : null
              )
            }
            className="w-full px-3 py-2 border rounded-md text-sm"
          >
            <option value="">All Developers</option>
            {users?.map((dev) => (
              <option key={dev.id} value={dev.id}>
                {dev.firstName} {dev.lastName}
              </option>
            ))}
          </select>
        </div>
        <div className="flex justify-between">
          <button
            onClick={clearFilters}
            className="text-gray-600 px-3 py-1 text-sm rounded-md hover:bg-gray-100"
          >
            Clear
          </button>
          <button
            onClick={applyFilters}
            className="bg-blue-500 text-white px-3 py-1 text-sm rounded-md hover:bg-blue-600"
          >
            Apply Filters
          </button>
        </div>
      </div>
    );
  };

  const TaskGroup = ({
    title,
    tasks,
    status,
  }: {
    title: string;
    tasks: TaskResponse[];
    status: TaskResponse["status"];
  }) => (
    <div className="flex-1 min-w-[300px]">
      <h2 className="text-lg font-semibold text-gray-700 mb-3">{title}</h2>
      <div className="space-y-3">
        {tasks
          .filter((task) => task.status === status)
          .map((task) => (
            <TaskCard key={task.id} task={task} />
          ))}
      </div>
    </div>
  );

  const TableView = ({ tasks }: { tasks: TaskResponse[] }) => {
    return (
      <div className="mt-4 overflow-x-auto">
        {tasks.length === 0 ? (
          <div className="text-center py-6 text-gray-500">
            No tasks match your filter criteria
          </div>
        ) : (
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Name
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Category
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Sprint
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Estimate / Real (hours)
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Assigned To
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Status
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {tasks.map((task) => (
                <tr key={task.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {task.description}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {task.category ? (
                      <span
                        className={`px-2 py-1 inline-flex items-center text-xs leading-5 font-semibold rounded-full 
                      ${
                        task.category === "bug"
                          ? "bg-red-100 text-red-800"
                          : task.category === "issue"
                          ? "bg-yellow-100 text-yellow-800"
                          : "bg-purple-100 text-purple-800"
                      }`}
                      >
                        {task.category === "bug" ? (
                          <Bug size={12} className="mr-1" />
                        ) : task.category === "issue" ? (
                          <Filter size={12} className="mr-1" />
                        ) : (
                          <Sparkles size={12} className="mr-1" />
                        )}
                        {task.category === "bug"
                          ? "Bug"
                          : task.category === "issue"
                          ? "Issue"
                          : "Feature"}
                      </span>
                    ) : (
                      <button
                        className="text-gray-400 hover:text-gray-600 flex items-center text-xs border border-dashed border-gray-300 rounded-full px-2 py-1 transition-colors"
                        title="Add category"
                      >
                        <Plus size={12} className="mr-1" />
                        <span>Add category</span>
                      </button>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {task.sprint ? (
                      task.sprint.name
                    ) : (
                      <button
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                        title="Assign to sprint"
                      >
                        <Plus size={16} />
                      </button>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {task.estimateHours ?? "-"} / {task.realHours ?? "-"}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {task.assignedTo ? (
                      <div className="flex items-center">
                        <img
                          src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${task.assignedTo}`}
                          alt={`${task.assignedTo.firstName} ${task.assignedTo.lastName}'s avatar`}
                          className="w-5 h-5 rounded-full mr-2"
                        />
                        <span>
                          {task.assignedTo.firstName} {task.assignedTo.lastName}
                        </span>
                      </div>
                    ) : (
                      <button
                        className="text-gray-400 hover:text-gray-600 flex items-center text-xs border border-dashed border-gray-300 rounded-full px-2 py-1 transition-colors"
                        title="Add category"
                      >
                        <Plus size={12} className="mr-1" />
                        <span>Add asignee</span>
                      </button>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span
                      className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        task.status === "created"
                          ? "bg-gray-100 text-gray-800"
                          : task.status === "in-progress"
                          ? "bg-yellow-100 text-yellow-800"
                          : task.status === "in-review"
                          ? "bg-blue-100 text-blue-800"
                          : task.status === "testing"
                          ? "bg-purple-100 text-purple-800"
                          : "bg-green-100 text-green-800"
                      }`}
                    >
                      {task.status === "created"
                        ? "Created"
                        : task.status === "in-progress"
                        ? "In Progress"
                        : task.status === "in-review"
                        ? "In Review"
                        : task.status === "testing"
                        ? "Testing"
                        : "Done"}
                    </span>
                  </td>
                  <td className="px-6 py-4 flex space-x-2 whitespace-nowrap text-sm text-gray-500">
                    <button
                      onClick={() => setTaskToEdit(task)}
                      className="text-blue-600 hover:text-blue-800 transition-colors"
                      title="Edit task"
                    >
                      <Pencil size={16} />
                    </button>
                    <DeleteTaskButton taskId={task.id} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    );
  };

  function DeleteTaskButton({ taskId }: { taskId: number }) {
    const deleteTaskMutation = useMutation({
      mutationFn: (taskId: number) => {
        return api.tasks.delete(taskId);
      },
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ["tasks"] });
      },
      onError: (error) => {
        console.error("Failed to delete task:", error);
        // Could add error handling UI here
      },
    });

    return (
      <button
        className="text-red-600 hover:text-red-800 ml-2"
        title="Delete task"
        onClick={() => deleteTaskMutation.mutate(taskId)}
      >
        {!deleteTaskMutation.isPending ? (
          <Trash2 size={16} />
        ) : (
          <Loader2 size={16} className="animate-spin" />
        )}
      </button>
    );
  }

  const KanbanView = ({ tasks }: { tasks: TaskResponse[] }) => (
    <div className="flex gap-6 overflow-x-auto pb-4">
      <TaskGroup title="Created" tasks={tasks} status="created" />
      <TaskGroup title="In Progress" tasks={tasks} status="in-progress" />
      <TaskGroup title="In Review" tasks={tasks} status="in-review" />
      <TaskGroup title="Testing" tasks={tasks} status="testing" />
      <TaskGroup title="Done" tasks={tasks} status="done" />
    </div>
  );

  // Add some visual indicator for active filters
  const hasActiveFilters =
    categoryFilter || statusFilter || assigneeFilter !== null;

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-semibold text-gray-800">Tasks</h1>
        <div className="flex items-center space-x-4">
          <div className="bg-gray-100 p-1 rounded-md flex">
            <button
              onClick={() => setViewMode("kanban")}
              className={`p-2 rounded-md flex items-center ${
                viewMode === "kanban" ? "bg-white shadow-sm" : ""
              }`}
              title="Kanban view"
            >
              <LayoutGrid size={18} />
            </button>
            <button
              onClick={() => setViewMode("table")}
              className={`p-2 rounded-md flex items-center ${
                viewMode === "table" ? "bg-white shadow-sm" : ""
              }`}
              title="Table view"
            >
              <List size={18} />
            </button>
          </div>
          <div className="relative">
            <button
              onClick={() => setShowFilters(!showFilters)}
              className={`px-4 py-2 rounded-md flex items-center space-x-2 ${
                hasActiveFilters
                  ? "bg-blue-100 text-blue-700 hover:bg-blue-200"
                  : "text-gray-600 hover:bg-gray-100"
              }`}
            >
              <Filter size={20} />
              <span>{hasActiveFilters ? "Filters Applied" : "Filter"}</span>
            </button>
            {showFilters && <FilterMenu />}
          </div>
          <button
            onClick={() => setShowAddModal(true)}
            className="bg-blue-500 text-white px-4 py-2 rounded-md flex items-center space-x-2 hover:bg-blue-600"
          >
            <Plus size={20} />
            <span>Add new task</span>
          </button>
        </div>
      </div>
      {tasks && (
        <>
          {viewMode === "kanban" ? (
            <KanbanView tasks={filteredTasks} />
          ) : (
            <TableView tasks={filteredTasks} />
          )}
        </>
      )}

      {showAddModal && <AddTaskModal onClose={() => setShowAddModal(false)} />}
      {taskToEdit && (
        <EditTaskModal task={taskToEdit} onClose={() => setTaskToEdit(null)} />
      )}
    </div>
  );
};

export default Tasks;
