import jsPDF from "jspdf";
import { TaskResponse } from "../api/client";

export async function generateTaskListPDF(tasks: TaskResponse[]) {
  // Create PDF document
  const pdf = new jsPDF("p", "mm", "a4");
  const pageWidth = pdf.internal.pageSize.width;
  const pageHeight = pdf.internal.pageSize.height;
  let yPos = 20;

  // Add decorative header background
  pdf.setFillColor(240, 247, 255);
  pdf.rect(0, 0, pageWidth, 40, "F");

  // Add accent bar
  pdf.setFillColor(59, 130, 246);
  pdf.rect(0, 0, 8, 40, "F");

  // Title with improved styling
  pdf.setFontSize(22);
  pdf.setTextColor(30, 64, 175);
  pdf.text("Task List Report", pageWidth / 2, yPos, { align: "center" });

  // Subtitle with date
  pdf.setFontSize(11);
  pdf.setTextColor(100, 116, 139);
  pdf.text(
    `Generated on: ${new Date().toLocaleDateString()}`,
    pageWidth / 2,
    yPos + 10,
    { align: "center" },
  );

  yPos += 30;

  // Add summary box
  pdf.setFillColor(248, 250, 252);
  pdf.roundedRect(15, yPos, pageWidth - 30, 20, 3, 3, "F");

  // Summary content
  pdf.setFontSize(10);
  pdf.setTextColor(71, 85, 105);
  pdf.text(`Total Tasks: ${tasks.length}`, 20, yPos + 8);

  const completedTasks = tasks.filter((t) => t.status === "done").length;
  pdf.text(`Completed: ${completedTasks}`, 70, yPos + 8);

  const completionRate = Math.round((completedTasks / tasks.length) * 100);
  pdf.text(`Completion Rate: ${completionRate}%`, 120, yPos + 8);

  const unassignedTasks = tasks.filter((t) => !t.assignedToId).length;
  pdf.text(`Unassigned: ${unassignedTasks}`, 180, yPos + 8);

  yPos += 30;

  // Table headers with gradient styling
  const tableStartX = 15;
  const tableWidth = pageWidth - 30;

  // Column definitions
  const columns = [
    { header: "ID", width: 15, align: "left" },
    { header: "Description", width: 85, align: "left" },
    { header: "Status", width: 30, align: "center" },
    { header: "Category", width: 30, align: "center" },
    { header: "Assigned To", width: 25, align: "center" },
  ];

  // Draw table header background
  pdf.setFillColor(241, 245, 249);
  pdf.rect(tableStartX, yPos - 5, tableWidth, 10, "F");

  // Draw header accent
  pdf.setFillColor(59, 130, 246);
  pdf.rect(tableStartX, yPos - 5, tableWidth, 1, "F");

  // Draw header text
  pdf.setFontSize(10);
  pdf.setTextColor(51, 65, 85);

  let xPos = tableStartX + 3;
  columns.forEach((column) => {
    pdf.text(column.header, xPos, yPos + 1);
    xPos += column.width;
  });

  yPos += 10;

  // Table rows with alternating colors and status highlighting
  pdf.setFontSize(9);

  tasks.forEach((task, index) => {
    // Check if we need a new page
    if (yPos > pageHeight - 20) {
      pdf.addPage();
      yPos = 20;

      // Add header to new page
      pdf.setFillColor(241, 245, 249);
      pdf.rect(tableStartX, yPos - 5, tableWidth, 10, "F");

      pdf.setFontSize(10);
      pdf.setTextColor(51, 65, 85);

      let xPos = tableStartX + 3;
      columns.forEach((column) => {
        pdf.text(column.header, xPos, yPos + 1);
        xPos += column.width;
      });

      yPos += 10;
    }

    // Alternating row background
    if (index % 2 === 0) {
      pdf.setFillColor(248, 250, 252);
      pdf.rect(tableStartX, yPos - 5, tableWidth, 8, "F");
    }

    // Draw row content
    xPos = tableStartX + 3;

    // ID column
    pdf.setTextColor(71, 85, 105);
    pdf.text(task.id.toString(), xPos, yPos);
    xPos += columns[0].width;

    // Description column (with truncation)
    const description =
      task.description.length > 45
        ? task.description.substring(0, 42) + "..."
        : task.description;
    pdf.text(description, xPos, yPos);
    xPos += columns[1].width;

    // Status column (with color coding)
    const statusColor = getStatusColor(task.status);
    pdf.setTextColor(statusColor.r, statusColor.g, statusColor.b);
    pdf.text(task.status, xPos, yPos);
    xPos += columns[2].width;

    // Category column
    pdf.setTextColor(71, 85, 105);
    pdf.text(task.category || "N/A", xPos, yPos);
    xPos += columns[3].width;

    // Assigned To column
    pdf.text(
      task.assignedToId ? task.assignedToId.toString() : "Unassigned",
      xPos,
      yPos,
    );

    yPos += 8;
  });

  // Add footer
  const footerY = pageHeight - 10;
  pdf.setFontSize(8);
  pdf.setTextColor(148, 163, 184);
  pdf.text("Task Management System", pageWidth / 2, footerY, {
    align: "center",
  });
  pdf.text(`Page 1 of ${pdf.getNumberOfPages()}`, pageWidth - 20, footerY);

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
      return { r: 100, g: 116, b: 139 }; // slate
  }
}
