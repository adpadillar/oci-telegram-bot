import jsPDF from 'jspdf';
import { TaskResponse, UserResponse } from '../api/client';

interface ReportData {
  users: UserResponse[];
  tasks: TaskResponse[];
  date: string;
  sprints: { id: number; name: string }[]; // Added sprints property
}

export async function generateTaskReport(data: ReportData) {
  const pdf = new jsPDF('p', 'mm', 'a4');
  const pageWidth = pdf.internal.pageSize.width;
  const pageCenter = pageWidth / 2;
  let yPos = 20;

  // Enhanced header with background
  pdf.setFillColor(240, 247, 255);
  pdf.rect(0, 0, pageWidth, 45, 'F');
  
  pdf.setFontSize(24);
  pdf.setTextColor(0, 0, 255);
  pdf.text('Project Performance Report', pageCenter, yPos, { align: 'center' });
  pdf.setFontSize(12);
  pdf.setTextColor(100, 100, 100);
  pdf.text(`Generated on: ${data.date}`, pageCenter, yPos + 10, { align: 'center' });

  yPos += 50;

  // Calculate summary metrics
  const totalTasks = data.tasks.length;
  const completedTasks = data.tasks.filter(t => t.status === 'done').length;
  const completionRate = Math.round((completedTasks / totalTasks) * 100);
  const totalDevelopers = data.users.filter(u => u.role === 'developer').length;
  const tasksWithoutEstimates = data.tasks.filter(t => t.estimateHours === null).length;
  const tasksWithoutAssignees = data.tasks.filter(t => t.assignedToId === null).length;

  // Additional metrics calculations
  const avgTasksPerSprint = Math.round(totalTasks / data.tasks.length) || 0;
  const tasksWithBothHours = data.tasks.filter(t => t.status === 'done' && t.estimateHours && t.realHours);
  const estimateAccuracy = tasksWithBothHours.length > 0
    ? Math.round((tasksWithBothHours.reduce((sum, t) => sum + (t.realHours || 0), 0) /
        tasksWithBothHours.reduce((sum, t) => sum + (t.estimateHours || 0), 0)) * 100)
    : 100;

  // Helper function to draw a circular progress chart
  function drawProgressCircle(x: number, y: number, radius: number, progress: number, color: string) {
    // Draw background circle
    pdf.setDrawColor(220, 220, 220);
    pdf.setFillColor(240, 240, 240);
    pdf.circle(x, y, radius, 'F');
    
    // Draw progress arc
    if (progress > 0) {
      pdf.setDrawColor(color);
      pdf.setFillColor(color);
      
      // Convert degrees to radians
      const startAngleRad = -Math.PI / 2;  // -90 degrees
      const endAngleRad = (progress * 2 * Math.PI / 100) - Math.PI / 2;
      
      // Draw the arc path manually
      pdf.path([
        ['M', x, y],
        ['L', x + radius * Math.cos(startAngleRad), y + radius * Math.sin(startAngleRad)],
        ['A', radius, radius, 0, progress > 50 ? 1 : 0, 1, x + radius * Math.cos(endAngleRad), y + radius * Math.sin(endAngleRad)],
        ['L', x, y]
      ], 'F');

      // Draw inner white circle
      pdf.setFillColor(255, 255, 255);
      pdf.circle(x, y, radius * 0.8, 'F');
    }
    
    // Add percentage text
    pdf.setTextColor(0, 0, 0);
    pdf.setFontSize(12);
    pdf.text(`${progress}%`, x, y, { align: 'center' });
  }

  // Draw bar chart for task distribution
  function drawBarChart(x: number, y: number, data: { label: string; value: number; color: string }[], maxValue: number) {
    const barHeight = 6;
    const barSpacing = 15;
    const barWidth = 60;
    
    data.forEach((item, index) => {
      const yPosition = y + (index * barSpacing);
      
      // Draw bar background
      pdf.setFillColor(240, 240, 240);
      pdf.rect(x, yPosition, barWidth, barHeight, 'F');
      
      // Draw actual bar
      pdf.setFillColor(item.color);
      const width = (item.value / maxValue) * barWidth;
      pdf.rect(x, yPosition, width, barHeight, 'F');
      
      // Add label and value
      pdf.setTextColor(80, 80, 80);
      pdf.setFontSize(8);
      pdf.text(item.label, x, yPosition - 2);
      pdf.text(item.value.toString(), x + barWidth + 5, yPosition + 4);
    });
  }

  // Enhanced Overall Summary section with better formatting
  pdf.setFillColor(240, 247, 255);
  pdf.roundedRect(15, yPos - 10, pageWidth - 30, 110, 3, 3, 'F'); // Reduced height from 130 to 110
  
  // Section title with accent bar
  pdf.setFillColor(219, 234, 254);
  pdf.rect(15, yPos - 10, 5, 110, 'F'); // Match container height
  
  pdf.setFontSize(18);
  pdf.setTextColor(0, 0, 255);
  pdf.text('Overall Summary', pageCenter, yPos, { align: 'center' });
  yPos += 15; // Reduced spacing

  // Center completion rate circle with label
  const circleX = pageCenter;
  const circleY = yPos + 10;
  
  pdf.setFontSize(14);
  pdf.setTextColor(100, 100, 100);
  pdf.text('Completion Rate', circleX, yPos - 7, { align: 'center' });
  
  // Draw colored completion circle with smaller radius (15 instead of 20)
  const circleColor = completionRate >= 75 ? '#22c55e' : completionRate >= 50 ? '#eab308' : '#ef4444';
  drawProgressCircle(circleX, circleY, 15, completionRate, circleColor);
  
  yPos += 40; // Reduced spacing after circle

  // Metrics grid in two rows, three columns
  const allMetrics = [
    { label: 'Total Tasks', value: `${totalTasks}`, color: '#3b82f6' },
    { label: 'Team Size', value: `${totalDevelopers}`, color: '#8b5cf6' },
    { label: 'Avg Tasks/Sprint', value: `${avgTasksPerSprint}`, color: '#06b6d4' },
    { label: 'Missing Estimates', value: `${tasksWithoutEstimates}`, color: '#f97316' },
    { label: 'Unassigned Tasks', value: `${tasksWithoutAssignees}`, color: '#ef4444' },
    { label: 'Estimate Accuracy', value: `${estimateAccuracy}%`, color: '#22c55e' },
  ];

  const metricsStartX = 30;
  const colWidth = (pageWidth - 60) / 3;

  allMetrics.forEach((metric, index) => {
    const col = index % 3;
    const row = Math.floor(index / 3);
    const xPos = metricsStartX + (col * colWidth);
    const yOffset = yPos + (row * 25);
    
    // Metric box with colored accent
    pdf.setFillColor(250, 250, 250);
    pdf.roundedRect(xPos, yOffset - 5, colWidth - 10, 20, 2, 2, 'F');
    pdf.setFillColor(metric.color);
    pdf.rect(xPos, yOffset - 5, 3, 20, 'F');
    
    // Metric label
    pdf.setFontSize(8);
    pdf.setTextColor(100, 100, 100);
    pdf.text(metric.label, xPos + 7, yOffset + 2);
    
    // Metric value
    pdf.setFontSize(12);
    pdf.setTextColor(0, 0, 0);
    pdf.text(metric.value, xPos + 6, yOffset + 10);
  });

  yPos += 70;

  // Enhanced Task Statistics section
  pdf.setFillColor(240, 247, 255);
  pdf.roundedRect(15, yPos - 10, pageWidth - 30, 100, 3, 3, 'F');
  
  pdf.setFontSize(18);
  pdf.setTextColor(0, 0, 255);
  pdf.text('Task Statistics', pageCenter, yPos, { align: 'center' });
  yPos += 20;

  // Status counts
  const statusCounts = {
    created: data.tasks.filter(t => t.status === 'created').length,
    'in-progress': data.tasks.filter(t => t.status === 'in-progress').length,
    'in-review': data.tasks.filter(t => t.status === 'in-review').length,
    testing: data.tasks.filter(t => t.status === 'testing').length,
    done: completedTasks
  };

  // Category counts
  const categoryCounts = {
    bug: data.tasks.filter(t => t.category === 'bug').length,
    feature: data.tasks.filter(t => t.category === 'feature').length,
    issue: data.tasks.filter(t => t.category === 'issue').length,
    uncategorized: data.tasks.filter(t => !t.category).length
  };

  // Draw status distribution chart on the left
  const statusData = [
    { label: 'Created', value: statusCounts.created, color: '#94a3b8' },
    { label: 'In Progress', value: statusCounts['in-progress'], color: '#eab308' },
    { label: 'In Review', value: statusCounts['in-review'], color: '#3b82f6' },
    { label: 'Testing', value: statusCounts.testing, color: '#9333ea' },
    { label: 'Done', value: statusCounts.done, color: '#22c55e' }
  ];

  // Draw category distribution chart on the right
  const categoryData = [
    { label: 'Bug', value: categoryCounts.bug, color: '#ef4444' },
    { label: 'Feature', value: categoryCounts.feature, color: '#3b82f6' },
    { label: 'Issue', value: categoryCounts.issue, color: '#f59e0b' },
    { label: 'Uncategorized', value: categoryCounts.uncategorized, color: '#94a3b8' }
  ];

  // Draw both charts side by side
  const leftChartX = 30;
  const rightChartX = pageWidth / 2 + 15;
  
  // Add labels for each chart
  pdf.setFontSize(12);
  pdf.setTextColor(80, 80, 80);
  pdf.text('Tasks by Status', leftChartX, yPos - 5);
  pdf.text('Tasks by Category', rightChartX, yPos - 5);

  // Draw the charts
  const maxStatusTasks = Math.max(...Object.values(statusCounts));
  const maxCategoryTasks = Math.max(...Object.values(categoryCounts));
  
  drawBarChart(leftChartX, yPos, statusData, maxStatusTasks);
  drawBarChart(rightChartX, yPos, categoryData, maxCategoryTasks);

  yPos += 100;

  // Add new page for Team Metrics per Sprint
  pdf.addPage();
  yPos = 20;

  // Enhanced Team Metrics per Sprint section with container
  pdf.setFillColor(240, 247, 255);
  pdf.roundedRect(15, yPos - 10, pageWidth - 30, 160, 3, 3, 'F'); // Increased height for table
  
  // Section title with accent bar
  pdf.setFillColor(219, 234, 254);
  pdf.rect(15, yPos - 10, 5, 160, 'F'); // Match container height
  
  pdf.setFontSize(18);
  pdf.setTextColor(0, 0, 255);
  pdf.text('Team Metrics per Sprint', pageCenter, yPos, { align: 'center' });
  yPos += 20;

  // Table container
  const tableStartX = 25; // Moved table right for padding
  const tableWidth = pageWidth - 50; // Adjusted width to fit in container

  // Table header with background
  pdf.setFillColor(240, 240, 240);
  pdf.rect(tableStartX, yPos - 5, tableWidth, 8, 'F');

  // Draw headers
  const headers = ['Sprint', 'Total Tasks', 'Completed', 'Hours Worked', 'Completion Rate'];
  const colWidths = [tableWidth * 0.35, tableWidth * 0.15, tableWidth * 0.15, tableWidth * 0.15, tableWidth * 0.2];
  
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
  const sprintMetrics = data.sprints.reduce((acc, sprint) => {
    const sprintTasks = data.tasks.filter(task => task.sprintId === sprint.id);
    acc[sprint.id] = {
      totalTasks: sprintTasks.length,
      completed: sprintTasks.filter(task => task.status === 'done').length,
      hoursWorked: sprintTasks.reduce((sum, task) => sum + (task.realHours || 0), 0),
    };
    return acc;
  }, {} as Record<number, { totalTasks: number; completed: number; hoursWorked: number }>);

  Object.entries(sprintMetrics).forEach(([sprintId, metrics], index) => {
    if (yPos > 270) {
      pdf.addPage();
      yPos = 20;
    }

    const sprint = data.sprints.find(s => s.id === Number(sprintId));
    if (!sprint) return;

    // Alternating row background
    if (index % 2 === 0) {
      pdf.setFillColor(250, 250, 250);
      pdf.rect(tableStartX, yPos - 5, tableWidth, 7, 'F');
    }

    let xPos = tableStartX;
    
    // Sprint name
    pdf.setTextColor(0, 0, 0);
    pdf.text(sprint.name, xPos, yPos);
    
    // Total tasks
    xPos += colWidths[0];
    pdf.text((metrics as { totalTasks: number }).totalTasks.toString(), xPos, yPos);
    
    // Completed tasks
    xPos += colWidths[1];
    pdf.text((metrics as { completed: number }).completed.toString(), xPos, yPos);
    
    // Hours worked
    xPos += colWidths[2];
    pdf.text(`${(metrics as { hoursWorked: number }).hoursWorked}h`, xPos, yPos);
    
    // Completion rate
    xPos += colWidths[3];
    const typedMetrics = metrics as { completed: number; totalTasks: number };
    const completionRate = Math.round((typedMetrics.completed / typedMetrics.totalTasks) * 100) || 0;
    
    // Set color based on completion rate
    if (completionRate >= 80) {
      pdf.setTextColor(22, 163, 74);   // Dark green
    } else if (completionRate >= 50) {
      pdf.setTextColor(234, 179, 8);   // Dark yellow
    } else {
      pdf.setTextColor(239, 68, 68);   // Dark red
    }
    pdf.text(`${completionRate}%`, xPos, yPos);
    
    yPos += 7;
  });

  yPos += 30;

  // Add new page for User Task Details
  pdf.addPage();
  yPos = 20;

  // Section 5: Detailed User Task Breakdown
  pdf.setFontSize(18);
  pdf.setTextColor(0, 0, 255);
  pdf.text('Detailed Developer Task Breakdown', 20, yPos);
  yPos += 10;

  // Group tasks by user
  const tasksByUser = data.users
    .filter(user => user.role === 'developer')
    .map(user => {
      const userTasks = data.tasks.filter(task => task.assignedToId === user.id);
      return {
        user,
        tasks: userTasks,
        metrics: {
          total: userTasks.length,
          completed: userTasks.filter(t => t.status === 'done').length,
          totalHours: userTasks.reduce((sum, t) => sum + (t.realHours || 0), 0),
          inProgress: userTasks.filter(t => t.status === 'in-progress').length,
          estimateAccuracy: calculateEstimateAccuracy(userTasks)
        }
      };
    });

  // Helper function to calculate estimate accuracy
  function calculateEstimateAccuracy(tasks: TaskResponse[]) {
    const completedTasks = tasks.filter(t => t.status === 'done' && t.estimateHours && t.realHours);
    if (completedTasks.length === 0) return 100;
    
    const totalEstimated = completedTasks.reduce((sum, t) => sum + (t.estimateHours || 0), 0);
    const totalReal = completedTasks.reduce((sum, t) => sum + (t.realHours || 0), 0);
    return Math.round((totalReal / totalEstimated) * 100);
  }

  // Generate detailed report for each user
  for (const [index, userData] of tasksByUser.entries()) {
    // Add new page for each user except the first one
    if (index > 0) {
      pdf.addPage();
      yPos = 20;
    }

    // Add more space between users
    yPos += 10;

    // Centered user section with enhanced styling
    pdf.setFillColor(240, 247, 255);
    pdf.roundedRect(15, yPos - 5, pageWidth - 30, 45, 3, 3, 'F');
    
    // Center user name
    pdf.setFontSize(14);
    pdf.setTextColor(0, 0, 255);
    pdf.text(`${userData.user.firstName} ${userData.user.lastName}`, pageCenter, yPos, { align: 'center' });
    
    // Center role/title
    pdf.setFontSize(10);
    pdf.setTextColor(100, 100, 100);
    pdf.text(userData.user.title || 'Developer', pageCenter, yPos + 7, { align: 'center' });

    // Centered metrics grid
    const metricsStartX = (pageWidth - 160) / 2; // Center the metrics grid
    const metrics = [
      { label: 'Total Tasks', value: userData.metrics.total },
      { label: 'Completed', value: userData.metrics.completed },
      { label: 'In Progress', value: userData.metrics.inProgress },
      { label: 'Total Hours', value: `${userData.metrics.totalHours}h` },
      { label: 'Completion Rate', value: `${Math.round((userData.metrics.completed / userData.metrics.total) * 100)}%` },
      { label: 'Estimate Accuracy', value: `${userData.metrics.estimateAccuracy}%` }
    ];

    metrics.forEach((metric, index) => {
      const col = index % 3;
      const row = Math.floor(index / 3);
      const xPos = metricsStartX + (col * 60);
      const yOffset = yPos + 15 + (row * 10);
      
      pdf.setFontSize(9);
      pdf.setTextColor(100, 100, 100);
      pdf.text(metric.label, xPos, yOffset);
      pdf.setFontSize(11);
      pdf.setTextColor(0, 0, 0);
      pdf.text(metric.value.toString(), xPos, yOffset + 5);
    });

    yPos += 55; // Add more space after metrics

    // Centered tasks table
    if (userData.tasks.length > 0) {
      const headers = ['Description', 'Status', 'Est. Hours', 'Real Hours', 'Diff'];
      const headerWidths = [80, 30, 25, 25, 20];
      
      // Table headers with gradient background
      pdf.setFillColor(240, 240, 240);
      pdf.rect(20, yPos - 5, 180, 8, 'F');
      pdf.setFontSize(10);
      pdf.setTextColor(80, 80, 80);
      
      headers.forEach((header, i) => {
        let xPos = 20;
        for (let j = 0; j < i; j++) xPos += headerWidths[j];
        pdf.text(header, xPos, yPos);
      });
      yPos += 8;

      // Table content with alternating row colors
      pdf.setFontSize(9);
      userData.tasks.forEach((task, index) => {
        if (yPos > 270) {
          pdf.addPage();
          yPos = 20;
        }

        // Alternating row background
        if (index % 2 === 0) {
          pdf.setFillColor(250, 250, 250);
          pdf.rect(20, yPos - 5, 180, 7, 'F');
        }

        let xPos = 20;
        pdf.setTextColor(0, 0, 0);
        pdf.text(task.description.substring(0, 40), xPos, yPos);
        
        xPos += headerWidths[0];
        const statusColor = getStatusColor(task.status);
        pdf.setTextColor(statusColor.r, statusColor.g, statusColor.b);
        pdf.text(task.status, xPos, yPos);
        
        xPos += headerWidths[1];
        pdf.setTextColor(0, 0, 0);
        pdf.text(task.estimateHours?.toString() || '-', xPos, yPos);
        
        xPos += headerWidths[2];
        pdf.text(task.realHours?.toString() || '-', xPos, yPos);
        
        xPos += headerWidths[3];
        const diff = (task.realHours || 0) - (task.estimateHours || 0);
        pdf.setTextColor(diff > 0 ? '#dc2626' : '#16a34a');
        pdf.text(diff !== 0 ? diff.toString() : '-', xPos, yPos);
        
        yPos += 7;
      });
      yPos += 15;
    }
    
    // Add space after each user section
    yPos += 20;
  }

  return pdf;
}

// Helper function for status colors
function getStatusColor(status: string): { r: number; g: number; b: number } {
  switch (status) {
    case 'done': return { r: 22, g: 163, b: 74 }; // green
    case 'in-progress': return { r: 234, g: 179, b: 8 }; // yellow
    case 'in-review': return { r: 37, g: 99, b: 235 }; // blue
    case 'testing': return { r: 147, g: 51, b: 234 }; // purple
    default: return { r: 100, g: 100, b: 100 }; // gray
  }
}
