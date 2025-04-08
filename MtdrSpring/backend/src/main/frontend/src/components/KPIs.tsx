"use client"

import { useState, useMemo } from "react"
import { useQuery } from "@tanstack/react-query"
import { api } from "../utils/api/client"
import { Loader2, BarChart3, PieChart, ArrowDown, ArrowUp, Clock, ListChecks, Calendar, X } from "lucide-react"
import { Pie, Bar } from "react-chartjs-2"
import { Chart as ChartJS, ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title } from "chart.js"

// Register the chart components
ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title)

const KPIs = () => {
  const [selectedSprint, setSelectedSprint] = useState<number | null>(null)
  // Use getDevelopers to fetch the complete list of developers (consistent with Developers.tsx)
  const { data: users, isLoading: usersLoading } = useQuery({
    queryKey: ["users"],
    queryFn: api.users.getDevelopers,
  })

  const { data: tasks, isLoading: tasksLoading } = useQuery({
    queryKey: ["tasks"],
    queryFn: api.tasks.list,
  })

  const { data: sprints, isLoading: sprintsLoading } = useQuery({
    queryKey: ["sprints"],
    queryFn: api.sprints.getSprints,
  })

  // Generate data for the tasks per DEVELOPER pie chart
  const tasksPerDeveloperData = useMemo(() => {
    if (!users || !tasks) return null

    // Get developers
    const developers = users.filter((user) => user.role === "developer")

    // Count tasks per developer
    const taskCounts = developers.reduce(
      (acc, dev) => {
        // Ensure both sides are numbers
        const count = tasks.filter((task) => Number(task.assignedToId) === Number(dev.id)).length
        if (count > 0) {
          acc.labels.push(`${dev.firstName} ${dev.lastName}`)
          acc.data.push(count)

          // Generate random colors for each developer - but make them blueish
          const r = Math.floor(Math.random() * 100)
          const g = Math.floor(Math.random() * 100 + 120)
          const b = Math.floor(Math.random() * 50 + 200)
          acc.backgroundColor.push(`rgba(${r}, ${g}, ${b}, 0.6)`)
          acc.borderColor.push(`rgba(${r}, ${g}, ${b}, 1)`)
        }
        return acc
      },
      {
        labels: [] as string[],
        data: [] as number[],
        backgroundColor: [] as string[],
        borderColor: [] as string[],
      },
    )

    return {
      labels: taskCounts.labels,
      datasets: [
        {
          label: "Tasks per Developer",
          data: taskCounts.data,
          backgroundColor: taskCounts.backgroundColor,
          borderColor: taskCounts.borderColor,
          borderWidth: 1,
        },
      ],
    }
  }, [users, tasks])

  // Generate data for hours per sprint bar chart
  const hoursPerSprintData = useMemo(() => {
    if (!tasks || !sprints) return null

    const sprintHours = sprints.map((sprint) => {
      // Get completed tasks for this sprint that have realHours
      const sprintTasks = tasks.filter((task) => task.sprintId === sprint.id && task.status === "done")

      // Sum the real hours
      const totalRealHours = sprintTasks.reduce((sum, task) => sum + (task.realHours || 0), 0)

      // Sum the estimated hours
      const totalEstimateHours = sprintTasks.reduce((sum, task) => sum + (task.estimateHours || 0), 0)

      // Store estimated (actually, estimated as "estimateHours") hours for each task to display on click
      const taskHours = sprintTasks.map((task) => ({
        taskId: task.id,
        description: task.description,
        realHours: task.realHours || 0,
        estimateHours: task.estimateHours || 0,
      }))

      return {
        sprintId: sprint.id,
        sprintName: sprint.name,
        totalRealHours,
        totalEstimateHours,
        tasks: taskHours,
      }
    })

    return {
      labels: sprintHours.map((s) => s.sprintName),
      datasets: [
        {
          label: "Estimated Hours",
          data: sprintHours.map((s) => s.totalEstimateHours),
          backgroundColor: "rgba(156, 163, 219, 0.6)",
          borderColor: "rgba(156, 163, 219, 1)",
          borderWidth: 1,
        },
        {
          label: "Real Hours Spent",
          data: sprintHours.map((s) => s.totalRealHours),
          backgroundColor: "rgba(54, 162, 235, 0.6)",
          borderColor: "rgba(54, 162, 235, 1)",
          borderWidth: 1,
        },
      ],
      sprintDetails: sprintHours,
    }
  }, [tasks, sprints])

  // Get details of selected sprint to show estimated vs real hours
  const selectedSprintDetails = useMemo(() => {
    if (!selectedSprint || !hoursPerSprintData) return null

    return hoursPerSprintData.sprintDetails.find((s) => s.sprintId === selectedSprint)
  }, [selectedSprint, hoursPerSprintData])

  // Calculate some quick summary metrics
  const summaryMetrics = useMemo(() => {
    if (!tasks || !sprints) return null

    // Total tasks
    const totalTasks = tasks.length

    // Completed tasks
    const completedTasks = tasks.filter((task) => task.status === "done").length

    // Completion rate
    const completionRate = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0

    // Active sprints
    const activeSprints = sprints.filter((sprint) => new Date(sprint.endsAt) >= new Date()).length

    // Tasks without estimates
    const tasksWithoutEstimates = tasks.filter((task) => task.estimateHours === null).length

    return {
      totalTasks,
      completedTasks,
      completionRate,
      activeSprints,
      tasksWithoutEstimates,
    }
  }, [tasks, sprints])

  // Loading state
  if (usersLoading || tasksLoading || sprintsLoading) {
    return (
      <div className="flex justify-center items-center h-full p-8">
        <div className="flex flex-col items-center">
          <Loader2 className="w-10 h-10 animate-spin text-blue-500 mb-3" />
          <p className="text-gray-500">Loading KPI data...</p>
        </div>
      </div>
    )
  }

  // Error state
  if (!users || !tasks || !sprints) {
    return (
      <div className="flex justify-center items-center h-full p-8 text-red-600">
        <p>Failed to load data for KPIs</p>
      </div>
    )
  }

  return (
    <div className="p-6 bg-gray-50 min-h-screen">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
          <BarChart3 className="text-blue-500" />
          KPI Dashboard
        </h1>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6 mb-6">
        <div className="bg-white p-5 rounded-xl shadow-sm border border-gray-200">
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm font-medium text-gray-500 mb-1">Total Tasks</p>
              <h3 className="text-2xl font-bold text-gray-900">{summaryMetrics?.totalTasks}</h3>
            </div>
            <div className="bg-blue-100 p-2 rounded-lg">
              <ListChecks className="h-6 w-6 text-blue-600" />
            </div>
          </div>
          <div className="mt-2 flex items-center text-sm">
            <span className="text-gray-500">Completion rate:</span>
            <span className="ml-1 font-medium text-green-600">{summaryMetrics?.completionRate}%</span>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl shadow-sm border border-gray-200">
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm font-medium text-gray-500 mb-1">Completed Tasks</p>
              <h3 className="text-2xl font-bold text-gray-900">{summaryMetrics?.completedTasks}</h3>
            </div>
            <div className="bg-green-100 p-2 rounded-lg">
              <ListChecks className="h-6 w-6 text-green-600" />
            </div>
          </div>
          <div className="mt-2 flex items-center text-sm">
            <span className="text-gray-500">Total tasks:</span>
            <span className="ml-1 font-medium text-gray-700">{summaryMetrics?.totalTasks}</span>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl shadow-sm border border-gray-200">
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm font-medium text-gray-500 mb-1">Active Sprints</p>
              <h3 className="text-2xl font-bold text-gray-900">{summaryMetrics?.activeSprints}</h3>
            </div>
            <div className="bg-yellow-100 p-2 rounded-lg">
              <Calendar className="h-6 w-6 text-yellow-600" />
            </div>
          </div>
          <div className="mt-2 flex items-center text-sm">
            <span className="text-gray-500">Total sprints:</span>
            <span className="ml-1 font-medium text-gray-700">{sprints.length}</span>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl shadow-sm border border-gray-200">
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm font-medium text-gray-500 mb-1">Missing Estimates</p>
              <h3 className="text-2xl font-bold text-gray-900">{summaryMetrics?.tasksWithoutEstimates}</h3>
            </div>
            <div className="bg-red-100 p-2 rounded-lg">
              <Clock className="h-6 w-6 text-red-600" />
            </div>
          </div>
          <div className="mt-2 flex items-center text-sm">
            <span className="text-gray-500">Tasks affected:</span>
            <span className="ml-1 font-medium text-red-600">
              {summaryMetrics?.tasksWithoutEstimates ?? 0 > 0
                ? `${Math.round(((summaryMetrics?.tasksWithoutEstimates ?? 0) / (summaryMetrics?.totalTasks ?? 1)) * 100)}%`
                : "0%"}
            </span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Tasks per developer pie chart */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
              <PieChart className="h-5 w-5 text-blue-500" />
              Tasks per Developer
            </h2>
          </div>
          <div className="h-80">
            {tasksPerDeveloperData ? (
              <Pie
                data={tasksPerDeveloperData}
                options={{
                  responsive: true,
                  plugins: {
                    legend: {
                      position: "bottom",
                      labels: {
                        boxWidth: 12,
                        padding: 15,
                      },
                    },
                    tooltip: {
                      backgroundColor: "rgba(50, 50, 50, 0.8)",
                      padding: 10,
                      bodyFont: {
                        size: 12,
                      },
                      titleFont: {
                        size: 14,
                      },
                    },
                  },
                }}
              />
            ) : (
              <div className="flex justify-center items-center h-full text-gray-500">No data available</div>
            )}
          </div>
        </div>

        {/* Hours per sprint bar chart */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
              <BarChart3 className="h-5 w-5 text-blue-500" />
              Hours per Sprint
            </h2>
          </div>
          <div className="h-80">
            {hoursPerSprintData ? (
              <Bar
                data={hoursPerSprintData}
                options={{
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: {
                    legend: {
                      position: "bottom",
                      labels: {
                        boxWidth: 12,
                        padding: 15,
                      },
                    },
                    tooltip: {
                      backgroundColor: "rgba(50, 50, 50, 0.8)",
                      padding: 10,
                    },
                  },
                  scales: {
                    y: {
                      title: {
                        display: true,
                        text: "Hours",
                      },
                    },
                  },
                  onClick: (_, elements) => {
                    if (elements.length > 0) {
                      const index = elements[0].index
                      const sprintId = hoursPerSprintData.sprintDetails[index].sprintId
                      setSelectedSprint(sprintId)
                    }
                  },
                }}
              />
            ) : (
              <div className="flex justify-center items-center h-full text-gray-500">No data available</div>
            )}
          </div>
          <p className="text-sm text-gray-500 mt-2 text-center">Click on a bar to see task details</p>
        </div>
      </div>

      {/* Sprint task details panel */}
      {selectedSprintDetails && (
        <div className="mt-6 bg-white p-6 rounded-xl shadow-sm border border-gray-200">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
              <Calendar className="h-5 w-5 text-blue-500" />
              Tasks in {selectedSprintDetails.sprintName}
            </h2>
            <button
              className="text-sm text-gray-500 hover:text-gray-700 px-3 py-1 rounded-md hover:bg-gray-100 flex items-center gap-1 transition-colors"
              onClick={() => setSelectedSprint(null)}
            >
              <X size={14} />
              Close
            </button>
          </div>

          <div className="overflow-x-auto rounded-lg border border-gray-200">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Task
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Estimated Hours
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actual Hours
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Diff
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {selectedSprintDetails.tasks.map((task) => (
                  <tr
                    key={task.taskId}
                    className={
                      task.realHours > task.estimateHours
                        ? "bg-red-50"
                        : task.realHours < task.estimateHours
                          ? "bg-green-50"
                          : ""
                    }
                  >
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{task.description}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{task.estimateHours}h</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{task.realHours}h</td>
                    <td
                      className={`px-6 py-4 whitespace-nowrap text-sm font-medium flex items-center
                      ${
                        task.realHours > task.estimateHours
                          ? "text-red-600"
                          : task.realHours < task.estimateHours
                            ? "text-green-600"
                            : "text-gray-900"
                      }`}
                    >
                      {task.realHours > task.estimateHours ? (
                        <ArrowUp className="w-4 h-4 mr-1 text-red-600" />
                      ) : task.realHours < task.estimateHours ? (
                        <ArrowDown className="w-4 h-4 mr-1 text-green-600" />
                      ) : null}
                      {task.realHours - task.estimateHours}h
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot className="bg-gray-50 font-medium">
                <tr>
                  <td className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Total
                  </td>
                  <td className="px-6 py-3 text-left text-xs font-medium text-gray-900">
                    {selectedSprintDetails.tasks.reduce((sum, task) => sum + task.estimateHours, 0)}h
                  </td>
                  <td className="px-6 py-3 text-left text-xs font-medium text-gray-900">
                    {selectedSprintDetails.totalRealHours}h
                  </td>
                  <td
                    className={`px-6 py-3 text-left text-xs font-medium flex items-center
                    ${
                      selectedSprintDetails.totalRealHours >
                      selectedSprintDetails.tasks.reduce((sum, task) => sum + task.estimateHours, 0)
                        ? "text-red-600"
                        : "text-green-600"
                    }`}
                  >
                    {selectedSprintDetails.totalRealHours >
                    selectedSprintDetails.tasks.reduce((sum, task) => sum + task.estimateHours, 0) ? (
                      <ArrowUp className="w-4 h-4 mr-1 text-red-600" />
                    ) : (
                      <ArrowDown className="w-4 h-4 mr-1 text-green-600" />
                    )}
                    {selectedSprintDetails.totalRealHours -
                      selectedSprintDetails.tasks.reduce((sum, task) => sum + task.estimateHours, 0)}
                    h
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}

export default KPIs
