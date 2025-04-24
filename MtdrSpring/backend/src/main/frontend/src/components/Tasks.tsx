import React, { useState, useMemo, useEffect } from "react";
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
  X,
  Search,
  Clock,
  Calendar,
  User,
  Tag,
  CalendarCheck,
  Download,
} from "lucide-react";
import { api, TaskRequest, TaskResponse } from "../utils/api/client";
import { useMutation, useQuery } from "@tanstack/react-query";
import { queryClient } from "../utils/query/query-client";
import { useSearchParams } from "react-router-dom";
import { generateTaskListPDF } from "../utils/reports/task-list-pdf-generator"; // Importa la nueva función

const Tasks: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [showAddModal, setShowAddModal] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [taskToEdit, setTaskToEdit] = useState<TaskResponse | null>(null);

  // Initialize search term from URL parameters
  const [searchTerm, setSearchTerm] = useState(
    () => searchParams.get("search") || "",
  );

  // Initialize state from URL parameters
  const [viewMode, setViewMode] = useState<"kanban" | "table">(() => {
    return (searchParams.get("view") as "kanban" | "table") || "table";
  });
  const [categoryFilter, setCategoryFilter] = useState<string>(
    () => searchParams.get("category") || "",
  );
  const [statusFilter, setStatusFilter] = useState<string>(
    () => searchParams.get("status") || "",
  );
  const [assigneeFilter, setAssigneeFilter] = useState<number | null>(() => {
    const assignee = searchParams.get("assignee");
    return assignee ? Number(assignee) : null;
  });
  const [sprintFilter, setSprintFilter] = useState<number | null>(() => {
    const sprint = searchParams.get("sprint");
    return sprint ? Number(sprint) : null;
  });

  // Update URL when filters, view mode, or search term change
  useEffect(() => {
    const params = new URLSearchParams(searchParams);

    // Update search term in URL
    if (searchTerm) {
      params.set("search", searchTerm);
    } else {
      params.delete("search");
    }

    // Update view mode in URL
    if (viewMode) {
      params.set("view", viewMode);
    } else {
      params.delete("view");
    }

    // Update filters in URL
    if (categoryFilter) {
      params.set("category", categoryFilter);
    } else {
      params.delete("category");
    }

    if (statusFilter) {
      params.set("status", statusFilter);
    } else {
      params.delete("status");
    }

    if (assigneeFilter !== null) {
      params.set("assignee", assigneeFilter.toString());
    } else {
      params.delete("assignee");
    }

    if (sprintFilter !== null) {
      params.set("sprint", sprintFilter.toString());
    } else {
      params.delete("sprint");
    }

    setSearchParams(params, { replace: true });
  }, [
    viewMode,
    categoryFilter,
    statusFilter,
    assigneeFilter,
    sprintFilter,
    setSearchParams,
    searchParams,
    searchTerm,
  ]);

  const { data: tasks, isLoading: tasksLoading } = useQuery({
    queryFn: api.tasks.list,
    queryKey: ["tasks"],
  });

  const { data: users } = useQuery({
    queryFn: api.users.getUsers,
    queryKey: ["users"],
  });

  const { data: sprints } = useQuery({
    queryFn: api.sprints.getSprints,
    queryKey: ["sprints"],
  });

  // Helper function to format date
  const formatDate = (date: Date | null | undefined) => {
    if (!date) return "No due date";
    return new Date(date).toLocaleDateString("en-MX", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  // Helper function to check if a due date is approaching or past
  const getDueDateStatus = (date: Date | null | undefined) => {
    if (!date) return "none";

    const dueDate = new Date(date);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const timeDiff = dueDate.getTime() - today.getTime();
    const daysDiff = timeDiff / (1000 * 3600 * 24);

    if (daysDiff < 0) return "overdue";
    if (daysDiff <= 3) return "soon";
    return "ok";
  };

  // Filter tasks using the defined filters and search term
  const filteredTasks = useMemo(() => {
    if (!tasks) return [];

    return tasks.filter((task) => {
      // Search term filter
      if (
        searchTerm &&
        !task.description.toLowerCase().includes(searchTerm.toLowerCase())
      ) {
        return false;
      }

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
        if (!task.assignedToId || task.assignedToId !== assigneeFilter) {
          return false;
        }
      }

      // Sprint filter
      if (sprintFilter !== null && task.sprintId !== sprintFilter) {
        return false;
      }

      return true;
    });
  }, [
    tasks,
    categoryFilter,
    statusFilter,
    assigneeFilter,
    sprintFilter,
    searchTerm,
  ]);

  // Get counts for each status
  const statusCounts = useMemo(() => {
    if (!filteredTasks) return {};

    return filteredTasks.reduce(
      (acc, task) => {
        acc[task.status] = (acc[task.status] || 0) + 1;
        return acc;
      },
      {} as Record<string, number>,
    );
  }, [filteredTasks]);

  const TaskCard = ({ task }: { task: TaskResponse }) => (
    <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition group">
      <div className="flex justify-between items-start mb-3">
        <h3 className="text-lg font-medium text-gray-900 line-clamp-2">
          {task.description}
        </h3>
        <span
          className={`
          px-2 py-1 rounded-full text-xs font-medium flex items-center gap-1 whitespace-nowrap
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
      <div className="space-y-2 text-sm">
        <div className="flex flex-wrap gap-2 mb-2">
          <span className="bg-blue-50 text-blue-600 px-2 py-0.5 rounded-full flex items-center gap-1">
            <Calendar size={12} />
            {sprints?.find((sprint) => sprint.id === task.sprintId)?.name ??
              "N/A"}
          </span>
          {task.estimateHours && (
            <span className="bg-gray-100 text-gray-700 px-2 py-0.5 rounded-full flex items-center gap-1">
              <Clock size={12} />
              {task.estimateHours}h est.
            </span>
          )}
          {task.dueDate && (
            <span
              className={`px-2 py-0.5 rounded-full flex items-center gap-1
                ${
                  getDueDateStatus(task.dueDate) === "overdue"
                    ? "bg-red-100 text-red-800"
                    : getDueDateStatus(task.dueDate) === "soon"
                      ? "bg-yellow-100 text-yellow-800"
                      : "bg-green-100 text-green-700"
                }
              `}
            >
              <CalendarCheck size={12} />
              {formatDate(task.dueDate)}
            </span>
          )}
        </div>
        <div className="text-gray-500 text-xs">
          Created:{" "}
          {task.createdAt.toLocaleDateString("en-MX", {
            year: "numeric",
            month: "short",
            day: "numeric",
          })}
        </div>
        {task.assignedToId && (
          <div className="flex items-center justify-between mt-3">
            <div className="flex items-center gap-2">
              <img
                src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${task.assignedToId}`}
                alt={`${task.assignedToId}'s avatar`}
                className="w-6 h-6 rounded-full border border-gray-200"
              />
              <span className="text-gray-700">
                {users?.find((user) => user.id === task.assignedToId)
                  ?.firstName || "Unknown"}
              </span>
            </div>
            <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex gap-1">
              <button
                onClick={(e) => {
                  e.preventDefault();
                  e.stopPropagation();
                  setTaskToEdit(task);
                }}
                className="p-1 rounded-md hover:bg-gray-100 text-gray-500 hover:text-blue-500"
              >
                <Pencil size={14} />
              </button>
              <DeleteTaskButton taskId={task.id} />
            </div>
          </div>
        )}
      </div>
    </div>
  );

  const AddTaskModal = ({ onClose }: { onClose: () => void }) => {
    const [description, setDescription] = useState("");
    const [category, setCategory] = useState<
      "bug" | "feature" | "issue" | null
    >("feature");
    const [sprint, setSprint] = useState<number | null>(null);
    const [assignedTo, setAssignedTo] = useState<number | null>(null);
    const [status, setStatus] = useState<
      "created" | "in-progress" | "in-review" | "testing" | "done"
    >("created");
    const [estimateHours, setEstimateHours] = useState<number | null>(null);
    const [realHours, setRealHours] = useState<number | null>(null);
    const [dueDate, setDueDate] = useState<string>("");

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
        dueDate: dueDate ? new Date(dueDate) : null,
      });
    };

    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden">
          <div className="flex justify-between items-center p-4 border-b border-gray-100">
            <h2 className="text-xl font-semibold text-gray-800">
              Add New Task
            </h2>
            <button
              onClick={onClose}
              className="w-8 h-8 rounded-full hover:bg-gray-100 flex items-center justify-center text-gray-500"
            >
              <X size={18} />
            </button>
          </div>
          <form onSubmit={handleSubmit} className="p-4">
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Task Description
                </label>
                <input
                  type="text"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Category
                  </label>
                  <select
                    value={category || ""}
                    onChange={(e) =>
                      setCategory(
                        e.target.value as "bug" | "feature" | "issue" | null,
                      )
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="feature">Feature</option>
                    <option value="bug">Bug</option>
                    <option value="issue">Issue</option>
                    <option value="">None</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Status
                  </label>
                  <select
                    value={status}
                    onChange={(e) =>
                      setStatus(
                        e.target.value as
                          | "created"
                          | "in-progress"
                          | "in-review"
                          | "testing"
                          | "done",
                      )
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="created">Created</option>
                    <option value="in-progress">In Progress</option>
                    <option value="in-review">In Review</option>
                    <option value="testing">Testing</option>
                    <option value="done">Done</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Due Date
                </label>
                <input
                  type="date"
                  value={dueDate}
                  onChange={(e) => setDueDate(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Sprint
                </label>
                <select
                  value={sprint === null ? "" : sprint}
                  onChange={(e) =>
                    setSprint(e.target.value ? Number(e.target.value) : null)
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select Sprint</option>
                  {sprints?.map((sprintOption) => (
                    <option key={sprintOption.id} value={sprintOption.id}>
                      {sprintOption.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Estimate (hours)
                  </label>
                  <input
                    type="number"
                    value={estimateHours === null ? "" : estimateHours}
                    onChange={(e) =>
                      setEstimateHours(
                        e.target.value ? Number(e.target.value) : null,
                      )
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Real (hours)
                  </label>
                  <input
                    type="number"
                    value={realHours === null ? "" : realHours}
                    onChange={(e) =>
                      setRealHours(
                        e.target.value ? Number(e.target.value) : null,
                      )
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Assigned To
                </label>
                <select
                  value={assignedTo === null ? "" : assignedTo}
                  onChange={(e) =>
                    setAssignedTo(
                      e.target.value ? Number(e.target.value) : null,
                    )
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select User</option>
                  {users?.map((dev) => (
                    <option key={dev.id} value={dev.id}>
                      {dev.firstName} {dev.lastName}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex justify-end mt-6">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-gray-700 mr-2 rounded-md hover:bg-gray-100"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md transition-colors flex items-center gap-2"
                disabled={createTaskMutation.isPending}
              >
                {createTaskMutation.isPending ? (
                  <>
                    <Loader2 size={16} className="animate-spin" />
                    Adding...
                  </>
                ) : (
                  <>
                    <Plus size={16} />
                    Add Task
                  </>
                )}
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
    const [sprint, setSprint] = useState<number | null>(task.sprintId || null);
    const [assignedTo, setAssignedTo] = useState<number | null>(
      task.assignedToId || null,
    );
    const [status, setStatus] = useState<
      "created" | "in-progress" | "in-review" | "testing" | "done"
    >(
      task.status as
        | "created"
        | "in-progress"
        | "in-review"
        | "testing"
        | "done",
    );
    const [estimateHours, setEstimateHours] = useState<number | null>(
      task.estimateHours,
    );
    const [realHours, setRealHours] = useState<number | null>(task.realHours);

    // Format date for input element (YYYY-MM-DD)
    const [dueDate, setDueDate] = useState<string>(
      task.dueDate ? new Date(task.dueDate).toISOString().split("T")[0] : "",
    );

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
        dueDate: dueDate ? new Date(dueDate) : null,
      });
    };

    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden">
          <div className="flex justify-between items-center p-4 border-b border-gray-100">
            <h2 className="text-xl font-semibold text-gray-800">Edit Task</h2>
            <button
              onClick={onClose}
              className="w-8 h-8 rounded-full hover:bg-gray-100 flex items-center justify-center text-gray-500"
            >
              <X size={18} />
            </button>
          </div>
          <form onSubmit={handleSubmit} className="p-4">
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Task Description
                </label>
                <input
                  type="text"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Category
                  </label>
                  <select
                    value={category || ""}
                    onChange={(e) =>
                      setCategory(
                        e.target.value as "bug" | "feature" | "issue" | null,
                      )
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="feature">Feature</option>
                    <option value="bug">Bug</option>
                    <option value="issue">Issue</option>
                    <option value="">None</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Status
                  </label>
                  <select
                    value={status}
                    onChange={(e) =>
                      setStatus(
                        e.target.value as
                          | "created"
                          | "in-progress"
                          | "in-review"
                          | "testing"
                          | "done",
                      )
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="created">Created</option>
                    <option value="in-progress">In Progress</option>
                    <option value="in-review">In Review</option>
                    <option value="testing">Testing</option>
                    <option value="done">Done</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Due Date
                </label>
                <input
                  type="date"
                  value={dueDate}
                  onChange={(e) => setDueDate(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Sprint
                </label>
                <select
                  value={sprint === null ? "" : sprint}
                  onChange={(e) =>
                    setSprint(e.target.value ? Number(e.target.value) : null)
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select Sprint</option>
                  {sprints?.map((sprintOption) => (
                    <option key={sprintOption.id} value={sprintOption.id}>
                      {sprintOption.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Estimate (hours)
                  </label>
                  <input
                    type="number"
                    value={estimateHours === null ? "" : estimateHours}
                    onChange={(e) =>
                      setEstimateHours(
                        e.target.value ? Number(e.target.value) : null,
                      )
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Real (hours)
                  </label>
                  <input
                    type="number"
                    value={realHours === null ? "" : realHours}
                    onChange={(e) =>
                      setRealHours(
                        e.target.value ? Number(e.target.value) : null,
                      )
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Assigned To
                </label>
                <select
                  value={assignedTo === null ? "" : assignedTo}
                  onChange={(e) =>
                    setAssignedTo(
                      e.target.value ? Number(e.target.value) : null,
                    )
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Select User</option>
                  {users?.map((dev) => (
                    <option key={dev.id} value={dev.id}>
                      {dev.firstName} {dev.lastName}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex justify-end mt-6">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-gray-700 mr-2 rounded-md hover:bg-gray-100"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md transition-colors flex items-center gap-2"
                disabled={updateTaskMutation.isPending}
              >
                {updateTaskMutation.isPending ? (
                  <>
                    <Loader2 size={16} className="animate-spin" />
                    Updating...
                  </>
                ) : (
                  <>
                    <Pencil size={16} />
                    Update Task
                  </>
                )}
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
      assigneeFilter,
    );
    const [tempSprintFilter, setTempSprintFilter] = useState<number | null>(
      sprintFilter,
    );

    const applyFilters = () => {
      setCategoryFilter(tempCategoryFilter);
      setStatusFilter(tempStatusFilter);
      setAssigneeFilter(tempAssigneeFilter);
      setSprintFilter(tempSprintFilter);
      setShowFilters(false);
    };

    const clearFilters = () => {
      setTempCategoryFilter("");
      setTempStatusFilter("");
      setTempAssigneeFilter(null);
      setTempSprintFilter(null);
    };

    return (
      <div className="absolute right-0 top-12 mt-2 w-72 rounded-lg shadow-lg bg-white border border-gray-100 overflow-hidden z-10">
        <div className="flex items-center justify-between p-3 border-b border-gray-100">
          <h3 className="text-sm font-medium text-gray-900">Filters</h3>
          <button
            onClick={() => setShowFilters(false)}
            className="text-gray-500 hover:text-gray-700"
          >
            <X size={16} />
          </button>
        </div>
        <div className="p-4 space-y-4">
          <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">
              Category
            </label>
            <select
              value={tempCategoryFilter}
              onChange={(e) => setTempCategoryFilter(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">All Categories</option>
              <option value="feature">Feature</option>
              <option value="bug">Bug</option>
              <option value="issue">Issue</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">
              Status
            </label>
            <select
              value={tempStatusFilter}
              onChange={(e) => setTempStatusFilter(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">All Statuses</option>
              <option value="created">Created</option>
              <option value="in-progress">In Progress</option>
              <option value="in-review">In Review</option>
              <option value="testing">Testing</option>
              <option value="done">Done</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">
              Assigned To
            </label>
            <select
              value={tempAssigneeFilter === null ? "" : tempAssigneeFilter}
              onChange={(e) =>
                setTempAssigneeFilter(
                  e.target.value ? Number(e.target.value) : null,
                )
              }
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">All Developers</option>
              {users?.map((dev) => (
                <option key={dev.id} value={dev.id}>
                  {dev.firstName} {dev.lastName}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">
              Sprint
            </label>
            <select
              value={tempSprintFilter === null ? "" : tempSprintFilter}
              onChange={(e) =>
                setTempSprintFilter(
                  e.target.value ? Number(e.target.value) : null,
                )
              }
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">All Sprints</option>
              {sprints?.map((sprintOption) => (
                <option key={sprintOption.id} value={sprintOption.id}>
                  {sprintOption.name}
                </option>
              ))}
            </select>
          </div>
          <div className="flex justify-between pt-2 border-t border-gray-100">
            <button
              onClick={clearFilters}
              className="text-gray-600 px-3 py-1.5 text-sm rounded-md hover:bg-gray-100 transition-colors"
            >
              Clear all
            </button>
            <button
              onClick={applyFilters}
              className="bg-blue-500 text-white px-3 py-1.5 text-sm rounded-md hover:bg-blue-600 transition-colors flex items-center gap-1"
            >
              <Filter size={14} />
              Apply Filters
            </button>
          </div>
        </div>
      </div>
    );
  };

  const TaskGroup = ({
    title,
    tasks,
    status,
    count,
  }: {
    title: string;
    tasks: TaskResponse[];
    status: TaskResponse["status"];
    count: number;
  }) => (
    <div className="flex-1 min-w-[300px] max-w-md">
      <div className="mb-3 flex items-center justify-between">
        <h2 className="text-lg font-semibold text-gray-700">{title}</h2>
        <span className="bg-gray-100 text-gray-700 px-2 py-0.5 text-xs font-medium rounded-full">
          {count}
        </span>
      </div>
      <div className="space-y-3 h-[calc(100vh-220px)] overflow-y-auto pr-2 pb-4">
        {tasks
          .filter((task) => task.status === status)
          .map((task) => (
            <TaskCard key={task.id} task={task} />
          ))}
        {tasks.filter((task) => task.status === status).length === 0 && (
          <div className="border border-dashed border-gray-300 rounded-lg p-4 text-center text-gray-500 h-32 flex items-center justify-center">
            No tasks in this status
          </div>
        )}
      </div>
    </div>
  );

  const TableView = ({ tasks }: { tasks: TaskResponse[] }) => {
    return (
      <div className="mt-4 overflow-x-auto rounded-lg border border-gray-200">
        {tasks.length === 0 ? (
          <div className="text-center py-12 text-gray-500 bg-white">
            No tasks match your filter criteria
          </div>
        ) : (
          <table className="min-w-full divide-y divide-gray-200 bg-white">
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
                  Due Date
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Estimate / Real
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
                  <td className="px-6 max-w-[40vw] md:max-w-[32rem] truncate py-4 whitespace-nowrap text-sm font-medium text-gray-900">
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
                        onClick={() => setTaskToEdit(task)}
                      >
                        <Plus size={12} className="mr-1" />
                        <span>Add category</span>
                      </button>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {task.sprintId ? (
                      <span className="bg-blue-50 text-blue-600 px-2 py-0.5 rounded-full text-xs">
                        {sprints?.find((sprint) => sprint.id === task.sprintId)
                          ?.name ?? "N/A"}
                      </span>
                    ) : (
                      <button
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                        title="Assign to sprint"
                        onClick={() => setTaskToEdit(task)}
                      >
                        <Plus size={16} />
                      </button>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    {task.dueDate ? (
                      <span
                        className={`px-2 py-1 inline-flex items-center text-xs leading-5 font-semibold rounded-full
                        ${
                          getDueDateStatus(task.dueDate) === "overdue"
                            ? "bg-red-100 text-red-800"
                            : getDueDateStatus(task.dueDate) === "soon"
                              ? "bg-yellow-100 text-yellow-800"
                              : "bg-green-100 text-green-700"
                        }`}
                      >
                        <CalendarCheck size={12} className="mr-1" />
                        {formatDate(task.dueDate)}
                      </span>
                    ) : (
                      <button
                        className="text-gray-400 hover:text-gray-600 flex items-center text-xs border border-dashed border-gray-300 rounded-full px-2 py-1 transition-colors"
                        title="Set due date"
                        onClick={() => setTaskToEdit(task)}
                      >
                        <CalendarCheck size={12} className="mr-1" />
                        <span>Set date</span>
                      </button>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <div className="flex items-center gap-1">
                      <Clock size={14} className="text-gray-400" />
                      <span className="font-medium text-gray-700">
                        {task.estimateHours ?? "-"}h
                      </span>
                      <span className="text-gray-400 mx-1">/</span>
                      <span
                        className={`font-medium ${
                          task.realHours &&
                          task.estimateHours &&
                          task.realHours > task.estimateHours
                            ? "text-red-600"
                            : "text-green-600"
                        }`}
                      >
                        {task.realHours ?? "-"}h
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {task.assignedToId ? (
                      <div className="flex items-center">
                        <img
                          src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${task.assignedToId}`}
                          alt={`${task.assignedToId}'s avatar`}
                          className="w-6 h-6 rounded-full mr-2 border border-gray-200"
                        />
                        <span>
                          {
                            users?.find((user) => user.id === task.assignedToId)
                              ?.firstName
                          }
                        </span>
                      </div>
                    ) : (
                      <button
                        className="text-gray-400 hover:text-gray-600 flex items-center gap-1 text-xs border border-dashed border-gray-300 rounded-full px-2 py-1 transition-colors"
                        title="Add assignee"
                        onClick={() => setTaskToEdit(task)}
                      >
                        <User size={12} />
                        <span>Assign</span>
                      </button>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span
                      className={`px-2 py-1 inline-flex items-center gap-1 text-xs leading-5 font-semibold rounded-full ${
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
                      className="text-blue-600 hover:text-blue-800 transition-colors p-1 rounded-md hover:bg-blue-50"
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
      },
    });

    return (
      <button
        className="text-red-600 hover:text-red-800 p-1 rounded-md hover:bg-red-50"
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
    <div className="flex gap-6 mt-6 overflow-x-auto pb-4 h-[calc(100vh-220px)]">
      <TaskGroup
        title="Created"
        tasks={tasks}
        status="created"
        count={statusCounts["created"] || 0}
      />
      <TaskGroup
        title="In Progress"
        tasks={tasks}
        status="in-progress"
        count={statusCounts["in-progress"] || 0}
      />
      <TaskGroup
        title="In Review"
        tasks={tasks}
        status="in-review"
        count={statusCounts["in-review"] || 0}
      />
      <TaskGroup
        title="Testing"
        tasks={tasks}
        status="testing"
        count={statusCounts["testing"] || 0}
      />
      <TaskGroup
        title="Done"
        tasks={tasks}
        status="done"
        count={statusCounts["done"] || 0}
      />
    </div>
  );

  // Add some visual indicator for active filters
  const hasActiveFilters =
    categoryFilter ||
    statusFilter ||
    assigneeFilter !== null ||
    sprintFilter !== null;

  const handleDownloadTaskListPDF = async () => {
    try {
      if (!tasks) {
        alert("No hay tareas disponibles para generar el reporte.");
        return;
      }

      const pdf = await generateTaskListPDF(tasks);
      pdf.save("task-list-report.pdf");
    } catch (error) {
      console.error("Error al generar el reporte:", error);
      alert("Ocurrió un error al generar el reporte.");
    }
  };

  return (
    <div className="p-4 sm:p-6 bg-gray-50 min-h-screen">
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 sm:p-6 mb-6">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
          <h1 className="text-xl sm:text-2xl font-bold text-gray-800 flex items-center gap-2">
            <Tag className="text-blue-500 h-5 w-5 sm:h-6 sm:w-6" />
            Tasks
            <span className="text-sm font-normal text-gray-500 ml-2">
              {filteredTasks.length}{" "}
              {filteredTasks.length === 1 ? "task" : "tasks"}
            </span>
          </h1>
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 w-full sm:w-auto">
            {/* Botón para descargar el reporte */}
            <button
              onClick={handleDownloadTaskListPDF}
              className="bg-blue-500 text-white px-3 py-2 rounded-md flex items-center justify-center gap-2 hover:bg-blue-600 transition-colors whitespace-nowrap"
            >
              <Download size={16} />
              <span className="hidden sm:inline">Download Task List</span>
              <span className="sm:hidden">Descargar</span>
            </button>
            {/* Otros botones existentes */}
            <div className="relative w-full sm:w-64">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Search className="h-4 w-4 text-gray-400" />
              </div>
              <input
                type="text"
                placeholder="Search tasks..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div className="flex items-center gap-2">
              <div className="bg-gray-100 p-1 rounded-md flex">
                <button
                  onClick={() => setViewMode("kanban")}
                  className={`p-2 rounded-md flex items-center transition-colors ${
                    viewMode === "kanban"
                      ? "bg-white shadow-sm text-blue-500"
                      : "text-gray-500 hover:text-gray-700"
                  }`}
                  title="Kanban view"
                >
                  <LayoutGrid size={18} />
                </button>
                <button
                  onClick={() => setViewMode("table")}
                  className={`p-2 rounded-md flex items-center transition-colors ${
                    viewMode === "table"
                      ? "bg-white shadow-sm text-blue-500"
                      : "text-gray-500 hover:text-gray-700"
                  }`}
                  title="Table view"
                >
                  <List size={18} />
                </button>
              </div>
              <div className="relative">
                <button
                  onClick={() => setShowFilters(!showFilters)}
                  className={`px-3 py-2 rounded-md flex items-center gap-1 transition-colors ${
                    hasActiveFilters
                      ? "bg-blue-50 text-blue-700 hover:bg-blue-100"
                      : "text-gray-600 hover:bg-gray-100"
                  }`}
                >
                  <Filter size={16} />
                  <span className="hidden sm:inline">
                    {hasActiveFilters ? "Filters" : "Filter"}
                  </span>
                  {hasActiveFilters && (
                    <span className="bg-blue-100 text-blue-800 w-5 h-5 flex items-center justify-center rounded-full text-xs">
                      {(categoryFilter ? 1 : 0) +
                        (statusFilter ? 1 : 0) +
                        (assigneeFilter !== null ? 1 : 0) +
                        (sprintFilter !== null ? 1 : 0)}
                    </span>
                  )}
                </button>
                {showFilters && <FilterMenu />}
              </div>
              <button
                onClick={() => setShowAddModal(true)}
                className="bg-blue-500 text-white px-3 py-2 rounded-md flex items-center justify-center gap-2 hover:bg-blue-600 transition-colors whitespace-nowrap"
              >
                <Plus size={16} />
                <span className="hidden sm:inline">Add Task</span>
                <span className="sm:hidden">Add</span>
              </button>
            </div>
          </div>
        </div>

        {/* Active filters display */}
        {hasActiveFilters && (
          <div className="flex flex-wrap items-center gap-2 mb-4 p-2 bg-gray-50 rounded-md">
            <span className="text-xs font-medium text-gray-500">
              Active filters:
            </span>
            {categoryFilter && (
              <span className="bg-blue-50 text-blue-700 px-2 py-0.5 rounded-md text-xs flex items-center gap-1">
                <Tag size={12} />
                Category: {categoryFilter}
                <button
                  onClick={() => setCategoryFilter("")}
                  className="ml-1 text-blue-500 hover:text-blue-700"
                >
                  <X size={12} />
                </button>
              </span>
            )}
            {statusFilter && (
              <span className="bg-blue-50 text-blue-700 px-2 py-0.5 rounded-md text-xs flex items-center gap-1">
                Status: {statusFilter}
                <button
                  onClick={() => setStatusFilter("")}
                  className="ml-1 text-blue-500 hover:text-blue-700"
                >
                  <X size={12} />
                </button>
              </span>
            )}
            {assigneeFilter !== null && (
              <span className="bg-blue-50 text-blue-700 px-2 py-0.5 rounded-md text-xs flex items-center gap-1">
                <User size={12} />
                Assignee:{" "}
                {users?.find((user) => user.id === assigneeFilter)?.firstName ||
                  "Unknown"}
                <button
                  onClick={() => setAssigneeFilter(null)}
                  className="ml-1 text-blue-500 hover:text-blue-700"
                >
                  <X size={12} />
                </button>
              </span>
            )}
            {sprintFilter !== null && (
              <span className="bg-blue-50 text-blue-700 px-2 py-0.5 rounded-md text-xs flex items-center gap-1">
                <Calendar size={12} />
                Sprint:{" "}
                {sprints?.find((sprint) => sprint.id === sprintFilter)?.name ||
                  "Unknown"}
                <button
                  onClick={() => setSprintFilter(null)}
                  className="ml-1 text-blue-500 hover:text-blue-700"
                >
                  <X size={12} />
                </button>
              </span>
            )}
            <button
              onClick={() => {
                setCategoryFilter("");
                setStatusFilter("");
                setAssigneeFilter(null);
                setSprintFilter(null);
              }}
              className="text-xs text-gray-600 hover:text-gray-800 underline ml-2"
            >
              Clear all
            </button>
          </div>
        )}

        {tasksLoading ? (
          <div className="flex items-center justify-center h-64">
            <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
          </div>
        ) : (
          <>
            {viewMode === "kanban" ? (
              <KanbanView tasks={filteredTasks} />
            ) : (
              <TableView tasks={filteredTasks} />
            )}
          </>
        )}
      </div>

      {showAddModal && <AddTaskModal onClose={() => setShowAddModal(false)} />}
      {taskToEdit && (
        <EditTaskModal task={taskToEdit} onClose={() => setTaskToEdit(null)} />
      )}
    </div>
  );
};

export default Tasks;
