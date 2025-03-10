import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, Calendar, ChevronRight, ListTodo } from "lucide-react";

interface Sprint {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
  status: "active" | "completed" | "planned";
  totalTasks: number;
  completedTasks: number;
}

const Sprints = () => {
  const navigate = useNavigate();
  const [showAddModal, setShowAddModal] = useState(false);

  const [sprints] = useState<Sprint[]>([
    {
      id: 1,
      name: "Sprint 1 - Initial Setup",
      startDate: "2024-03-01",
      endDate: "2024-03-15",
      status: "completed",
      totalTasks: 12,
      completedTasks: 12,
    },
    {
      id: 2,
      name: "Sprint 2 - Core Features",
      startDate: "2024-03-16",
      endDate: "2024-03-30",
      status: "active",
      totalTasks: 15,
      completedTasks: 8,
    },
    {
      id: 3,
      name: "Sprint 3 - UI Enhancement",
      startDate: "2024-03-31",
      endDate: "2024-04-14",
      status: "planned",
      totalTasks: 10,
      completedTasks: 0,
    },
  ]);

  const AddSprintModal = ({ onClose }: { onClose: () => void }) => {
    const [formData, setFormData] = useState({
      name: "",
      startDate: "",
      endDate: "",
    });

    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault();
      console.log("New sprint data:", formData);
      onClose();
    };

    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold text-gray-800">Add New Sprint</h2>
            <button
              onClick={onClose}
              className="text-gray-600 hover:text-gray-900"
            >
              ✕
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Sprint Name
              </label>
              <input
                type="text"
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={formData.name}
                onChange={(e) =>
                  setFormData({ ...formData, name: e.target.value })
                }
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Start Date
              </label>
              <input
                type="date"
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={formData.startDate}
                onChange={(e) =>
                  setFormData({ ...formData, startDate: e.target.value })
                }
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                End Date
              </label>
              <input
                type="date"
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={formData.endDate}
                onChange={(e) =>
                  setFormData({ ...formData, endDate: e.target.value })
                }
                required
              />
            </div>
            <div className="flex space-x-3 pt-4">
              <button
                type="submit"
                className="flex-1 bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600"
              >
                Add Sprint
              </button>
              <button
                type="button"
                onClick={onClose}
                className="flex-1 bg-gray-200 text-gray-800 py-2 rounded-md hover:bg-gray-300"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  const getStatusColor = (status: Sprint["status"]) => {
    switch (status) {
      case "active":
        return "bg-green-100 text-green-800";
      case "completed":
        return "bg-blue-100 text-blue-800";
      case "planned":
        return "bg-yellow-100 text-yellow-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  return (
    <div className="p-6 bg-gray-50">
      {showAddModal && (
        <AddSprintModal onClose={() => setShowAddModal(false)} />
      )}

      <div className="bg-white shadow-md rounded-lg">
        <div className="flex justify-between items-center p-6 border-b">
          <h1 className="text-2xl font-semibold text-gray-800">Sprints</h1>
          <button
            onClick={() => setShowAddModal(true)}
            className="bg-blue-500 text-white px-4 py-2 rounded-md flex items-center space-x-2 hover:bg-blue-600"
          >
            <Plus size={20} />
            <span>Add new sprint</span>
          </button>
        </div>

        <div className="p-6">
          <div className="space-y-4">
            {sprints.map((sprint) => (
              <div
                key={sprint.id}
                className="flex justify-between items-center bg-gray-50 p-4 rounded-lg shadow-sm hover:bg-gray-100 transition"
              >
                <div className="flex flex-col space-y-2">
                  <div className="flex items-center space-x-3">
                    <h3 className="text-lg font-semibold text-gray-800">
                      {sprint.name}
                    </h3>
                    <span
                      className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(
                        sprint.status
                      )}`}
                    >
                      {sprint.status.charAt(0).toUpperCase() +
                        sprint.status.slice(1)}
                    </span>
                  </div>
                  <div className="flex items-center space-x-2 text-sm text-gray-600">
                    <Calendar size={16} />
                    <span>
                      {formatDate(sprint.startDate)} -{" "}
                      {formatDate(sprint.endDate)}
                    </span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <div className="w-48 h-2 bg-gray-200 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-blue-500"
                        style={{
                          width: `${
                            (sprint.completedTasks / sprint.totalTasks) * 100
                          }%`,
                        }}
                      />
                    </div>
                    <span className="text-sm text-gray-600">
                      {sprint.completedTasks}/{sprint.totalTasks} tasks
                    </span>
                  </div>
                </div>
                <button
                  onClick={() => navigate(`/tasks?sprint=${sprint.id}`)}
                  className="text-blue-500 hover:text-blue-700 flex items-center space-x-2"
                >
                  <ListTodo size={18} />
                  <span>See Tasks</span>
                  <ChevronRight size={18} />
                </button>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Sprints;
