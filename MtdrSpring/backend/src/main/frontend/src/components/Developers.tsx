import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { User, Plus, ListTodo, ChevronRight, CheckCircle } from "lucide-react";
import { api, UserResponse } from "../utils/api/client";
import { useMutation, useQuery } from "@tanstack/react-query";
import { queryClient } from "../utils/query/query-client";

type DeveloperWithTasks = {
  id: number;
  name: string;
  role: string;
  profilePic: string;
  pendingTasks: number;
  email: string;
  originalData: UserResponse;
};

const Developers = () => {
  const navigate = useNavigate();
  const [selectedDeveloper, setSelectedDeveloper] =
    useState<DeveloperWithTasks | null>(null);
  const [showAddModal, setShowAddModal] = useState(false);

  // Fetch all users with React Query
  const { data: allUsers, isLoading } = useQuery({
    queryKey: ["users"],
    queryFn: api.users.getDevelopers,
  });

  // Get tasks to calculate pending tasks per developer
  const { data: tasks } = useQuery({
    queryKey: ["tasks"],
    queryFn: api.tasks.list,
  });

  // Separate developers from pending users
  const developers =
    allUsers?.filter((user) => user.role === "developer") || [];
  const pendingUsers =
    allUsers?.filter((user) => user.role === "user-pending-activation") || [];

  // Transform developers data to include pending tasks count
  const developersWithTasks: DeveloperWithTasks[] = developers.map((dev) => {
    const pendingTasksCount =
      tasks?.filter(
        (task) => task.assignedToId === dev.id && task.status !== "done"
      ).length || 0;

    return {
      id: dev.id,
      name: `${dev.firstName} ${dev.lastName}`,
      role: dev.title || "Developer",
      profilePic: `https://api.dicebear.com/7.x/avataaars/svg?seed=${dev.firstName}`,
      pendingTasks: pendingTasksCount,
      email: `${dev.firstName.toLowerCase()}.${dev.lastName.toLowerCase()}@company.com`,
      // Original user data is preserved for reference
      originalData: dev,
    };
  });

  // Mutation for updating user status (approving users)
  const updateUserStatusMutation = useMutation({
    mutationFn: ({
      userId,
      status,
    }: {
      userId: number;
      status: "developer" | "manager";
    }) => {
      return api.users.updateStatus(userId, status);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
    },
    onError: (error) => {
      console.error("Failed to update user status:", error);
    },
  });

  // Handle approving pending users
  const approveUser = (userId: number) => {
    updateUserStatusMutation.mutate({ userId, status: "developer" });
  };

  const AddDeveloperModal = ({ onClose }: { onClose: () => void }) => {
    const [formData, setFormData] = useState({
      firstName: "",
      lastName: "",
      title: "",
    });

    const createUserMutation = useMutation({
      mutationFn: (userData: {
        firstName: string;
        lastName: string;
        role: "developer" | "manager" | "user-pending-activation";
        title: string | null;
        telegramId: string | null;
      }) => {
        return api.users.patch(0, userData); // We're using patch as a workaround since createUser isn't available
      },
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ["users"] });
        onClose();
      },
      onError: (error) => {
        console.error("Failed to add developer:", error);
      },
    });

    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault();
      createUserMutation.mutate({
        firstName: formData.firstName,
        lastName: formData.lastName,
        role: "developer",
        title: formData.title || null,
        telegramId: null,
      });
    };

    return (
      <div className="fixed inset-0 bg-black/50 bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold text-gray-800">
              Add New Developer
            </h2>
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
                First Name
              </label>
              <input
                type="text"
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={formData.firstName}
                onChange={(e) =>
                  setFormData({ ...formData, firstName: e.target.value })
                }
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Last Name
              </label>
              <input
                type="text"
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={formData.lastName}
                onChange={(e) =>
                  setFormData({ ...formData, lastName: e.target.value })
                }
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Title/Role
              </label>
              <input
                type="text"
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={formData.title}
                onChange={(e) =>
                  setFormData({ ...formData, title: e.target.value })
                }
              />
            </div>
            <div className="flex space-x-3 pt-4">
              <button
                type="submit"
                className="flex-1 bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600"
                disabled={createUserMutation.isPending}
              >
                {createUserMutation.isPending ? "Adding..." : "Add Developer"}
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

  const ViewDeveloperDetails = ({
    developer,
    onClose,
  }: {
    developer: (typeof developersWithTasks)[0];
    onClose: () => void;
  }) => {
    return (
      <div className="fixed inset-0 bg-black/50 bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold text-gray-800">
              Developer Details
            </h2>
            <button
              onClick={onClose}
              className="text-gray-600 hover:text-gray-900"
            >
              ✕
            </button>
          </div>

          <div className="space-y-4">
            <div className="flex items-center space-x-4">
              <img
                src={developer.profilePic}
                alt={developer.name}
                className="w-16 h-16 rounded-full"
              />
              <div>
                <h3 className="text-xl font-semibold">{developer.name}</h3>
                <p className="text-gray-600">{developer.role}</p>
              </div>
            </div>

            <div className="space-y-2">
              <div>
                <p className="text-sm text-gray-500">Email</p>
                <p className="font-medium">{developer.email}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Tasks assigned</p>
                <p className="font-medium">
                  {developer.pendingTasks} pending task(s)
                </p>
              </div>
            </div>

            <div className="flex space-x-2">
              <button
                onClick={() => navigate(`/tasks?developer=${developer.id}`)}
                className="flex-1 bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600 flex items-center justify-center space-x-2"
              >
                <ListTodo size={18} />
                <span>View Tasks</span>
                {developer.pendingTasks > 0 && (
                  <span className="bg-white text-blue-500 rounded-full px-2 ml-2">
                    {developer.pendingTasks}
                  </span>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="p-6 bg-gray-50">
      {selectedDeveloper && (
        <ViewDeveloperDetails
          developer={selectedDeveloper}
          onClose={() => setSelectedDeveloper(null)}
        />
      )}
      {showAddModal && (
        <AddDeveloperModal onClose={() => setShowAddModal(false)} />
      )}

      <div className="bg-white shadow-md rounded-lg mb-6">
        <div className="flex justify-between items-center p-6 border-b">
          <h1 className="text-2xl font-semibold text-gray-800">Developers</h1>
          <button
            onClick={() => setShowAddModal(true)}
            className="bg-blue-500 text-white px-4 py-2 rounded-md flex items-center space-x-2 hover:bg-blue-600"
          >
            <Plus size={20} />
            <span>Add Developer</span>
          </button>
        </div>

        <div className="p-6">
          {isLoading ? (
            <div className="flex justify-center p-4">
              <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-blue-500 border-b-2"></div>
            </div>
          ) : (
            <div className="space-y-4">
              {developersWithTasks.length === 0 ? (
                <p className="text-center text-gray-500">No developers found</p>
              ) : (
                developersWithTasks.map((developer) => (
                  <div
                    key={developer.id}
                    className="flex justify-between items-center bg-gray-50 p-4 rounded-lg shadow-sm hover:bg-gray-100 transition"
                  >
                    <div className="flex items-center space-x-4">
                      <img
                        src={developer.profilePic}
                        alt={developer.name}
                        className="w-12 h-12 rounded-full"
                      />
                      <div>
                        <h3 className="text-lg font-semibold text-gray-800">
                          {developer.name}
                        </h3>
                        <div className="flex items-center space-x-2">
                          <p className="text-gray-600">{developer.role}</p>
                          {developer.pendingTasks > 0 && (
                            <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
                              {developer.pendingTasks} pending tasks
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center space-x-4">
                      <button
                        onClick={() => setSelectedDeveloper(developer)}
                        className="text-gray-600 hover:text-gray-800 flex items-center space-x-1"
                      >
                        <User size={18} />
                        <span>Details</span>
                      </button>
                      <button
                        onClick={() =>
                          navigate(`/tasks?developer=${developer.id}`)
                        }
                        className="text-blue-500 hover:text-blue-700 flex items-center space-x-1"
                      >
                        <ListTodo size={18} />
                        <span>Tasks</span>
                        <ChevronRight size={18} />
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      </div>

      {/* Pending Approval Users Section */}
      {pendingUsers && pendingUsers.length > 0 && (
        <div className="bg-white shadow-md rounded-lg">
          <div className="p-6 border-b">
            <h2 className="text-xl font-semibold text-gray-800">
              Pending Approval ({pendingUsers.length})
            </h2>
          </div>
          <div className="p-6">
            <div className="space-y-4">
              {pendingUsers.map((user) => (
                <div
                  key={user.id}
                  className="flex justify-between items-center bg-yellow-50 p-4 rounded-lg shadow-sm"
                >
                  <div className="flex items-center space-x-4">
                    <img
                      src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${user.firstName}`}
                      alt={`${user.firstName} ${user.lastName}`}
                      className="w-12 h-12 rounded-full"
                    />
                    <div>
                      <h3 className="text-lg font-semibold text-gray-800">
                        {user.firstName} {user.lastName}
                      </h3>
                      <div className="flex items-center space-x-2">
                        <p className="text-gray-600">
                          {user.title || "Developer"}
                        </p>
                        <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                          Pending Approval
                        </span>
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={() => approveUser(user.id)}
                    disabled={updateUserStatusMutation.isPending}
                    className="bg-green-500 text-white px-3 py-1.5 rounded-md flex items-center space-x-1 hover:bg-green-600 disabled:bg-green-300"
                  >
                    <CheckCircle size={16} />
                    <span>
                      {updateUserStatusMutation.isPending &&
                      user.id === updateUserStatusMutation.variables?.userId
                        ? "Approving..."
                        : "Approve"}
                    </span>
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Developers;
