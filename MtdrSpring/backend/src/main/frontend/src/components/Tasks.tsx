import React, { useState } from "react";
import { Plus, Filter, Bug, Sparkles, LayoutGrid, List } from "lucide-react";
import { useTasks } from "../hooks/use-tasks";
import { Task } from "../utils/api/client";

interface Developer {
  id: number;
  name: string;
  role: string;
  profilePic: string;
  pendingTasks: number;
  email: string;
  skills: string[];
  yearsOfExperience: number;
}

const Tasks: React.FC = () => {
  const [showAddModal, setShowAddModal] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [viewMode, setViewMode] = useState<"kanban" | "table">("kanban");

  const { data: tasks } = useTasks();

  // const [tasks] = useState<Task[]>([
  //   {
  //     id: "1",
  //     name: "Fix login authentication bug",
  //     category: "bug",
  //     sprint: 1,
  //     dateCreated: "2024-03-01",
  //     assignedTo: "John Smith",
  //     status: "in_progress",
  //   },
  //   {
  //     id: "2",
  //     name: "Implement dark mode feature",
  //     category: "feature",
  //     sprint: 1,
  //     dateCreated: "2024-03-02",
  //     assignedTo: "Sarah Johnson",
  //     status: "ready",
  //   },
  //   {
  //     id: "3",
  //     name: "Add export to PDF functionality",
  //     category: "feature",
  //     sprint: 2,
  //     dateCreated: "2024-03-03",
  //     assignedTo: "Mike Chen",
  //     status: "done",
  //   },
  // ]);

  const [developers] = useState<Developer[]>([
    {
      id: 1,
      name: "John Smith",
      role: "Frontend Developer",
      profilePic: "https://api.dicebear.com/7.x/avataaars/svg?seed=John",
      pendingTasks: 5,
      email: "john.smith@company.com",
      skills: ["React", "TypeScript", "Tailwind"],
      yearsOfExperience: 3,
    },
    {
      id: 2,
      name: "Sarah Johnson",
      role: "Backend Developer",
      profilePic: "https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah",
      pendingTasks: 3,
      email: "sarah.j@company.com",
      skills: ["Node.js", "Python", "MongoDB"],
      yearsOfExperience: 5,
    },
    {
      id: 3,
      name: "Mike Chen",
      role: "Full Stack Developer",
      profilePic: "https://api.dicebear.com/7.x/avataaars/svg?seed=Mike",
      pendingTasks: 7,
      email: "mike.chen@company.com",
      skills: ["React", "Node.js", "PostgreSQL"],
      yearsOfExperience: 4,
    },
  ]);

  const TaskCard = ({ task }: { task: Task }) => (
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
              : "bg-purple-100 text-purple-800"
          }
        `}
        >
          {task.category === "bug" ? <Bug size={12} /> : <Sparkles size={12} />}
          {task.category === "bug" ? "Bug" : "Feature"}
        </span>
      </div>
      <div className="flex flex-wrap gap-2 text-sm text-gray-600">
        <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded-full">
          Sprint #{task.sprint?.toString() ?? "N/A"}
        </span>
        <span>Created: {task.createdAt.toISOString()}</span>
        <span className="flex items-center gap-1">
          <img
            src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${task.assignedTo}`}
            // alt={task.assignedTo}
            className="w-4 h-4 rounded-full"
          />
          {/* {task.assignedTo} */}
          Assigned to
        </span>
      </div>
    </div>
  );

  const AddTaskModal = ({ onClose }: { onClose: () => void }) => {
    const [name, setName] = useState("");
    const [category, setCategory] = useState<"bug" | "feature">("feature");
    const [sprint, setSprint] = useState(1);
    const [assignedTo, setAssignedTo] = useState("");
    const [status, setStatus] = useState<"in_progress" | "ready" | "done">("in_progress");

    const handleAddTask = () => {
      const newTask: Task = {
        id: (tasks.length + 1).toString(),
        name,
        category,
        sprint,
        dateCreated: new Date().toISOString().split("T")[0],
        assignedTo,
        status,
      };
      setTasks([...tasks, newTask]);
      onClose();
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
          <form onSubmit={(e) => { e.preventDefault(); handleAddTask(); }}>
            <div className="mb-4">
              <label className="block text-gray-700">Task Name</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-3 py-2 border rounded-md"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Category</label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value as "bug" | "feature")}
                className="w-full px-3 py-2 border rounded-md"
              >
                <option value="feature">Feature</option>
                <option value="bug">Bug</option>
              </select>
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Sprint</label>
              <input
                type="number"
                value={sprint}
                onChange={(e) => setSprint(Number(e.target.value))}
                className="w-full px-3 py-2 border rounded-md"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Assigned To</label>
              <select
                value={assignedTo}
                onChange={(e) => setAssignedTo(e.target.value)}
                className="w-full px-3 py-2 border rounded-md"
                required
              >
                <option value="">Select Developer</option>
                {developers.map((dev) => (
                  <option key={dev.id} value={dev.name}>
                    {dev.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="mb-4">
              <label className="block text-gray-700">Status</label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value as "in_progress" | "ready" | "done")}
                className="w-full px-3 py-2 border rounded-md"
              >
                <option value="in_progress">In Progress</option>
                <option value="ready">Ready</option>
                <option value="done">Done</option>
              </select>
            </div>
            <div className="flex justify-end">
              <button
                type="submit"
                className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600"
              >
                Add Task
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  const FilterMenu = () => {
    const [categoryFilter, setCategoryFilter] = useState("");
    const [statusFilter, setStatusFilter] = useState("");

    const applyFilters = () => {
      // Implementar la lógica para aplicar los filtros
    };

    return (
      <div className="absolute right-0 top-16 mt-2 w-64 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 z-10 p-4">
        <h3 className="text-sm font-medium text-gray-900 mb-3">Filters</h3>
        <div className="mb-4">
          <label className="block text-gray-700">Category</label>
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="w-full px-3 py-2 border rounded-md"
          >
            <option value="">All</option>
            <option value="feature">Feature</option>
            <option value="bug">Bug</option>
          </select>
        </div>
        <div className="mb-4">
          <label className="block text-gray-700">Status</label>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="w-full px-3 py-2 border rounded-md"
          >
            <option value="">All</option>
            <option value="in_progress">In Progress</option>
            <option value="ready">Ready</option>
            <option value="done">Done</option>
          </select>
        </div>
        <div className="flex justify-end">
          <button
            onClick={applyFilters}
            className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600"
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
    tasks: Task[];
    status: Task["status"];
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

  const TableView = ({ tasks }: { tasks: Task[] }) => (
    <div className="mt-4 overflow-x-auto">
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
              Date Created
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
                    className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full 
                  ${
                    task.category === "bug"
                      ? "bg-red-100 text-red-800"
                      : "bg-purple-100 text-purple-800"
                  }`}
                  >
                    {task.category === "bug" ? (
                      <Bug size={12} className="mr-1" />
                    ) : (
                      <Sparkles size={12} className="mr-1" />
                    )}
                    {task.category === "bug" ? "Bug" : "Feature"}
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
                {new Date(task.createdAt).toLocaleDateString("en-MX", {
                  year: "numeric",
                  month: "short",
                  day: "numeric",
                })}
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
                    task.status === "in_progress"
                      ? "bg-yellow-100 text-yellow-800"
                      : task.status === "ready"
                      ? "bg-blue-100 text-blue-800"
                      : "bg-green-100 text-green-800"
                  }`}
                >
                  {task.status === "in_progress"
                    ? "In Progress"
                    : task.status === "ready"
                    ? "Ready"
                    : "Done"}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );

  const KanbanView = ({ tasks }: { tasks: Task[] }) => (
    <div className="flex gap-6 overflow-x-auto pb-4">
      <TaskGroup title="In Progress" tasks={tasks} status="in_progress" />
      <TaskGroup title="Ready" tasks={tasks} status="ready" />
      <TaskGroup title="Done" tasks={tasks} status="done" />
    </div>
  );

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
              className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-md flex items-center space-x-2"
            >
              <Filter size={20} />
              <span>Filter</span>
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
            <KanbanView tasks={tasks} />
          ) : (
            <TableView tasks={tasks} />
          )}
        </>
      )}

      {showAddModal && <AddTaskModal onClose={() => setShowAddModal(false)} />}
    </div>
  );
};

export default Tasks;
