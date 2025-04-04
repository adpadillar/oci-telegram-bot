import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, Calendar, ChevronRight, ListTodo, Loader2 } from "lucide-react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../utils/api/client";

// Removed unused Sprint interface

const Sprints = () => {
  const navigate = useNavigate();
  const [showAddModal, setShowAddModal] = useState(false);

  const { data: sprints, isLoading, error } = useQuery({
    queryKey: ["sprints"],
    queryFn: api.sprints.getSprints,
  });

  const AddSprintModal = ({ onClose }: { onClose: () => void }) => {
    const queryClient = useQueryClient();
    const [formData, setFormData] = useState({
      name: "",
      description: "",
      startDate: "",
      endDate: "",
    });

    const createSprintMutation = useMutation({
      mutationFn: () => {
        return api.sprints.create({
          name: formData.name,
          description: formData.description,
          startedAt: new Date(formData.startDate).toISOString(),
          endsAt: new Date(formData.endDate).toISOString(),
        });
      },
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ["sprints"] });
        onClose();
      },
      onError: (error) => {
        console.error("Failed to create sprint:", error);
        // Could add error toast/notification here
      },
    });

    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault();
      createSprintMutation.mutate();
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
                Description
              </label>
              <textarea
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={formData.description}
                onChange={(e) =>
                  setFormData({ ...formData, description: e.target.value })
                }
                rows={3}
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
                disabled={createSprintMutation.isPending}
              >
                {createSprintMutation.isPending ? "Creating..." : "Add Sprint"}
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

  const formatDate = (date: Date) => {
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <Loader2 className="w-8 h-8 animate-spin" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex justify-center items-center h-screen text-red-600">
        Failed to load sprints
      </div>
    );
  }

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
            {sprints?.map((sprint) => (
              <div
                key={sprint.id}
                className="flex justify-between items-center bg-gray-50 p-4 rounded-lg shadow-sm hover:bg-gray-100 transition"
              >
                <div className="flex flex-col space-y-2">
                  <div className="flex items-center space-x-3">
                    <h3 className="text-lg font-semibold text-gray-800">
                      {sprint.name}
                    </h3>
                  </div>
                  {sprint.description && (
                    <p className="text-sm text-gray-600">{sprint.description}</p>
                  )}
                  <div className="flex items-center space-x-2 text-sm text-gray-600">
                    <Calendar size={16} />
                    <span>
                      {formatDate(sprint.startedAt)} - {formatDate(sprint.endsAt)}
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
