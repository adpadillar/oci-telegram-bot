"use client";

import { useState, useMemo, useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "../utils/api/client";
import {
  Loader2,
  BarChart3,
  PieChart,
  ArrowDown,
  ArrowUp,
  Calendar,
  X,
  TrendingUp,
  Users,
  CheckCircle2,
  AlertCircle,
  Filter,
  LineChart,
  Zap,
  Target,
  Award,
  ClipboardList,
  Timer,
  GitPullRequest,
  BrainCircuit,
  Scale,
  UserX,
  UsersRound,
  Download,
} from "lucide-react";
import { Pie, Bar, Line } from "react-chartjs-2";
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  PointElement,
  LineElement,
} from "chart.js";
import { generateTaskReport } from "../utils/reports/pdf-generator";

// Register the chart components
ChartJS.register(
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  PointElement,
  LineElement,
);

// Custom hook for extra small screens (below sm breakpoint)
// eslint-disable-next-line react-refresh/only-export-components
export const useExtraSmallScreen = () => {
  const [isXs, setIsXs] = useState(false);

  useEffect(() => {
    const checkScreenSize = () => {
      setIsXs(window.innerWidth < 640); // sm breakpoint
    };

    checkScreenSize();
    window.addEventListener("resize", checkScreenSize);

    return () => {
      window.removeEventListener("resize", checkScreenSize);
    };
  }, []);

  return isXs;
};

