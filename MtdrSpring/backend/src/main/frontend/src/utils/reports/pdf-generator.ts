import jsPDF from "jspdf";
import type { TaskResponse, UserResponse } from "../api/client";

interface ReportData {
  users: UserResponse[];
  tasks: TaskResponse[];
  date: string;
  sprints: { id: number; name: string }[];
}

interface MetricItem {
  label: string;
  value: string | number;
  color: string;
}

interface ChartDataItem {
  label: string;
  value: number;
  color: string;
}

export async function generateTaskReport(data: ReportData) {
  // Initialize PDF document
  const pdf = new jsPDF("p", "mm", "a4");
  const pageWidth = pdf.internal.pageSize.width;
  const pageCenter = pageWidth / 2;
  let yPos = 20;

  // Color palette
  const colors = {
    primary: "#3b82f6", // Blue
    secondary: "#8b5cf6", // Purple
    success: "#22c55e", // Green
    warning: "#eab308", // Yellow
    danger: "#ef4444", // Red
    info: "#06b6d4", // Cyan
    muted: "#94a3b8", // Slate
    light: "#f8fafc", // Slate 50
    dark: "#1e293b", // Slate 800
    bgLight: "#f1f5f9", // Slate 100
    bgAccent: "#dbeafe", // Blue 100
  };

  // Calculate summary metrics
  const totalTasks = data.tasks.length;
  const completedTasks = data.tasks.filter((t) => t.status === "done").length;
  const completionRate =
    totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;
  const totalDevelopers = data.users.filter(
    (u) => u.role === "developer",
  ).length;
  const tasksWithoutEstimates = data.tasks.filter(
    (t) => t.estimateHours === null,
  ).length;
  const tasksWithoutAssignees = data.tasks.filter(
    (t) => t.assignedToId === null,
  ).length;
  const avgTasksPerSprint =
    data.sprints.length > 0 ? Math.round(totalTasks / data.sprints.length) : 0;

  const tasksWithBothHours = data.tasks.filter(
    (t) => t.status === "done" && t.estimateHours && t.realHours,
  );

  const estimateAccuracy =
    tasksWithBothHours.length > 0
      ? Math.round(
          (tasksWithBothHours.reduce((sum, t) => sum + (t.realHours || 0), 0) /
            tasksWithBothHours.reduce(
              (sum, t) => sum + (t.estimateHours || 0),
              0,
            )) *
            100,
        )
      : 100;

  // ===== HELPER FUNCTIONS =====

  // Draw a section container with title
  function drawSection(title: string, y: number, height: number) {
    // Container with rounded corners
    pdf.setFillColor(colors.bgLight);
    pdf.roundedRect(15, y - 10, pageWidth - 30, height, 3, 3, "F");

    // Accent bar on left side
    pdf.setFillColor(colors.bgAccent);
    pdf.rect(15, y - 10, 5, height, "F");

    // Section title
    pdf.setFontSize(18);
    pdf.setTextColor(colors.primary);
    pdf.text(title, pageCenter, y, { align: "center" });

    return y + 15;
  }

  // Draw a circular progress chart - FIXED
  function drawProgressCircle(
    x: number,
    y: number,
    radius: number,
    progress: number,
    color: string,
  ) {
    // Background circle
    pdf.setDrawColor(220, 220, 220);
    pdf.setFillColor(240, 240, 240);
    pdf.circle(x, y, radius, "F");

    // Progress arc - FIXED to use simpler approach with multiple small lines
    if (progress > 0) {
      pdf.setDrawColor(color);
      pdf.setFillColor(color);

      // Draw the progress as a series of small lines to form an arc
      const segments = 36; // Number of segments to make the arc smooth
      const totalAngle = (progress / 100) * 360;
      const angleStep = totalAngle / segments;

      // Start from -90 degrees (top of circle)
      let currentAngle = -90;

      // Create a polygon to fill
      const points = [];

      // Add center point
      points.push([x, y]);

      // Add starting point (top of circle)
      points.push([
        x + radius * Math.cos((currentAngle * Math.PI) / 180),
        y + radius * Math.sin((currentAngle * Math.PI) / 180),
      ]);

      // Add points along the arc
      for (let i = 0; i <= segments; i++) {
        currentAngle = -90 + i * angleStep;
        points.push([
          x + radius * Math.cos((currentAngle * Math.PI) / 180),
          y + radius * Math.sin((currentAngle * Math.PI) / 180),
        ]);
      }

      // Draw the filled polygon
      pdf.setFillColor(color);
      // Removed unnecessary beginPath call
      points.forEach(([px, py], index) => {
        if (index === 0) {
          pdf.moveTo(px, py);
        } else {
          pdf.lineTo(px, py);
        }
      });
      // Removed unnecessary closePath call
      pdf.fill();

      // Inner white circle
      pdf.setFillColor(255, 255, 255);
      pdf.circle(x, y, radius * 0.8, "F");
    }

    // Percentage text
    pdf.setTextColor(0, 0, 0);
    pdf.setFontSize(12);
    pdf.text(`${progress}%`, x, y + 4, { align: "center" });
  }

  // Draw a bar chart
  function drawBarChart(
    x: number,
    y: number,
    data: ChartDataItem[],
    maxValue: number,
  ) {
    const barHeight = 6;
    const barSpacing = 15;
    const barWidth = 60;

    data.forEach((item, index) => {
      const yPosition = y + index * barSpacing;

      // Bar background
      pdf.setFillColor(240, 240, 240);
      pdf.rect(x, yPosition, barWidth, barHeight, "F");

      // Actual bar
      pdf.setFillColor(item.color);
      const width = (item.value / maxValue) * barWidth;
      pdf.rect(x, yPosition, width, barHeight, "F");

      // Label and value
      pdf.setTextColor(80, 80, 80);
      pdf.setFontSize(8);
      pdf.text(item.label, x, yPosition - 2);
      pdf.text(item.value.toString(), x + barWidth + 5, yPosition + 4);
    });
  }

  // Draw a metrics grid - FIXED
  function drawMetricsGrid(
    x: number,
    y: number,
    metrics: MetricItem[],
    cols = 3,
  ) {
    const colWidth = (pageWidth - 60) / cols;

    metrics.forEach((metric, index) => {
      const col = index % cols;
      const row = Math.floor(index / cols);
      const xPos = x + col * colWidth;
      const yOffset = y + row * 25;

      // Metric box with colored accent - FIXED to ensure accent bar covers full height
      pdf.setFillColor(250, 250, 250);
      pdf.roundedRect(xPos, yOffset - 5, colWidth - 10, 20, 2, 2, "F");

      // Colored accent bar - now covers the full height
      pdf.setFillColor(metric.color);
      pdf.rect(xPos, yOffset - 5, 3, 20, "F");

      // Metric label
      pdf.setFontSize(8);
      pdf.setTextColor(100, 100, 100);
      pdf.text(metric.label, xPos + 7, yOffset + 2);

      // Metric value
      pdf.setFontSize(12);
      pdf.setTextColor(0, 0, 0);
      pdf.text(metric.value.toString(), xPos + 6, yOffset + 10);
    });
  }

  // ===== DOCUMENT HEADER =====

  // Header with gradient background
  const gradientColors = {
    start: [240, 249, 255], // Light blue
    end: [219, 234, 254], // Slightly darker blue
  };

  for (let i = 0; i < 45; i++) {
    const ratio = i / 45;
    const r = Math.floor(
      gradientColors.start[0] * (1 - ratio) + gradientColors.end[0] * ratio,
    );
    const g = Math.floor(
      gradientColors.start[1] * (1 - ratio) + gradientColors.end[1] * ratio,
    );
    const b = Math.floor(
      gradientColors.start[2] * (1 - ratio) + gradientColors.end[2] * ratio,
    );

    pdf.setFillColor(r, g, b);
    pdf.rect(0, i, pageWidth, 1, "F");
  }

  // Title and date
  pdf.setFontSize(24);
  pdf.setTextColor(colors.primary);
  pdf.text("Project Performance Report", pageCenter, yPos, { align: "center" });

  pdf.setFontSize(12);
  pdf.setTextColor(100, 100, 100);
  pdf.text(`Generated on: ${data.date}`, pageCenter, yPos + 10, {
    align: "center",
  });

  yPos += 50;

  // ===== OVERALL SUMMARY SECTION =====

  const summaryHeight = 110;
  yPos = drawSection("Overall Summary", yPos, summaryHeight);

  // Center completion rate circle with label
  const circleX = pageCenter;
  const circleY = yPos + 10;

  pdf.setFontSize(14);
  pdf.setTextColor(100, 100, 100);
  pdf.text("Completion Rate", circleX, yPos - 7, { align: "center" });

  // Draw colored completion circle
  const circleColor =
    completionRate >= 75
      ? colors.success
      : completionRate >= 50
        ? colors.warning
        : colors.danger;
  drawProgressCircle(circleX, circleY, 15, completionRate, circleColor);

  yPos += 40;

  // Metrics grid
  const allMetrics: MetricItem[] = [
    { label: "Total Tasks", value: totalTasks, color: colors.primary },
    { label: "Team Size", value: totalDevelopers, color: colors.secondary },
    { label: "Avg Tasks/Sprint", value: avgTasksPerSprint, color: colors.info },
    {
      label: "Missing Estimates",
      value: tasksWithoutEstimates,
      color: colors.warning,
    },
    {
      label: "Unassigned Tasks",
      value: tasksWithoutAssignees,
      color: colors.danger,
    },
    {
      label: "Estimate Accuracy",
      value: `${estimateAccuracy}%`,
      color: colors.success,
    },
  ];

  drawMetricsGrid(30, yPos, allMetrics);

  yPos += 70;

  // ===== TASK STATISTICS SECTION =====

  const statsHeight = 100;
  yPos = drawSection("Task Statistics", yPos, statsHeight);

  // Status counts
  const statusCounts = {
    created: data.tasks.filter((t) => t.status === "created").length,
    "in-progress": data.tasks.filter((t) => t.status === "in-progress").length,
    "in-review": data.tasks.filter((t) => t.status === "in-review").length,
    testing: data.tasks.filter((t) => t.status === "testing").length,
    done: completedTasks,
  };

  // Category counts
  const categoryCounts = {
    bug: data.tasks.filter((t) => t.category === "bug").length,
    feature: data.tasks.filter((t) => t.category === "feature").length,
    issue: data.tasks.filter((t) => t.category === "issue").length,
    uncategorized: data.tasks.filter((t) => !t.category).length,
  };

  // Status distribution chart data
  const statusData: ChartDataItem[] = [
    { label: "Created", value: statusCounts.created, color: colors.muted },
    {
      label: "In Progress",
      value: statusCounts["in-progress"],
      color: colors.warning,
    },
    {
      label: "In Review",
      value: statusCounts["in-review"],
      color: colors.primary,
    },
    { label: "Testing", value: statusCounts.testing, color: colors.secondary },
    { label: "Done", value: statusCounts.done, color: colors.success },
  ];

  // Category distribution chart data
  const categoryData: ChartDataItem[] = [
    { label: "Bug", value: categoryCounts.bug, color: colors.danger },
    { label: "Feature", value: categoryCounts.feature, color: colors.primary },
    { label: "Issue", value: categoryCounts.issue, color: colors.warning },
    {
      label: "Uncategorized",
      value: categoryCounts.uncategorized,
      color: colors.muted,
    },
  ];

  // Draw both charts side by side
  const leftChartX = 30;
  const rightChartX = pageWidth / 2 + 15;

  // Add labels for each chart
  pdf.setFontSize(12);
  pdf.setTextColor(80, 80, 80);
  pdf.text("Tasks by Status", leftChartX, yPos - 5);
  pdf.text("Tasks by Category", rightChartX, yPos - 5);

  // Draw the charts
  const maxStatusTasks = Math.max(...Object.values(statusCounts), 1);
  const maxCategoryTasks = Math.max(...Object.values(categoryCounts), 1);

  drawBarChart(leftChartX, yPos, statusData, maxStatusTasks);
  drawBarChart(rightChartX, yPos, categoryData, maxCategoryTasks);

  yPos += 100;

  // ===== TEAM METRICS PER SPRINT =====

  pdf.addPage();
  yPos = 20;

  const sprintTableHeight = 160;
  yPos = drawSection("Team Metrics per Sprint", yPos, sprintTableHeight);

  // Table container
  const tableStartX = 25;
  const tableWidth = pageWidth - 50;

  // Table header with background
  pdf.setFillColor(230, 230, 230);
  pdf.rect(tableStartX, yPos - 5, tableWidth, 8, "F");

  // Draw headers
  const headers = [
    "Sprint",
    "Total Tasks",
    "Completed",
    "Hours Worked",
    "Completion Rate",
  ];
  const colWidths = [
    tableWidth * 0.35,
    tableWidth * 0.15,
    tableWidth * 0.15,
    tableWidth * 0.15,
    tableWidth * 0.2,
  ];

  pdf.setFontSize(10);
  pdf.setTextColor(80, 80, 80);
  headers.forEach((header, i) => {
    let xPos = tableStartX;
    for (let j = 0; j < i; j++) xPos += colWidths[j];
    pdf.text(header, xPos + 5, yPos);
  });
  yPos += 8;

  // Draw table content
  pdf.setFontSize(9);
  const sprintMetrics = data.sprints.reduce(
    (acc, sprint) => {
      const sprintTasks = data.tasks.filter(
        (task) => task.sprintId === sprint.id,
      );
      acc[sprint.id] = {
        totalTasks: sprintTasks.length,
        completed: sprintTasks.filter((task) => task.status === "done").length,
        hoursWorked: sprintTasks.reduce(
          (sum, task) => sum + (task.realHours || 0),
          0,
        ),
      };
      return acc;
    },
    {} as Record<
      number,
      { totalTasks: number; completed: number; hoursWorked: number }
    >,
  );

  Object.entries(sprintMetrics).forEach(([sprintId, metrics], index) => {
    if (yPos > 270) {
      pdf.addPage();
      yPos = 20;
    }

    const sprint = data.sprints.find((s) => s.id === Number(sprintId));
    if (!sprint) return;

    // Alternating row background
    if (index % 2 === 0) {
      pdf.setFillColor(250, 250, 250);
      pdf.rect(tableStartX, yPos - 5, tableWidth, 7, "F");
    }

    let xPos = tableStartX;

    // Sprint name
    pdf.setTextColor(0, 0, 0);
    pdf.text(sprint.name, xPos + 5, yPos);

    // Total tasks
    xPos += colWidths[0];
    pdf.text(metrics.totalTasks.toString(), xPos + 5, yPos);

    // Completed tasks
    xPos += colWidths[1];
    pdf.text(metrics.completed.toString(), xPos + 5, yPos);

    // Hours worked
    xPos += colWidths[2];
    pdf.text(`${metrics.hoursWorked}h`, xPos + 5, yPos);

    // Completion rate
    xPos += colWidths[3];
    const completionRate =
      metrics.totalTasks > 0
        ? Math.round((metrics.completed / metrics.totalTasks) * 100)
        : 0;

    // Set color based on completion rate
    if (completionRate >= 80) {
      pdf.setTextColor(22, 163, 74); // Dark green
    } else if (completionRate >= 50) {
      pdf.setTextColor(234, 179, 8); // Dark yellow
    } else {
      pdf.setTextColor(239, 68, 68); // Dark red
    }
    pdf.text(`${completionRate}%`, xPos + 5, yPos);

    yPos += 7;
  });

  // ===== DETAILED USER TASK BREAKDOWN =====

  // Helper function to calculate estimate accuracy
  function calculateEstimateAccuracy(tasks: TaskResponse[]) {
    const completedTasks = tasks.filter(
      (t) => t.status === "done" && t.estimateHours && t.realHours,
    );
    if (completedTasks.length === 0) return 100;

    const totalEstimated = completedTasks.reduce(
      (sum, t) => sum + (t.estimateHours || 0),
      0,
    );
    const totalReal = completedTasks.reduce(
      (sum, t) => sum + (t.realHours || 0),
      0,
    );
    return Math.round((totalReal / totalEstimated) * 100);
  }

  // Group tasks by user
  const tasksByUser = data.users
    .filter((user) => user.role === "developer")
    .map((user) => {
      const userTasks = data.tasks.filter(
        (task) => task.assignedToId === user.id,
      );
      return {
        user,
        tasks: userTasks,
        metrics: {
          total: userTasks.length,
          completed: userTasks.filter((t) => t.status === "done").length,
          totalHours: userTasks.reduce((sum, t) => sum + (t.realHours || 0), 0),
          inProgress: userTasks.filter((t) => t.status === "in-progress")
            .length,
          estimateAccuracy: calculateEstimateAccuracy(userTasks),
        },
      };
    });

  // Generate detailed report for each user
  for (const [, userData] of tasksByUser.entries()) {
    // Add new page for each user
    pdf.addPage();
    yPos = 20;

    // User section with enhanced styling
    const userSectionHeight = 60; // Increased height to accommodate metrics
    pdf.setFillColor(colors.bgLight);
    pdf.roundedRect(15, yPos - 5, pageWidth - 30, userSectionHeight, 3, 3, "F");

    // Accent bar - covers the full height
    pdf.setFillColor(colors.bgAccent);
    pdf.rect(15, yPos - 5, 5, userSectionHeight, "F");

    // User name
    pdf.setFontSize(16);
    pdf.setTextColor(colors.primary);
    pdf.text(
      `${userData.user.firstName} ${userData.user.lastName}`,
      pageCenter,
      yPos,
      { align: "center" },
    );

    // Role/title
    pdf.setFontSize(10);
    pdf.setTextColor(100, 100, 100);
    pdf.text(userData.user.title || "Developer", pageCenter, yPos + 7, {
      align: "center",
    });

    // User metrics
    const userMetrics: MetricItem[] = [
      {
        label: "Total Tasks",
        value: userData.metrics.total,
        color: colors.primary,
      },
      {
        label: "Completed",
        value: userData.metrics.completed,
        color: colors.success,
      },
      {
        label: "In Progress",
        value: userData.metrics.inProgress,
        color: colors.warning,
      },
      {
        label: "Total Hours",
        value: `${userData.metrics.totalHours}h`,
        color: colors.info,
      },
      {
        label: "Completion Rate",
        value:
          userData.metrics.total > 0
            ? `${Math.round((userData.metrics.completed / userData.metrics.total) * 100)}%`
            : "0%",
        color: colors.secondary,
      },
      {
        label: "Estimate Accuracy",
        value: `${userData.metrics.estimateAccuracy}%`,
        color: colors.muted,
      },
    ];

    // Center the metrics grid
    const metricsStartX = 30;
    drawMetricsGrid(metricsStartX, yPos + 15, userMetrics, 3);

    // Increase yPos to ensure metrics and table don't overlap
    yPos += userSectionHeight + 10;

    // Tasks table
    if (userData.tasks.length > 0) {
      const tableStartY = yPos;

      // Ajustar los anchos de las columnas para que quepan correctamente en la página
      const headers = [
        "Description",
        "Status",
        "Est. Hours",
        "Real Hours",
        "Diff",
      ];
      // Distribuir mejor el ancho de las columnas
      const headerWidths = [
        pageWidth * 0.4,
        pageWidth * 0.15,
        pageWidth * 0.15,
        pageWidth * 0.15,
        pageWidth * 0.15,
      ];
      const tableWidth = pageWidth - 30;

      // Centrar la tabla en la página
      const tableStartX = 15;

      // Fondo de la cabecera de la tabla
      pdf.setFillColor(230, 230, 230);
      pdf.rect(tableStartX, tableStartY - 5, tableWidth, 8, "F");

      pdf.setFontSize(10);
      pdf.setTextColor(80, 80, 80);

      // Dibujar los textos de la cabecera
      let xPos = tableStartX;
      headers.forEach((header, i) => {
        pdf.text(header, xPos + 5, tableStartY);
        xPos += headerWidths[i];
      });

      yPos = tableStartY + 8;

      // Contenido de la tabla con colores alternados
      pdf.setFontSize(9);
      userData.tasks.forEach((task, index) => {
        if (yPos > 270) {
          pdf.addPage();
          yPos = 20;

          // Redibuja la cabecera en la nueva página
          pdf.setFillColor(230, 230, 230);
          pdf.rect(tableStartX, yPos - 5, tableWidth, 8, "F");

          let headerX = tableStartX;
          pdf.setFontSize(10);
          pdf.setTextColor(80, 80, 80);
          headers.forEach((header, i) => {
            pdf.text(header, headerX + 5, yPos);
            headerX += headerWidths[i];
          });

          yPos += 8;
        }

        // Fondo alternado para las filas
        if (index % 2 === 0) {
          pdf.setFillColor(245, 245, 245);
          pdf.rect(tableStartX, yPos - 5, tableWidth, 7, "F");
        }

        xPos = tableStartX;
        pdf.setTextColor(0, 0, 0);

        // Truncar descripción si es demasiado larga
        const maxChars = Math.floor(headerWidths[0] / 2);
        const description =
          task.description.length > maxChars
            ? task.description.substring(0, maxChars - 3) + "..."
            : task.description;
        pdf.text(description, xPos + 5, yPos);

        xPos += headerWidths[0];
        const statusColor = getStatusColor(task.status);
        pdf.setTextColor(statusColor.r, statusColor.g, statusColor.b);
        pdf.text(task.status, xPos + 5, yPos);

        xPos += headerWidths[1];
        pdf.setTextColor(0, 0, 0);
        pdf.text(task.estimateHours?.toString() || "-", xPos + 5, yPos);

        xPos += headerWidths[2];
        pdf.text(task.realHours?.toString() || "-", xPos + 5, yPos);

        xPos += headerWidths[3];
        if (task.estimateHours && task.realHours) {
          const diff = task.realHours - task.estimateHours;
          pdf.setTextColor(
            diff > 0 ? 220 : 22,
            diff > 0 ? 38 : 163,
            diff > 0 ? 38 : 74,
          );
          pdf.text(diff !== 0 ? diff.toString() : "-", xPos + 5, yPos);
        } else {
          pdf.setTextColor(100, 100, 100);
          pdf.text("-", xPos + 5, yPos);
        }

        yPos += 7;
      });
    }
  }

  // Add footer to all pages
  const totalPages = pdf.getNumberOfPages();
  for (let i = 1; i <= totalPages; i++) {
    pdf.setPage(i);
    pdf.setFontSize(8);
    pdf.setTextColor(150, 150, 150);
    pdf.text(
      `Page ${i} of ${totalPages}`,
      pageWidth - 20,
      pdf.internal.pageSize.height - 10,
      { align: "right" },
    );

    // Add company logo or name in footer
    pdf.text(
      "Project Performance Report",
      20,
      pdf.internal.pageSize.height - 10,
    );
  }

  return pdf;
}

// Helper function for status colors
function getStatusColor(status: string): { r: number; g: number; b: number } {
  switch (status) {
    case "done":
      return { r: 22, g: 163, b: 74 }; // green
    case "in-progress":
      return { r: 234, g: 179, b: 8 }; // yellow
    case "in-review":
      return { r: 37, g: 99, b: 235 }; // blue
    case "testing":
      return { r: 147, g: 51, b: 234 }; // purple
    default:
      return { r: 100, g: 100, b: 100 }; // gray
  }
}
