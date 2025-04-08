import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { Plus, Calendar, ChevronRight, ListTodo, Loader2, X, Clock, CalendarDays } from 'lucide-react'
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { api } from "../utils/api/client"

const Sprints = () => {
  const navigate = useNavigate()
  const [showAddModal, setShowAddModal] = useState(false)

  const { data: sprints, isLoading, error } = useQuery({
    queryKey: ["sprints"],
    queryFn: api.sprints.getSprints,
  })

  const { data: tasks } = useQuery({
    queryKey: ["tasks"],
    queryFn: api.tasks.list,
  })

  // Calculate tasks per sprint
  const getSprintTaskCount = (sprintId: number) => {
    if (!tasks) return 0
    return tasks.filter((task) => task.sprintId === sprintId).length
  }

  // Check if sprint is active
  const isSprintActive = (sprint: any) => {
    const now = new Date()
    return new Date(sprint.startedAt) <= now && new Date(sprint.endsAt) >= now
  }

  const AddSprintModal = ({ onClose }: { onClose: () => void }) => {
    const queryClient = useQueryClient()
    const [formData, setFormData] = useState({
      name: "",
      description: "",
      startDate: "",
      endDate: "",
    })

    const createSprintMutation = useMutation({
      mutationFn: () => {
        return api.sprints.create({
          name: formData.name,
          description: formData.description,
          startedAt: new Date(formData.startDate).toISOString(),
          endsAt: new Date(formData.endDate).toISOString(),
        })
      },
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ["sprints"] })
        onClose()
      },
      onError: (error) => {
        console.error("Failed to create sprint:", error)
      },
    })

    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault()
      createSprintMutation.mutate()
    }

    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden">
          <div className="flex justify-between items-center p-4 border-b border-gray-100">
            <h2 className="text-xl font-semibold text-gray-800">Add New Sprint</h2>
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
                <label className="block text-sm font-medium text-gray-700 mb-1">Sprint Name</label>
                <input
                  type="text"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                <textarea
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  rows={3}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Start Date</label>
                  <div className="relative">
                    <input
                      type="date"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent pl-9"
                      value={formData.startDate}
                      onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                      required
                    />
                    <Calendar className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">End Date</label>
                  <div className="relative">
                    <input
                      type="date"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent pl-9"
                      value={formData.endDate}
                      onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                      required
                    />
                    <Calendar className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
                  </div>
                </div>
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
                disabled={createSprintMutation.isPending}
              >
                {createSprintMutation.isPending ? (
                  <>
                    <Loader2 size={16} className="animate-spin" />
                    Creating...
                  </>
                ) : (
                  <>
                    <Plus size={16} />
                    Add Sprint
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    )
  }

  const formatDate = (date: Date) => {
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    })
  }

  // Calculate days remaining for a sprint
  const getDaysRemaining = (endDate: Date) => {
    const now = new Date()
    const end = new Date(endDate)
    const diffTime = end.getTime() - now.getTime()
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    return diffDays
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex justify-center items-center h-64 text-red-600">
        <p>Failed to load sprints</p>
      </div>
    )
  }

  // Group sprints by status
  const activeSprints = sprints?.filter((sprint) => isSprintActive(sprint)) || []
  const upcomingSprints = sprints?.filter((sprint) => new Date(sprint.startedAt) > new Date()) || []
  const completedSprints = sprints?.filter((sprint) => new Date(sprint.endsAt) < new Date()) || []

  return (
    <div className="space-y-6">
      {showAddModal && <AddSprintModal onClose={() => setShowAddModal(false)} />}

      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-2">
        <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
          <CalendarDays className="text-blue-500" />
          Sprints
          <span className="text-sm font-normal text-gray-500 ml-2">
            {sprints?.length} {sprints?.length === 1 ? "sprint" : "sprints"}
          </span>
        </h1>
        <button
          onClick={() => setShowAddModal(true)}
          className="bg-blue-500 text-white px-4 py-2 rounded-md flex items-center gap-2 hover:bg-blue-600 transition-colors"
        >
          <Plus size={18} />
          <span>Add Sprint</span>
        </button>
      </div>

      {/* Active Sprints */}
      {activeSprints.length > 0 && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="p-4 border-b border-gray-100 bg-blue-50">
            <h2 className="text-lg font-semibold text-blue-800">Active Sprints</h2>
          </div>
          <div className="divide-y divide-gray-100">
            {activeSprints.map((sprint) => (
              <div key={sprint.id} className="p-4 hover:bg-gray-50 transition-colors">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div className="flex-grow">
                    <div className="flex items-center gap-2">
                      <h3 className="text-lg font-semibold text-gray-800">{sprint.name}</h3>
                      <span className="px-2 py-0.5 bg-green-100 text-green-800 text-xs font-medium rounded-full">
                        Active
                      </span>
                    </div>
                    {sprint.description && <p className="text-gray-600 mt-1">{sprint.description}</p>}
                    <div className="flex flex-wrap items-center gap-x-4 gap-y-2 mt-2 text-sm text-gray-600">
                      <div className="flex items-center gap-1">
                        <Calendar size={14} className="text-gray-400" />
                        <span>
                          {formatDate(sprint.startedAt)} - {formatDate(sprint.endsAt)}
                        </span>
                      </div>
                      <div className="flex items-center gap-1">
                        <Clock size={14} className="text-gray-400" />
                        <span>
                          {getDaysRemaining(sprint.endsAt)} day{getDaysRemaining(sprint.endsAt) !== 1 ? "s" : ""}{" "}
                          remaining
                        </span>
                      </div>
                      <div className="flex items-center gap-1">
                        <ListTodo size={14} className="text-gray-400" />
                        <span>{getSprintTaskCount(sprint.id)} tasks</span>
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={() => navigate(`/tasks?sprint=${sprint.id}`)}
                    className="flex items-center gap-1 px-3 py-2 bg-blue-50 hover:bg-blue-100 text-blue-600 rounded-md transition-colors"
                  >
                    <ListTodo size={16} />
                    <span>View Tasks</span>
                    <ChevronRight size={16} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Upcoming Sprints */}
      {upcomingSprints.length > 0 && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="p-4 border-b border-gray-100">
            <h2 className="text-lg font-semibold text-gray-800">Upcoming Sprints</h2>
          </div>
          <div className="divide-y divide-gray-100">
            {upcomingSprints.map((sprint) => (
              <div key={sprint.id} className="p-4 hover:bg-gray-50 transition-colors">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div className="flex-grow">
                    <div className="flex items-center gap-2">
                      <h3 className="text-lg font-semibold text-gray-800">{sprint.name}</h3>
                      <span className="px-2 py-0.5 bg-yellow-100 text-yellow-800 text-xs font-medium rounded-full">
                        Upcoming
                      </span>
                    </div>
                    {sprint.description && <p className="text-gray-600 mt-1">{sprint.description}</p>}
                    <div className="flex flex-wrap items-center gap-x-4 gap-y-2 mt-2 text-sm text-gray-600">
                      <div className="flex items-center gap-1">
                        <Calendar size={14} className="text-gray-400" />
                        <span>
                          {formatDate(sprint.startedAt)} - {formatDate(sprint.endsAt)}
                        </span>
                      </div>
                      <div className="flex items-center gap-1">
                        <Clock size={14} className="text-gray-400" />
                        <span>Starts in {Math.abs(getDaysRemaining(sprint.startedAt))} days</span>
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={() => navigate(`/tasks?sprint=${sprint.id}`)}
                    className="flex items-center gap-1 px-3 py-2 bg-gray-50 hover:bg-gray-100 text-gray-600 rounded-md transition-colors"
                  >
                    <ListTodo size={16} />
                    <span>View Tasks</span>
                    <ChevronRight size={16} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Completed Sprints */}
      {completedSprints.length > 0 && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="p-4 border-b border-gray-100">
            <h2 className="text-lg font-semibold text-gray-800">Completed Sprints</h2>
          </div>
          <div className="divide-y divide-gray-100">
            {completedSprints.map((sprint) => (
              <div key={sprint.id} className="p-4 hover:bg-gray-50 transition-colors">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div className="flex-grow">
                    <div className="flex items-center gap-2">
                      <h3 className="text-lg font-semibold text-gray-800">{sprint.name}</h3>
                      <span className="px-2 py-0.5 bg-gray-100 text-gray-800 text-xs font-medium rounded-full">
                        Completed
                      </span>
                    </div>
                    {sprint.description && <p className="text-gray-600 mt-1">{sprint.description}</p>}
                    <div className="flex flex-wrap items-center gap-x-4 gap-y-2 mt-2 text-sm text-gray-600">
                      <div className="flex items-center gap-1">
                        <Calendar size={14} className="text-gray-400" />
                        <span>
                          {formatDate(sprint.startedAt)} - {formatDate(sprint.endsAt)}
                        </span>
                      </div>
                      <div className="flex items-center gap-1">
                        <ListTodo size={14} className="text-gray-400" />
                        <span>{getSprintTaskCount(sprint.id)} tasks</span>
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={() => navigate(`/tasks?sprint=${sprint.id}`)}
                    className="flex items-center gap-1 px-3 py-2 bg-gray-50 hover:bg-gray-100 text-gray-600 rounded-md transition-colors"
                  >
                    <ListTodo size={16} />
                    <span>View Tasks</span>
                    <ChevronRight size={16} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Empty state */}
      {sprints?.length === 0 && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8 text-center">
          <CalendarDays className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-800 mb-2">No sprints yet</h3>
          <p className="text-gray-500 mb-6">Create your first sprint to start organizing your tasks</p>
          <button
            onClick={() => setShowAddModal(true)}
            className="bg-blue-500 text-white px-4 py-2 rounded-md inline-flex items-center gap-2 hover:bg-blue-600 transition-colors"
          >
            <Plus size={18} />
            <span>Add Sprint</span>
          </button>
        </div>
      )}
    </div>
  )
}

export default Sprints
