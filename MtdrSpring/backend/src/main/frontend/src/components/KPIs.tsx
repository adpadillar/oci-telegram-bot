import { useState, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "../utils/api/client";
import { Loader2 } from "lucide-react";
import { Pie, Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
} from "chart.js";

// Register the chart components
ChartJS.register(
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement,
  Title
);

const KPIs = () => {
    const [selectedSprint, setSelectedSprint] = useState<number | null>(null);
  // Use getDevelopers to fetch the complete list of developers (consistent with Developers.tsx)
  const { data: users, isLoading: usersLoading } = useQuery({
    queryKey: ["users"],
    queryFn: api.users.getDevelopers,
  });

  const { data: tasks, isLoading: tasksLoading } = useQuery({
    queryKey: ["tasks"],
    queryFn: api.tasks.list,
  });

  const { data: sprints, isLoading: sprintsLoading } = useQuery({
    queryKey: ["sprints"],
    queryFn: api.sprints.getSprints,
  });

  // Generate data for the tasks per DEVELOPER pie chart
  const tasksPerDeveloperData = useMemo(() => {
    if (!users || !tasks) return null;

    // Get developers
    const developers = users.filter(user => user.role === "developer");
    
    // Count tasks per developer
    const taskCounts = developers.reduce((acc, dev) => {
       // Ensure both sides are numbers
    const count = tasks.filter(task => Number(task.assignedToId) === Number(dev.id)).length;
      if (count > 0) {
        acc.labels.push(`${dev.firstName} ${dev.lastName}`);
        acc.data.push(count);
        
        // Generate random colors for each developer
        const r = Math.floor(Math.random() * 200);
        const g = Math.floor(Math.random() * 200);
        const b = Math.floor(Math.random() * 200);
        acc.backgroundColor.push(`rgba(${r}, ${g}, ${b}, 0.6)`);
        acc.borderColor.push(`rgba(${r}, ${g}, ${b}, 1)`);
      }
      return acc;
    }, {
      labels: [] as string[],
      data: [] as number[],
      backgroundColor: [] as string[],
      borderColor: [] as string[]
    });

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

    const sprintHours = sprints.map(sprint => {
      // Get completed tasks for this sprint that have realHours
      const sprintTasks = tasks.filter(
        task => task.sprintId === sprint.id && task.status === "done"
      );

      // Sum the real hours
      const totalRealHours = sprintTasks.reduce(
        (sum, task) => sum + (task.realHours || 0),
        0
      );

      // Store estimated (actually, estimated as "estimateHours") hours for each task to display on click
      const taskHours = sprintTasks.map(task => ({
        taskId: task.id,
        description: task.description,
        realHours: task.realHours || 0,
        estimateHours: task.estimateHours || 0
      }));

      return {
        sprintId: sprint.id,
        sprintName: sprint.name,
        totalRealHours,
        tasks: taskHours
      };
    });

    return {
      labels: sprintHours.map(s => s.sprintName),
      datasets: [
        {
          label: "Real Hours Spent per Sprint",
          data: sprintHours.map(s => s.totalRealHours),
          backgroundColor: "rgba(54, 162, 235, 0.6)",
          borderColor: "rgba(54, 162, 235, 1)",
          borderWidth: 1,
        },
      ],
      sprintDetails: sprintHours,
    };
  }, [tasks, sprints]);

  // Get details of selected sprint to show estimated vs real hours
  const selectedSprintDetails = useMemo(() => {
    if (!selectedSprint || !hoursPerSprintData) return null;
    
    return hoursPerSprintData.sprintDetails.find(
      s => s.sprintId === selectedSprint
    );
  }, [selectedSprint, hoursPerSprintData]);

  // Loading state
  if (usersLoading || tasksLoading || sprintsLoading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <Loader2 className="w-8 h-8 animate-spin" />
      </div>
    );
  }

  // Error state
  if (!users || !tasks || !sprints) {
    return (
      <div className="flex justify-center items-center h-screen text-red-600">
        Failed to load data for KPIs
      </div>
    );
  }

  return (
    <div className="p-6 bg-gray-50">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Tasks per developer pie chart */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold mb-4">Tasks per Developer</h2>
          <div className="h-80">
            {tasksPerDeveloperData ? (
              <Pie data={tasksPerDeveloperData} />
            ) : (
              <div className="flex justify-center items-center h-full text-gray-500">
                No data available
              </div>
            )}
          </div>
        </div>

        {/* Hours per sprint bar chart */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold mb-4">Real Hours per Sprint</h2>
          <div className="h-80">
            {hoursPerSprintData ? (
              <Bar 
                data={hoursPerSprintData}
                options={{
                  responsive: true,
                  onClick: (_, elements) => {
                    if (elements.length > 0) {
                      const index = elements[0].index;
                      const sprintId = hoursPerSprintData.sprintDetails[index].sprintId;
                      setSelectedSprint(sprintId);
                    }
                  }
                }}
              />
            ) : (
              <div className="flex justify-center items-center h-full text-gray-500">
                No data available
              </div>
            )}
          </div>
          <p className="text-sm text-gray-500 mt-2">
            Click on a bar to see task details
          </p>
        </div>
      </div>

      {/* Sprint task details panel */}
      {selectedSprintDetails && (
        <div className="mt-6 bg-white p-6 rounded-lg shadow-md">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold">
              Tasks in {selectedSprintDetails.sprintName}
            </h2>
            <button 
              className="text-sm text-gray-500 hover:text-gray-700"
              onClick={() => setSelectedSprint(null)}
            >
              Close
            </button>
          </div>
          
          <div className="overflow-x-auto">
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
                {selectedSprintDetails.tasks.map(task => (
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
                      {task.estimateHours}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {task.realHours}
                    </td>
                    <td className={`px-6 py-4 whitespace-nowrap text-sm font-medium
                      ${task.realHours > task.estimateHours 
                        ? "text-red-600" 
                        : task.realHours < task.estimateHours 
                          ? "text-green-600" 
                          : "text-gray-900"}`
                    }>
                      {task.realHours - task.estimateHours}
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot className="bg-gray-50">
                <tr>
                  <td className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Total
                  </td>
                  <td className="px-6 py-3 text-left text-xs font-medium text-gray-900">
                    {selectedSprintDetails.tasks.reduce((sum, task) => sum + task.estimateHours, 0)}
                  </td>
                  <td className="px-6 py-3 text-left text-xs font-medium text-gray-900">
                    {selectedSprintDetails.totalRealHours}
                  </td>
                  <td className={`px-6 py-3 text-left text-xs font-medium
                    ${selectedSprintDetails.totalRealHours > selectedSprintDetails.tasks.reduce((sum, task) => sum + task.estimateHours, 0)
                      ? "text-red-600" 
                      : "text-green-600"}`
                  }>
                    {selectedSprintDetails.totalRealHours - 
                      selectedSprintDetails.tasks.reduce((sum, task) => sum + task.estimateHours, 0)}
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>
      )}

      {/* Summary cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-6">
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h3 className="text-lg font-medium text-gray-900">Total Tasks</h3>
          <p className="text-3xl font-bold mt-2">{tasks.length}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h3 className="text-lg font-medium text-gray-900">Completed Tasks</h3>
          <p className="text-3xl font-bold mt-2">
            {tasks.filter(task => task.status === "done").length}
          </p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h3 className="text-lg font-medium text-gray-900">Active Sprints</h3>
          <p className="text-3xl font-bold mt-2">
            {sprints.filter(sprint => 
              new Date(sprint.endsAt) >= new Date()
            ).length}
          </p>
        </div>
      </div>
    </div>
  );
};

export default KPIs;