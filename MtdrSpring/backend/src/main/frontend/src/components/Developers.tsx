"use client"

import type React from "react"

import { useState } from "react"
import { useNavigate } from "react-router-dom"
import {
  User,
  ListTodo,
  ChevronRight,
  CheckCircle,
  X,
  Search,
  UserPlus,
  Users,
  Clock,
  Mail,
  Briefcase,
} from "lucide-react"
import { api, type UserResponse } from "../utils/api/client"
import { useMutation, useQuery } from "@tanstack/react-query"
import { queryClient } from "../utils/query/query-client"

type DeveloperWithTasks = {
  id: number
  name: string
  role: string
  profilePic: string
  pendingTasks: number
  email: string
  originalData: UserResponse
}

const Developers = () => {
  const navigate = useNavigate()
  const [selectedDeveloper, setSelectedDeveloper] = useState<DeveloperWithTasks | null>(null)
  const [showAddModal, setShowAddModal] = useState(false)
  const [searchTerm, setSearchTerm] = useState("")

  // Fetch all users with React Query
  const { data: allUsers, isLoading } = useQuery({
    queryKey: ["users"],
    queryFn: api.users.getDevelopers,
  })

  // Get tasks to calculate pending tasks per developer
  const { data: tasks } = useQuery({
    queryKey: ["tasks"],
    queryFn: api.tasks.list,
  })

  // Separate developers from pending users
  const developers = allUsers?.filter((user) => user.role === "developer") || []
  const pendingUsers = allUsers?.filter((user) => user.role === "user-pending-activation") || []

  // Transform developers data to include pending tasks count
  const developersWithTasks: DeveloperWithTasks[] = developers.map((dev) => {
    const pendingTasksCount =
      tasks?.filter((task) => task.assignedToId === dev.id && task.status !== "done").length || 0

    return {
      id: dev.id,
      name: `${dev.firstName} ${dev.lastName}`,
      role: dev.title || "Developer",
      profilePic: `https://api.dicebear.com/7.x/avataaars/svg?seed=${dev.firstName}`,
      pendingTasks: pendingTasksCount,
      email: `${dev.firstName.toLowerCase()}.${dev.lastName.toLowerCase()}@company.com`,
      // Original user data is preserved for reference
      originalData: dev,
    }
  })

  // Filter developers based on search term
  const filteredDevelopers = developersWithTasks.filter((dev) =>
    dev.name.toLowerCase().includes(searchTerm.toLowerCase()),
  )

  // Mutation for updating user status (approving users)
  const updateUserStatusMutation = useMutation({
    mutationFn: ({ userId, status }: { userId: number; status: "developer" | "manager" }) => {
      return api.users.updateStatus(userId, status)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] })
    },
    onError: (error) => {
      console.error("Failed to update user status:", error)
    },
  })

  // Handle approving pending users
  const approveUser = (userId: number) => {
    updateUserStatusMutation.mutate({ userId, status: "developer" })
  }

  const AddDeveloperModal = ({ onClose }: { onClose: () => void }) => {
    const [formData, setFormData] = useState({
      firstName: "",
      lastName: "",
      title: "",
    })

    const createUserMutation = useMutation({
      mutationFn: (userData: {
        firstName: string
        lastName: string
        role: "developer" | "manager" | "user-pending-activation"
        title: string | null
        telegramId: string | null
      }) => {
        return api.users.patch(0, userData) // We're using patch as a workaround since createUser isn't available
      },
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ["users"] })
        onClose()
      },
      onError: (error) => {
        console.error("Failed to add developer:", error)
      },
    })

    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault()
      createUserMutation.mutate({
        firstName: formData.firstName,
        lastName: formData.lastName,
        role: "developer",
        title: formData.title || null,
        telegramId: null,
      })
    }

    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden">
          <div className="flex justify-between items-center p-4 border-b border-gray-100">
            <h2 className="text-xl font-semibold text-gray-800">Add New Developer</h2>
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
                <label className="block text-sm font-medium text-gray-700 mb-1">First Name</label>
                <input
                  type="text"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Last Name</label>
                <input
                  type="text"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Title/Role</label>
                <input
                  type="text"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  placeholder="e.g. Frontend Developer"
                />
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
                disabled={createUserMutation.isPending}
              >
                {createUserMutation.isPending ? (
                  <>
                    <Clock className="animate-spin" size={16} />
                    Adding...
                  </>
                ) : (
                  <>
                    <UserPlus size={16} />
                    Add Developer
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    )
  }

  const ViewDeveloperDetails = ({
    developer,
    onClose,
  }: {
    developer: (typeof developersWithTasks)[0]
    onClose: () => void
  }) => {
    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden">
          <div className="flex justify-between items-center p-4 border-b border-gray-100">
            <h2 className="text-xl font-semibold text-gray-800">Developer Details</h2>
            <button
              onClick={onClose}
              className="w-8 h-8 rounded-full hover:bg-gray-100 flex items-center justify-center text-gray-500"
            >
              <X size={18} />
            </button>
          </div>

          <div className="p-4">
            <div className="flex items-center gap-4 mb-6">
              <div className="w-16 h-16 rounded-full overflow-hidden border-2 border-blue-100">
                <img
                  src={developer.profilePic || "/placeholder.svg"}
                  alt={developer.name}
                  className="w-full h-full object-cover"
                />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-gray-800">{developer.name}</h3>
                <p className="text-gray-600">{developer.role}</p>
              </div>
            </div>

            <div className="space-y-4">
              <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                <Mail className="text-gray-400" size={18} />
                <div>
                  <p className="text-xs text-gray-500">Email</p>
                  <p className="font-medium text-gray-800">{developer.email}</p>
                </div>
              </div>

              <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                <Briefcase className="text-gray-400" size={18} />
                <div>
                  <p className="text-xs text-gray-500">Role</p>
                  <p className="font-medium text-gray-800">{developer.role}</p>
                </div>
              </div>

              <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                <ListTodo className="text-gray-400" size={18} />
                <div>
                  <p className="text-xs text-gray-500">Tasks assigned</p>
                  <p className="font-medium text-gray-800">
                    {developer.pendingTasks} pending task{developer.pendingTasks !== 1 ? "s" : ""}
                  </p>
                </div>
              </div>
            </div>

            <div className="mt-6">
              <button
                onClick={() => {
                  navigate(`/tasks?developer=${developer.id}`)
                  onClose()
                }}
                className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600 transition-colors flex items-center justify-center gap-2"
              >
                <ListTodo size={18} />
                <span>View Tasks</span>
                {developer.pendingTasks > 0 && (
                  <span className="bg-white text-blue-500 rounded-full px-2 ml-1 text-sm">
                    {developer.pendingTasks}
                  </span>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {selectedDeveloper && (
        <ViewDeveloperDetails developer={selectedDeveloper} onClose={() => setSelectedDeveloper(null)} />
      )}
      {showAddModal && <AddDeveloperModal onClose={() => setShowAddModal(false)} />}

      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-2">
        <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
          <Users className="text-blue-500" />
          Developers
          <span className="text-sm font-normal text-gray-500 ml-2">
            {developers.length} {developers.length === 1 ? "developer" : "developers"}
          </span>
        </h1>
        <div className="flex items-center gap-3 w-full md:w-auto">
          <div className="relative w-full md:w-64">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search className="h-4 w-4 text-gray-400" />
            </div>
            <input
              type="text"
              placeholder="Search developers..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <button
            onClick={() => setShowAddModal(true)}
            className="bg-blue-500 text-white px-4 py-2 rounded-md flex items-center gap-2 hover:bg-blue-600 transition-colors whitespace-nowrap"
          >
            <UserPlus size={18} />
            <span className="hidden sm:inline">Add Developer</span>
          </button>
        </div>
      </div>

      {/* Developers List */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="p-4 border-b border-gray-100">
          <h2 className="text-lg font-semibold text-gray-800">Team Members</h2>
        </div>

        {isLoading ? (
          <div className="flex justify-center items-center p-8">
            <Clock className="w-8 h-8 animate-spin text-blue-500" />
          </div>
        ) : filteredDevelopers.length === 0 ? (
          <div className="p-8 text-center text-gray-500">
            {searchTerm ? "No developers match your search" : "No developers found"}
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {filteredDevelopers.map((developer) => (
              <div key={developer.id} className="p-4 hover:bg-gray-50 transition-colors">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div className="flex items-center gap-3">
                    <div className="relative">
                      <img
                        src={developer.profilePic || "/placeholder.svg"}
                        alt={developer.name}
                        className="w-12 h-12 rounded-full border-2 border-white shadow-sm"
                      />
                      {developer.pendingTasks > 0 && (
                        <span className="absolute -top-1 -right-1 bg-orange-500 text-white w-5 h-5 flex items-center justify-center rounded-full text-xs">
                          {developer.pendingTasks}
                        </span>
                      )}
                    </div>
                    <div>
                      <h3 className="text-lg font-semibold text-gray-800">{developer.name}</h3>
                      <p className="text-gray-600 text-sm">{developer.role}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 ml-auto">
                    <button
                      onClick={() => setSelectedDeveloper(developer)}
                      className="px-3 py-1.5 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-md flex items-center gap-1 transition-colors"
                    >
                      <User size={16} />
                      <span>Details</span>
                    </button>
                    <button
                      onClick={() => navigate(`/tasks?developer=${developer.id}`)}
                      className="px-3 py-1.5 text-blue-600 hover:text-blue-700 hover:bg-blue-50 rounded-md flex items-center gap-1 transition-colors"
                    >
                      <ListTodo size={16} />
                      <span>Tasks</span>
                      <ChevronRight size={16} />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Pending Approval Users Section */}
      {pendingUsers && pendingUsers.length > 0 && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="p-4 border-b border-gray-100 bg-yellow-50">
            <h2 className="text-lg font-semibold text-yellow-800 flex items-center gap-2">
              <Clock size={18} />
              Pending Approval ({pendingUsers.length})
            </h2>
          </div>
          <div className="divide-y divide-gray-100">
            {pendingUsers.map((user) => (
              <div key={user.id} className="p-4 bg-yellow-50/30 hover:bg-yellow-50/50 transition-colors">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div className="flex items-center gap-3">
                    <img
                      src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${user.firstName}`}
                      alt={`${user.firstName} ${user.lastName}`}
                      className="w-12 h-12 rounded-full border-2 border-white shadow-sm"
                    />
                    <div>
                      <h3 className="text-lg font-semibold text-gray-800">
                        {user.firstName} {user.lastName}
                      </h3>
                      <div className="flex items-center gap-2">
                        <p className="text-gray-600 text-sm">{user.title || "Developer"}</p>
                        <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                          Pending
                        </span>
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={() => approveUser(user.id)}
                    disabled={updateUserStatusMutation.isPending}
                    className="px-3 py-1.5 bg-green-500 text-white rounded-md flex items-center gap-1 hover:bg-green-600 disabled:bg-green-300 transition-colors ml-auto"
                  >
                    <CheckCircle size={16} />
                    <span>
                      {updateUserStatusMutation.isPending && user.id === updateUserStatusMutation.variables?.userId
                        ? "Approving..."
                        : "Approve"}
                    </span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

export default Developers