const KPIs = () => {
  const [selectedSprint, setSelectedSprint] = useState<number | null>(null);
  const [selectedMetricView, setSelectedMetricView] = useState<
    "tasks" | "developers" | "sprints"
  >("tasks");
  const [showTrends, setShowTrends] = useState(false);
  const [hoveredCard, setHoveredCard] = useState<string | null>(null);
  const [selectedSprintFilter, setSelectedSprintFilter] = useState<
    number | null
  >(null);

  const isXs = useExtraSmallScreen();

  // Use getDevelopers to fetch the complete list of developers (consistent with Developers.tsx)
  const { data: users, isLoading: usersLoading } = useQuery({
    queryKey: ["users"],
    queryFn: api.users.getUsers,
  });

  const { data: tasks, isLoading: tasksLoading } = useQuery({
    queryKey: ["tasks"],
    queryFn: api.tasks.list,
  });

  const { data: sprints, isLoading: sprintsLoading } = useQuery({
    queryKey: ["sprints"],
    queryFn: api.sprints.getSprints,
  });

  const handleDownloadReport = async () => {
    try {
      if (!users || !tasks || !sprints) {
        throw new Error("Required data not loaded");
      }

      const pdf = await generateTaskReport({
        users,
        tasks,
        sprints,
        date: new Date().toLocaleDateString(),
      });
      pdf.save("task-report.pdf");
    } catch (error) {
      console.error("Error generating report:", error);
    }
  };

  // Generate data for the tasks per DEVELOPER pie chart
  const tasksPerDeveloperData = useMemo(() => {
    if (!users || !tasks) return null;

    // Get developers
    const developers = users;

    // Count tasks per developer
    const taskCounts = developers.reduce(
      (acc, dev) => {
        // Ensure both sides are numbers
        const count = tasks.filter(
          (task) => Number(task.assignedToId) === Number(dev.id),
        ).length;
        if (count > 0) {
          acc.labels.push(`${dev.firstName} ${dev.lastName}`);
          acc.data.push(count);

          // Generate random colors for each developer - but make them blueish
          const r = Math.floor(Math.random() * 100);
          const g = Math.floor(Math.random() * 100 + 120);
          const b = Math.floor(Math.random() * 50 + 200);
          acc.backgroundColor.push(`rgba(${r}, ${g}, ${b}, 0.6)`);
          acc.borderColor.push(`rgba(${r}, ${g}, ${b}, 1)`);
        }
        return acc;
      },
      {
        labels: [] as string[],
        data: [] as number[],
        backgroundColor: [] as string[],
        borderColor: [] as string[],
      },
    );

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
    };
  }, [users, tasks]);

  // Generate data for hours per sprint bar chart
  const hoursPerSprintData = useMemo(() => {
    if (!tasks || !sprints) return null;

    const sprintHours = sprints.map((sprint) => {
      // Get completed tasks for this sprint that have realHours
      const sprintTasks = tasks.filter((task) => task.sprintId === sprint.id);

      // Sum the real hours
      const totalRealHours = sprintTasks.reduce(
        (sum, task) => sum + (task.realHours || 0),
        0,
      );

      // Sum the estimated hours
      const totalEstimateHours = sprintTasks.reduce(
        (sum, task) => sum + (task.estimateHours || 0),
        0,
      );

      // Store estimated (actually, estimated as "estimateHours") hours for each task to display on click
      const taskHours = sprintTasks.map((task) => ({
        taskId: task.id,
        description: task.description,
        realHours: task.realHours || 0,
        estimateHours: task.estimateHours || 0,
      }));

      return {
        sprintId: sprint.id,
        sprintName: sprint.name,
        totalRealHours,
        totalEstimateHours,
        tasks: taskHours,
      };
    });

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
    };
  }, [tasks, sprints]);

  // Generate data for task status distribution
  const taskStatusData = useMemo(() => {
    if (!tasks) return null;

    const statusCounts = {
      created: 0,
      "in-progress": 0,
      "in-review": 0,
      testing: 0,
      done: 0,
    };

    tasks.forEach((task) => {
      if (task.status in statusCounts) {
        statusCounts[task.status as keyof typeof statusCounts]++;
      }
    });

    return {
      labels: ["Created", "In Progress", "In Review", "Testing", "Done"],
      datasets: [
        {
          label: "Tasks by Status",
          data: [
            statusCounts.created,
            statusCounts["in-progress"],
            statusCounts["in-review"],
            statusCounts.testing,
            statusCounts.done,
          ],
          backgroundColor: [
            "rgba(156, 163, 219, 0.6)",
            "rgba(255, 206, 86, 0.6)",
            "rgba(54, 162, 235, 0.6)",
            "rgba(153, 102, 255, 0.6)",
            "rgba(75, 192, 192, 0.6)",
          ],
          borderColor: [
            "rgba(156, 163, 219, 1)",
            "rgba(255, 206, 86, 1)",
            "rgba(54, 162, 235, 1)",
            "rgba(153, 102, 255, 1)",
            "rgba(75, 192, 192, 1)",
          ],
          borderWidth: 1,
        },
      ],
    };
  }, [tasks]);

  // Generate data for task category distribution
  const taskCategoryData = useMemo(() => {
    if (!tasks) return null;

    const categoryCounts = {
      bug: 0,
      feature: 0,
      issue: 0,
      null: 0,
    };

    tasks.forEach((task) => {
      const category = task.category || "null";
      if (category in categoryCounts) {
        categoryCounts[category as keyof typeof categoryCounts]++;
      }
    });

    return {
      labels: ["Bug", "Feature", "Issue", "Uncategorized"],
      datasets: [
        {
          label: "Tasks by Category",
          data: [
            categoryCounts.bug,
            categoryCounts.feature,
            categoryCounts.issue,
            categoryCounts.null,
          ],
          backgroundColor: [
            "rgba(255, 99, 132, 0.6)",
            "rgba(54, 162, 235, 0.6)",
            "rgba(255, 206, 86, 0.6)",
            "rgba(156, 163, 219, 0.6)",
          ],
          borderColor: [
            "rgba(255, 99, 132, 1)",
            "rgba(54, 162, 235, 1)",
            "rgba(255, 206, 86, 1)",
            "rgba(156, 163, 219, 1)",
          ],
          borderWidth: 1,
        },
      ],
    };
  }, [tasks]);

  // Generate data for sprint completion trend
  const sprintCompletionTrendData = useMemo(() => {
    if (!tasks || !sprints) return null;

    // Sort sprints by start date
    const sortedSprints = [...sprints].sort(
      (a, b) =>
        new Date(a.startedAt).getTime() - new Date(b.startedAt).getTime(),
    );

    const completionRates = sortedSprints.map((sprint) => {
      const sprintTasks = tasks.filter((task) => task.sprintId === sprint.id);
      const completedTasks = sprintTasks.filter(
        (task) => task.status === "done",
      );
      const completionRate =
        sprintTasks.length > 0
          ? (completedTasks.length / sprintTasks.length) * 100
          : 0;

      return {
        sprintName: sprint.name,
        completionRate: Math.round(completionRate),
        taskCount: sprintTasks.length,
      };
    });

    return {
      labels: completionRates.map((s) => s.sprintName),
      datasets: [
        {
          label: "Completion Rate (%)",
          data: completionRates.map((s) => s.completionRate),
          borderColor: "rgba(54, 162, 235, 1)",
          backgroundColor: "rgba(54, 162, 235, 0.2)",
          tension: 0.3,
          fill: true,
        },
        {
          label: "Task Count",
          data: completionRates.map((s) => s.taskCount),
          borderColor: "rgba(156, 163, 219, 1)",
          backgroundColor: "transparent",
          borderDashed: true,
          tension: 0.3,
        },
      ],
    };
  }, [tasks, sprints]);

  // Get details of selected sprint to show estimated vs real hours
  const selectedSprintDetails = useMemo(() => {
    if (!selectedSprint || !hoursPerSprintData) return null;

    return hoursPerSprintData.sprintDetails.find(
      (s) => s.sprintId === selectedSprint,
    );
  }, [selectedSprint, hoursPerSprintData]);

  // Calculate some quick summary metrics
  const summaryMetrics = useMemo(() => {
    if (!tasks || !sprints) return null;

    // Total tasks
    const totalTasks = tasks.length;

    // Completed tasks
    const completedTasks = tasks.filter(
      (task) => task.status === "done",
    ).length;

    // Completion rate
    const completionRate =
      totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;

    // Active sprints
    const activeSprints = sprints.filter(
      (sprint) => new Date(sprint.endsAt) >= new Date(),
    ).length;

    // Tasks without estimates
    const tasksWithoutEstimates = tasks.filter(
      (task) => task.estimateHours === null,
    ).length;

    // Tasks without assignees
    const tasksWithoutAssignees = tasks.filter(
      (task) => task.assignedToId === null,
    ).length;

    // Average tasks per sprint
    const avgTasksPerSprint =
      sprints.length > 0 ? Math.round(tasks.length / sprints.length) : 0;

    // Estimate accuracy (ratio of real hours to estimated hours)
    const tasksWithBothHours = tasks.filter(
      (task) => task.status === "done" && task.estimateHours && task.realHours,
    );

    const estimateAccuracy =
      tasksWithBothHours.length > 0
        ? Math.round(
            (tasksWithBothHours.reduce(
              (sum, task) => sum + (task.realHours || 0),
              0,
            ) /
              tasksWithBothHours.reduce(
                (sum, task) => sum + (task.estimateHours || 0),
                0,
              )) *
              100,
          )
        : 0;

    return {
      totalTasks,
      completedTasks,
      completionRate,
      activeSprints,
      tasksWithoutEstimates,
      tasksWithoutAssignees,
      avgTasksPerSprint,
      estimateAccuracy,
    };
  }, [tasks, sprints]);

  // Calculate team metrics per sprint
  const teamMetricsPerSprint = useMemo(() => {
    if (!tasks || !sprints) return null;

    return sprints.map((sprint) => {
      const sprintTasks = tasks.filter((task) => task.sprintId === sprint.id);
      const completedTasks = sprintTasks.filter(
        (task) => task.status === "done",
      );
      const totalHours = sprintTasks.reduce(
        (sum, task) => sum + (task.realHours || 0),
        0,
      );

      return {
        sprintId: sprint.id,
        sprintName: sprint.name,
        totalTasks: sprintTasks.length,
        completedTasks: completedTasks.length,
        totalHours,
        completionRate:
          sprintTasks.length > 0
            ? (completedTasks.length / sprintTasks.length) * 100
            : 0,
      };
    });
  }, [tasks, sprints]);

  // Calculate developer metrics per sprint
  const developerMetricsPerSprint = useMemo(() => {
    if (!users || !tasks || !sprints) return null;

    const developers = users;

    return developers.map((dev) => {
      const devTasks = selectedSprintFilter
        ? tasks.filter(
            (task) =>
              task.assignedToId === dev.id &&
              task.sprintId === selectedSprintFilter,
          )
        : tasks.filter((task) => task.assignedToId === dev.id);

      const completedTasks = devTasks.filter((task) => task.status === "done");
      const totalHours = devTasks.reduce(
        (sum, task) => sum + (task.realHours || 0),
        0,
      );

      return {
        id: dev.id,
        name: `${dev.firstName} ${dev.lastName}`,
        totalTasks: devTasks.length,
        completedTasks: completedTasks.length,
        totalHours,
        completionRate:
          devTasks.length > 0
            ? (completedTasks.length / devTasks.length) * 100
            : 0,
      };
    });
  }, [users, tasks, sprints, selectedSprintFilter]);

  // 1. Agregar los nuevos datasets usando useMemo
  const hoursByDeveloperPerSprint = useMemo(() => {
    if (!users || !tasks || !sprints) return null;
    const labels = sprints.map((s) => s.name);
    const datasets = users.map((dev, idx) => {
      const data = sprints.map((sprint) => {
        return tasks
          .filter(
            (task) => task.sprintId === sprint.id && task.assignedToId === dev.id && task.realHours
          )
          .reduce((sum, task) => sum + (task.realHours || 0), 0);
      });
      return {
        label: `${dev.firstName} ${dev.lastName}`,
        data,
        backgroundColor: `rgba(${50 + idx * 40}, ${120 + idx * 30}, 220, 0.6)`
      };
    });
    return { labels, datasets };
  }, [users, tasks, sprints]);

  const completedTasksByDeveloperPerSprint = useMemo(() => {
    if (!users || !tasks || !sprints) return null;
    const labels = sprints.map((s) => s.name);
    const datasets = users.map((dev, idx) => {
      const data = sprints.map((sprint) => {
        return tasks.filter(
          (task) => task.sprintId === sprint.id && task.assignedToId === dev.id && task.status === 'done'
        ).length;
      });
      return {
        label: `${dev.firstName} ${dev.lastName}`,
        data,
        backgroundColor: `rgba(${50 + idx * 40}, ${200 - idx * 30}, 150, 0.6)`
      };
    });
    return { labels, datasets };
  }, [users, tasks, sprints]);

  // Loading state
  if (usersLoading || tasksLoading || sprintsLoading) {
    return (
      <div className="flex justify-center items-center h-full p-8">
        <div className="flex flex-col items-center">
          <Loader2 className="w-10 h-10 animate-spin text-blue-500 mb-3" />
          <p className="text-gray-500">Loading KPI data...</p>
        </div>
      </div>
    );
  }

  // Error state
  if (!users || !tasks || !sprints) {
    return (
      <div className="flex justify-center items-center h-full p-8 text-red-600">
        <p>Failed to load data for KPIs</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-4">
        <h1 className="text-xl sm:text-2xl font-bold text-gray-800 flex items-center gap-2">
          <BarChart3 className="text-blue-500" />
          KPI Dashboard
        </h1>
        <div className="flex flex-wrap items-center gap-2 sm:gap-3 w-full sm:w-auto">
          <button
            onClick={handleDownloadReport}
            className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-white bg-blue-500 rounded-md hover:bg-blue-600 transition-colors"
          >
            <Download size={16} />
            Download Report
          </button>
          <div className="bg-gray-100 p-1 rounded-md flex flex-wrap w-full sm:w-auto">
            <button
              onClick={() => setSelectedMetricView("tasks")}
              className={`flex-1 sm:flex-none px-2 sm:px-3 py-1.5 rounded-md text-xs sm:text-sm font-medium transition-colors ${
                selectedMetricView === "tasks"
                  ? "bg-white shadow-sm text-blue-600"
                  : "text-gray-600"
              }`}
            >
              Tasks
            </button>
            <button
              onClick={() => setSelectedMetricView("developers")}
              className={`flex-1 sm:flex-none px-2 sm:px-3 py-1.5 rounded-md text-xs sm:text-sm font-medium transition-colors ${
                selectedMetricView === "developers"
                  ? "bg-white shadow-sm text-blue-600"
                  : "text-gray-600"
              }`}
            >
              Developers
            </button>
            <button
              onClick={() => setSelectedMetricView("sprints")}
              className={`flex-1 sm:flex-none px-2 sm:px-3 py-1.5 rounded-md text-xs sm:text-sm font-medium transition-colors ${
                selectedMetricView === "sprints"
                  ? "bg-white shadow-sm text-blue-600"
                  : "text-gray-600"
              }`}
            >
              Sprints
            </button>
          </div>
          {selectedMetricView === "sprints" && (
            <button
              onClick={() => setShowTrends(!showTrends)}
              className={`px-3 py-1.5 rounded-md text-xs sm:text-sm font-medium flex items-center gap-1 ${
                showTrends
                  ? "bg-blue-100 text-blue-600 hover:bg-blue-200"
                  : "bg-gray-100 text-gray-600 hover:bg-gray-200"
              }`}
            >
              <TrendingUp size={16} />
              <span>Trends</span>
            </button>
          )}
        </div>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-1 xs:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
        <div
          data-testid="task-overview-card"
          className="relative bg-white p-5 rounded-xl shadow-sm border border-gray-200 cursor-help"
          onMouseEnter={() => setHoveredCard("taskOverview")}
          onMouseLeave={() => setHoveredCard(null)}
        >
          {hoveredCard === "taskOverview" && (
            <div className="fixed sm:absolute z-50 w-64 p-4 bg-gray-900 text-white text-xs sm:text-sm rounded-lg shadow-lg transform -translate-x-1/2 left-1/2 sm:translate-x-0 sm:left-0 top-20 sm:-top-2">
              Overview of total tasks in the system and their completion status.
              <div className="absolute left-1/2 -ml-2 -bottom-2 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-gray-900 sm:left-4 sm:top-full"></div>
            </div>
          )}
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm font-medium text-gray-500 mb-1">
                Task Overview
              </p>
              <h3 className="text-2xl font-bold text-gray-900">
                {summaryMetrics?.totalTasks}
              </h3>
            </div>
            <div className="bg-blue-100 p-2 rounded-lg">
              <ClipboardList className="h-6 w-6 text-blue-600" />
            </div>
          </div>
          <div className="mt-2 flex items-center text-sm">
            <span className="text-gray-500">Completion rate:</span>
            <span className="ml-1 font-medium text-green-600">
              {summaryMetrics?.completionRate}%
            </span>
          </div>
        </div>

        <div
          className="relative bg-white p-5 rounded-xl shadow-sm border border-gray-200 cursor-help"
          onMouseEnter={() => setHoveredCard("completedTasks")}
          onMouseLeave={() => setHoveredCard(null)}
        >
          {hoveredCard === "completedTasks" && (
            <div className="fixed sm:absolute z-50 w-64 p-4 bg-gray-900 text-white text-xs sm:text-sm rounded-lg shadow-lg transform -translate-x-1/2 left-1/2 sm:translate-x-0 sm:left-0 top-20 sm:-top-2">
              Number of tasks currently in progress and in review
              <div className="absolute left-1/2 -ml-2 -bottom-2 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-gray-900 sm:left-4 sm:top-full"></div>
            </div>
          )}
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm font-medium text-gray-500 mb-1">
                Tasks in Progress
              </p>
              <h3 className="text-2xl font-bold text-gray-900">
                {tasks?.filter((task) => task.status === "in-progress").length}
              </h3>
            </div>
            <div className="bg-yellow-100 p-2 rounded-lg">
              <GitPullRequest className="h-6 w-6 text-yellow-600" />
            </div>
          </div>
          <div className="mt-2 flex items-center text-sm">
            <span className="text-gray-500">In Review:</span>
            <span className="ml-1 font-medium text-gray-600">
              {tasks?.filter((task) => task.status === "in-review").length}
            </span>
          </div>
        </div>

        <div
          className="relative bg-white p-5 rounded-xl shadow-sm border border-gray-200 cursor-help"
          onMouseEnter={() => setHoveredCard("activeSprints")}
          onMouseLeave={() => setHoveredCard(null)}
        >
          {hoveredCard === "activeSprints" && (
            <div className="fixed sm:absolute z-50 w-64 p-4 bg-gray-900 text-white text-xs sm:text-sm rounded-lg shadow-lg transform -translate-x-1/2 left-1/2 sm:translate-x-0 sm:left-0 top-20 sm:-top-2">
              Number of sprints currently in progress or upcoming and total
              number of sprints
              <div className="absolute left-1/2 -ml-2 -bottom-2 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-gray-900 sm:left-4 sm:top-full"></div>
            </div>
          )}
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm font-medium text-gray-500 mb-1">
                Active Sprints
              </p>
              <h3 className="text-2xl font-bold text-gray-900">
                {summaryMetrics?.activeSprints}
              </h3>
            </div>
            <div className="bg-yellow-100 p-2 rounded-lg">
              <Timer className="h-6 w-6 text-yellow-600" />
            </div>
          </div>
          <div className="mt-2 flex items-center text-sm">
            <span className="text-gray-500">Total sprints:</span>
            <span className="ml-1 font-medium text-gray-700">
              {sprints.length}
            </span>
          </div>
        </div>

        <div
          className="relative bg-white p-5 rounded-xl shadow-sm border border-gray-200 cursor-help"
          onMouseEnter={() => setHoveredCard("missingEstimates")}
          onMouseLeave={() => setHoveredCard(null)}
        >
          {hoveredCard === "missingEstimates" && (
            <div className="fixed sm:absolute z-50 w-64 p-4 bg-gray-900 text-white text-xs sm:text-sm rounded-lg shadow-lg transform -translate-x-1/2 left-1/2 sm:translate-x-0 sm:left-0 top-20 sm:-top-2">
              Tasks that don't have time estimates set, which can affect sprint
              planning accuracy
              <div className="absolute left-1/2 -ml-2 -bottom-2 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-gray-900 sm:left-4 sm:top-full"></div>
            </div>
          )}
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm font-medium text-gray-500 mb-1">
                Missing Estimates
              </p>
              <h3 className="text-2xl font-bold text-gray-900">
                {summaryMetrics?.tasksWithoutEstimates}
              </h3>
            </div>
            <div className="bg-red-100 p-2 rounded-lg">
              <Scale className="h-6 w-6 text-red-600" />
            </div>
          </div>
          <div className="mt-2 flex items-center text-sm">
            <span className="text-gray-500">Tasks affected:</span>
            <span className="ml-1 font-medium text-red-600">
              {(summaryMetrics?.tasksWithoutEstimates ?? 0 > 0)
                ? `${Math.round(((summaryMetrics?.tasksWithoutEstimates ?? 0) / (summaryMetrics?.totalTasks ?? 1)) * 100)}%`
                : "0%"}
            </span>
          </div>
        </div>
      </div>

      {/* Additional metrics cards */}
      <div className="grid grid-cols-1 xs:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
        <div
          className="relative bg-white p-5 rounded-xl shadow-sm border border-gray-200 cursor-help"
          onMouseEnter={() => setHoveredCard("avgTasksPerSprint")}
          onMouseLeave={() => setHoveredCard(null)}
        >
          {hoveredCard === "avgTasksPerSprint" && (
            <div className="fixed sm:absolute z-50 w-64 p-4 bg-gray-900 text-white text-xs sm:text-sm rounded-lg shadow-lg transform -translate-x-1/2 left-1/2 sm:translate-x-0 sm:left-0 top-20 sm:-top-2">
              Average number of tasks assigned per sprint, helping to understand
              task distribution and workload
              <div className="absolute left-1/2 -ml-2 -bottom-2 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-gray-900 sm:left-4 sm:top-full"></div>
            </div>
          )}
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm font-medium text-gray-500 mb-1">
                Avg. Tasks per Sprint
              </p>
              <h3 className="text-2xl font-bold text-gray-900">
                {summaryMetrics?.avgTasksPerSprint}
              </h3>
            </div>
            <div className="bg-purple-100 p-2 rounded-lg">
              <BrainCircuit className="h-6 w-6 text-purple-600" />
            </div>
          </div>
          <div className="mt-2 flex items-center text-sm">
            <span className="text-gray-500">
              Based on {sprints.length} sprints
            </span>
          </div>
        </div>

        <div
          className="relative bg-white p-5 rounded-xl shadow-sm border border-gray-200 cursor-help"
          onMouseEnter={() => setHoveredCard("estimateAccuracy")}
          onMouseLeave={() => setHoveredCard(null)}
        >
          {hoveredCard === "estimateAccuracy" && (
            <div className="fixed sm:absolute z-50 w-64 p-4 bg-gray-900 text-white text-xs sm:text-sm rounded-lg shadow-lg transform -translate-x-1/2 left-1/2 sm:translate-x-0 sm:left-0 top-20 sm:-top-2">
              How accurate task time estimates are compared to actual time
              spent. Over 100% means tasks took longer than estimated
              <div className="absolute left-1/2 -ml-2 -bottom-2 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-gray-900 sm:left-4 sm:top-full"></div>
            </div>
          )}
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm font-medium text-gray-500 mb-1">
                Estimate Accuracy
              </p>
              <h3 className="text-2xl font-bold text-gray-900">
                {summaryMetrics?.estimateAccuracy}%
              </h3>
            </div>
            <div className="bg-blue-100 p-2 rounded-lg">
              <Target className="h-6 w-6 text-blue-600" />
            </div>
          </div>
          <div className="mt-2 flex items-center text-sm">
            <span className="text-gray-500">
              {summaryMetrics?.estimateAccuracy &&
              summaryMetrics.estimateAccuracy > 100
                ? "Underestimated by "
                : "Overestimated by "}
            </span>
            <span
              className={`ml-1 font-medium ${
                summaryMetrics?.estimateAccuracy &&
                Math.abs(summaryMetrics.estimateAccuracy - 100) <= 10
                  ? "text-green-600"
                  : "text-orange-600"
              }`}
            >
              {summaryMetrics?.estimateAccuracy
                ? Math.abs(summaryMetrics.estimateAccuracy - 100)
                : 0}
              %
            </span>
          </div>
        </div>

        <div
          className="relative bg-white p-5 rounded-xl shadow-sm border border-gray-200 cursor-help"
          onMouseEnter={() => setHoveredCard("unassignedTasks")}
          onMouseLeave={() => setHoveredCard(null)}
        >
          {hoveredCard === "unassignedTasks" && (
            <div className="fixed sm:absolute z-50 w-64 p-4 bg-gray-900 text-white text-xs sm:text-sm rounded-lg shadow-lg transform -translate-x-1/2 left-1/2 sm:translate-x-0 sm:left-0 top-20 sm:-top-2">
              Tasks that haven't been assigned to any team member, which may
              delay project progress
              <div className="absolute left-1/2 -ml-2 -bottom-2 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-gray-900 sm:left-4 sm:top-full"></div>
            </div>
          )}
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm font-medium text-gray-500 mb-1">
                Unassigned Tasks
              </p>
              <h3 className="text-2xl font-bold text-gray-900">
                {summaryMetrics?.tasksWithoutAssignees}
              </h3>
            </div>
            <div className="bg-orange-100 p-2 rounded-lg">
              <UserX className="h-6 w-6 text-orange-600" />
            </div>
          </div>
          <div className="mt-2 flex items-center text-sm">
            <span className="text-gray-500">Tasks affected:</span>
            <span className="ml-1 font-medium text-orange-600">
              {(summaryMetrics?.tasksWithoutAssignees ?? 0 > 0)
                ? `${Math.round(((summaryMetrics?.tasksWithoutAssignees ?? 0) / (summaryMetrics?.totalTasks ?? 1)) * 100)}%`
                : "0%"}
            </span>
          </div>
        </div>

        <div
          className="relative bg-white p-5 rounded-xl shadow-sm border border-gray-200 cursor-help"
          onMouseEnter={() => setHoveredCard("teamSize")}
          onMouseLeave={() => setHoveredCard(null)}
        >
          {hoveredCard === "teamSize" && (
            <div className="fixed sm:absolute z-50 w-64 p-4 bg-gray-900 text-white text-xs sm:text-sm rounded-lg shadow-lg transform -translate-x-1/2 left-1/2 sm:translate-x-0 sm:left-0 top-20 sm:-top-2">
              Number of active developers in the team and average number of
              tasks per developer
              <div className="absolute left-1/2 -ml-2 -bottom-2 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-gray-900 sm:left-4 sm:top-full"></div>
            </div>
          )}
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm font-medium text-gray-500 mb-1">
                Team Size
              </p>
              <h3 className="text-2xl font-bold text-gray-900">
                {users.length}
              </h3>
            </div>
            <div className="bg-teal-100 p-2 rounded-lg">
              <UsersRound className="h-6 w-6 text-teal-600" />
            </div>
          </div>
          <div className="mt-2 flex items-center text-sm">
            <span className="text-gray-500">Tasks per team member:</span>
            <span className="ml-1 font-medium text-gray-700">
              {Math.round(
                tasks.filter((task) => task.assignedToId !== null).length /
                  (users.length || 1),
              )}
            </span>
          </div>
        </div>
      </div>

      {/* Task metrics view */}
      {selectedMetricView === "tasks" && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Tasks per status pie chart */}
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                <PieChart className="h-5 w-5 text-blue-500" />
                Tasks by Status
              </h2>
            </div>
            <div className="h-60 sm:h-70 md:h-80">
              {taskStatusData ? (
                <Pie
                  data={taskStatusData}
                  options={{
                    responsive: true,
                    plugins: {
                      legend: {
                        position: isXs ? "right" : "bottom",
                        labels: {
                          boxWidth: isXs ? 8 : 12,
                          padding: isXs ? 10 : 15,
                          font: {
                            size: isXs ? 10 : 12,
                          },
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
                <div className="flex justify-center items-center h-full text-gray-500">
                  No data available
                </div>
              )}
            </div>
          </div>

          {/* Tasks per category pie chart */}
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                <Filter className="h-5 w-5 text-blue-500" />
                Tasks by Category
              </h2>
            </div>
            <div className="h-60 sm:h-70 md:h-80">
              {taskCategoryData ? (
                <Pie
                  data={taskCategoryData}
                  options={{
                    responsive: true,
                    plugins: {
                      legend: {
                        position: isXs ? "right" : "bottom",
                        labels: {
                          boxWidth: isXs ? 8 : 12,
                          padding: isXs ? 10 : 15,
                          font: {
                            size: isXs ? 10 : 12,
                          },
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
                <div className="flex justify-center items-center h-full text-gray-500">
                  No data available
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Developer metrics view */}
      {selectedMetricView === "developers" && (
        <div className="grid grid-cols-1 gap-6">
          {/* Sprint filter for developer metrics */}
          <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-200">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-medium text-gray-700">
                Filter by Sprint
              </h3>
              <select
                value={selectedSprintFilter || ""}
                onChange={(e) =>
                  setSelectedSprintFilter(
                    e.target.value ? Number(e.target.value) : null,
                  )
                }
                className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md"
              >
                <option value="">All Sprints</option>
                {sprints?.map((sprint) => (
                  <option key={sprint.id} value={sprint.id}>
                    {sprint.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Developer performance table */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
            <div className="p-4 border-b border-gray-100">
              <h2 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                <Award className="h-5 w-5 text-blue-500" />
                Team Performance
              </h2>
            </div>
            <div className="overflow-x-auto -mx-6 px-6">
              <table className="min-w-full divide-y divide-gray-200 table-fixed sm:table-auto">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Developer
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Total Tasks
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Completed
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Hours Worked
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Completion Rate
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {developerMetricsPerSprint?.map((dev) => (
                    <tr key={dev.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="flex-shrink-0 h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-medium">
                            {dev.name.charAt(0)}
                          </div>
                          <div className="ml-3">
                            <div className="text-sm font-medium text-gray-900">
                              {dev.name}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {dev.totalTasks}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {dev.completedTasks}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {dev.totalHours}h
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="w-16 bg-gray-200 rounded-full h-2.5 mr-2">
                            <div
                              className="bg-blue-600 h-2.5 rounded-full"
                              style={{ width: `${dev.completionRate}%` }}
                            ></div>
                          </div>
                          <span className="text-sm text-gray-900">
                            {Math.round(dev.completionRate)}%
                          </span>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Gráfica 1: Horas Totales trabajadas por Sprint */}
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
            <h2 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
              <BarChart3 className="h-5 w-5 text-blue-500" />
              Total Hours Worked per Sprint
            </h2>
            <div className="h-60 sm:h-70 md:h-80">
              {hoursPerSprintData ? (
                <Bar
                  data={hoursPerSprintData}
                  options={{
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { position: 'bottom' } },
                    scales: { y: { title: { display: true, text: 'Horas' } } },
                  }}
                />
              ) : (
                <div className="flex justify-center items-center h-full text-gray-500">No data available</div>
              )}
            </div>
          </div>

          {/* Gráfica 2: Horas Trabajadas por Developer por Sprint */}
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
            <h2 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
              <BarChart3 className="h-5 w-5 text-blue-500" />
              Hours Worked per Developer per Sprint
            </h2>
            <div className="h-60 sm:h-70 md:h-80">
              {hoursByDeveloperPerSprint ? (
                <Bar
                  data={hoursByDeveloperPerSprint}
                  options={{
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { position: 'bottom' } },
                    scales: { y: { title: { display: true, text: 'Horas' } } },
                  }}
                />
              ) : (
                <div className="flex justify-center items-center h-full text-gray-500">No data available</div>
              )}
            </div>
          </div>

          {/* Gráfica 3: Tareas Completadas por Developer por Sprint */}
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
            <h2 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
              <BarChart3 className="h-5 w-5 text-blue-500" />
              Tasks Completed by Developer per Sprint
            </h2>
            <div className="h-60 sm:h-70 md:h-80">
              {completedTasksByDeveloperPerSprint ? (
                <Bar
                  data={completedTasksByDeveloperPerSprint}
                  options={{
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { position: 'bottom' } },
                    scales: { y: { title: { display: true, text: 'Tareas completadas' } } },
                  }}
                />
              ) : (
                <div className="flex justify-center items-center h-full text-gray-500">No data available</div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Sprint metrics view */}
      {selectedMetricView === "sprints" && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Team metrics per sprint */}
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                <Users className="h-5 w-5 text-blue-500" />
                Team Metrics per Sprint
              </h2>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Sprint
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Total Tasks
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Completed
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Hours Worked
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Completion Rate
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {teamMetricsPerSprint?.map((sprint) => (
                    <tr key={sprint.sprintId} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {sprint.sprintName}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {sprint.totalTasks}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {sprint.completedTasks}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {sprint.totalHours}h
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="w-16 bg-gray-200 rounded-full h-2.5 mr-2">
                            <div
                              className="bg-blue-600 h-2.5 rounded-full"
                              style={{ width: `${sprint.completionRate}%` }}
                            ></div>
                          </div>
                          <span className="text-sm text-gray-900">
                            {Math.round(sprint.completionRate)}%
                          </span>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
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
            <div className="h-60 sm:h-70 md:h-80">
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
                          boxWidth: isXs ? 8 : 12,
                          padding: isXs ? 10 : 15,
                          font: {
                            size: isXs ? 10 : 12,
                          },
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
                        const index = elements[0].index;
                        const sprintId =
                          hoursPerSprintData.sprintDetails[index].sprintId;
                        setSelectedSprint(sprintId);
                      }
                    },
                  }}
                />
              ) : (
                <div className="flex justify-center items-center h-full text-gray-500">
                  No data available
                </div>
              )}
            </div>
            <p className="text-sm text-gray-500 mt-2 text-center">
              Click on a bar to see task details
            </p>
          </div>

          {/* Sprint completion trend line chart */}
          {showTrends && (
            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                  <LineChart className="h-5 w-5 text-blue-500" />
                  Sprint Completion Trend
                </h2>
              </div>
              <div className="h-60 sm:h-70 md:h-80">
                {sprintCompletionTrendData ? (
                  <Line
                    data={sprintCompletionTrendData}
                    options={{
                      responsive: true,
                      maintainAspectRatio: false,
                      plugins: {
                        legend: {
                          position: "bottom",
                          labels: {
                            boxWidth: isXs ? 8 : 12,
                            padding: isXs ? 10 : 15,
                            font: {
                              size: isXs ? 10 : 12,
                            },
                          },
                        },
                        tooltip: {
                          backgroundColor: "rgba(50, 50, 50, 0.8)",
                          padding: 10,
                        },
                      },
                      scales: {
                        y: {
                          beginAtZero: true,
                          title: {
                            display: true,
                            text: "Completion Rate (%)",
                          },
                          max: 100,
                        },
                      },
                    }}
                  />
                ) : (
                  <div className="flex justify-center items-center h-full text-gray-500">
                    No data available
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Sprint task details panel */}
      {selectedSprintDetails && (
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
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

          <div className="overflow-x-auto -mx-6 px-6 sm:mx-0 sm:px-0">
            <table className="min-w-full divide-y divide-gray-200 table-fixed sm:table-auto">
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
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {task.description}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {task.estimateHours}h
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {task.realHours}h
                    </td>
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
                    {selectedSprintDetails.tasks.reduce(
                      (sum, task) => sum + task.estimateHours,
                      0,
                    )}
                    h
                  </td>
                  <td className="px-6 py-3 text-left text-xs font-medium text-gray-900">
                    {selectedSprintDetails.totalRealHours}h
                  </td>
                  <td
                    className={`px-6 py-3 text-left text-xs font-medium flex items-center
                    ${
                      selectedSprintDetails.totalRealHours >
                      selectedSprintDetails.tasks.reduce(
                        (sum, task) => sum + task.estimateHours,
                        0,
                      )
                        ? "text-red-600"
                        : "text-green-600"
                    }`}
                  >
                    {selectedSprintDetails.totalRealHours >
                    selectedSprintDetails.tasks.reduce(
                      (sum, task) => sum + task.estimateHours,
                      0,
                    ) ? (
                      <ArrowUp className="w-4 h-4 mr-1 text-red-600" />
                    ) : (
                      <ArrowDown className="w-4 h-4 mr-1 text-green-600" />
                    )}
                    {selectedSprintDetails.totalRealHours -
                      selectedSprintDetails.tasks.reduce(
                        (sum, task) => sum + task.estimateHours,
                        0,
                      )}
                    h
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>
      )}

      {/* Efficiency insights */}
      <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
        <div className="flex items-center justify-between mb-4">
          <div
            className="relative cursor-help"
            onMouseEnter={() => setHoveredCard("efficiencyInsights")}
            onMouseLeave={() => setHoveredCard(null)}
          >
            <h2 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
              <Zap className="h-5 w-5 text-blue-500" />
              Efficiency Insights
            </h2>
            {hoveredCard === "efficiencyInsights" && (
              <div className="fixed sm:absolute z-50 w-72 p-4 bg-gray-900 text-white text-xs sm:text-sm rounded-lg shadow-lg transform -translate-x-1/2 left-1/2 sm:translate-x-0 sm:left-0 top-20 sm:-top-2">
                Automatic analysis of your team's performance metrics,
                highlighting areas that need attention and celebrating
                achievements. These insights help you identify bottlenecks and
                improve team efficiency.
                <div className="absolute left-1/2 -ml-2 -bottom-2 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-gray-900 sm:left-4 sm:top-full"></div>
              </div>
            )}
          </div>
        </div>
        <div className="space-y-3 sm:space-y-4 text-sm sm:text-base">
          {/* Task Completion Rate Insight */}
          {summaryMetrics?.completionRate &&
            summaryMetrics.completionRate < 50 && (
              <div className="flex items-start gap-3 p-3 bg-yellow-50 rounded-lg border border-yellow-100">
                <AlertCircle
                  className="text-yellow-500 mt-0.5 flex-shrink-0"
                  size={18}
                />
                <div>
                  <p className="font-medium text-yellow-800">
                    Low Task Completion Rate
                  </p>
                  <p className="text-sm text-yellow-700 mt-1">
                    Only {summaryMetrics.completionRate}% of tasks are
                    completed. Consider reviewing sprint planning and task
                    allocation to improve completion rates.
                  </p>
                </div>
              </div>
            )}

          {/* Estimate Accuracy Warning */}
          {summaryMetrics?.estimateAccuracy &&
            (summaryMetrics.estimateAccuracy < 80 ||
              summaryMetrics.estimateAccuracy > 120) && (
              <div className="flex items-start gap-3 p-3 bg-red-50 rounded-lg border border-red-100">
                <AlertCircle
                  className="text-red-500 mt-0.5 flex-shrink-0"
                  size={18}
                />
                <div>
                  <p className="font-medium text-red-800">
                    Estimate Accuracy Issues
                  </p>
                  <p className="text-sm text-red-700 mt-1">
                    {summaryMetrics.estimateAccuracy > 100
                      ? `Tasks are taking ${summaryMetrics.estimateAccuracy - 100}% longer than estimated.`
                      : `Tasks are being completed ${100 - summaryMetrics.estimateAccuracy}% faster than estimated.`}{" "}
                    Consider adjusting your estimation process.
                  </p>
                </div>
              </div>
            )}

          {/* High Task Load Warning */}
          {tasks &&
            users &&
            tasksPerDeveloperData?.datasets[0].data.some(
              (count) => count > 8,
            ) && (
              <div className="flex items-start gap-3 p-3 bg-yellow-50 rounded-lg border border-yellow-100">
                <AlertCircle
                  className="text-yellow-500 mt-0.5 flex-shrink-0"
                  size={18}
                />
                <div>
                  <p className="font-medium text-yellow-800">
                    High Task Load Detected
                  </p>
                  <p className="text-sm text-yellow-700 mt-1">
                    Some team members have more than 8 tasks assigned. Consider
                    redistributing work to prevent burnout and maintain quality.
                  </p>
                </div>
              </div>
            )}

          {/* Positive: Good Sprint Distribution */}
          {summaryMetrics?.avgTasksPerSprint &&
            summaryMetrics.avgTasksPerSprint >= 3 &&
            summaryMetrics.avgTasksPerSprint <= 7 && (
              <div className="flex items-start gap-3 p-3 bg-green-50 rounded-lg border border-green-100">
                <CheckCircle2
                  className="text-green-500 mt-0.5 flex-shrink-0"
                  size={18}
                />
                <div>
                  <p className="font-medium text-green-800">
                    Optimal Sprint Task Distribution
                  </p>
                  <p className="text-sm text-green-700 mt-1">
                    Average of {summaryMetrics.avgTasksPerSprint} tasks per
                    sprint indicates a well-balanced workload distribution.
                  </p>
                </div>
              </div>
            )}

          {/* Positive: Good Team Balance */}
          {tasksPerDeveloperData?.datasets?.[0]?.data &&
            tasksPerDeveloperData.datasets[0].data.length > 0 &&
            Math.max(...tasksPerDeveloperData.datasets[0].data) /
              Math.min(...tasksPerDeveloperData.datasets[0].data) <
              2 && (
              <div className="flex items-start gap-3 p-3 bg-green-50 rounded-lg border border-green-100">
                <CheckCircle2
                  className="text-green-500 mt-0.5 flex-shrink-0"
                  size={18}
                />
                <div>
                  <p className="font-medium text-green-800">
                    Balanced Team Workload
                  </p>
                  <p className="text-sm text-green-700 mt-1">
                    Task distribution among team members is well balanced, with
                    no significant overload on any individual.
                  </p>
                </div>
              </div>
            )}

          {/* Perfect Performance */}
          {(!summaryMetrics?.tasksWithoutEstimates ||
            summaryMetrics.tasksWithoutEstimates === 0) &&
            (!summaryMetrics?.tasksWithoutAssignees ||
              summaryMetrics.tasksWithoutAssignees === 0) &&
            summaryMetrics?.completionRate &&
            summaryMetrics.completionRate >= 80 &&
            summaryMetrics?.estimateAccuracy &&
            summaryMetrics.estimateAccuracy >= 90 &&
            summaryMetrics.estimateAccuracy <= 110 && (
              <div className="flex items-start gap-3 p-3 bg-green-50 rounded-lg border border-green-100">
                <CheckCircle2
                  className="text-green-500 mt-0.5 flex-shrink-0"
                  size={18}
                />
                <div>
                  <p className="font-medium text-green-800">
                    Excellent Team Performance
                  </p>
                  <p className="text-sm text-green-700 mt-1">
                    Your team is performing exceptionally well with a{" "}
                    {summaryMetrics.completionRate}% completion rate and{" "}
                    {Math.abs(summaryMetrics.estimateAccuracy - 100)}% estimate
                    accuracy. All tasks are properly assigned and estimated.
                  </p>
                </div>
              </div>
            )}
        </div>
      </div>
    </div>
  );
};

export default KPIs;
