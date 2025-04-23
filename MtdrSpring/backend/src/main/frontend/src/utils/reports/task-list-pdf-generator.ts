import jsPDF from "jspdf";
import { TaskResponse } from "../api/client";

export async function generateTaskListPDF(tasks: TaskResponse[]) {
  const pdf = new jsPDF("p", "mm", "a4");
  const pageWidth = pdf.internal.pageSize.width;
  let yPos = 20;

  // Header
  pdf.setFontSize(18);
  pdf.setTextColor(0, 0, 255);
  pdf.text("Task List Report", pageWidth / 2, yPos, { align: "center" });
  pdf.setFontSize(12);
  pdf.setTextColor(100, 100, 100);
  pdf.text(`Generated on: ${new Date().toLocaleDateString()}`, pageWidth / 2, yPos + 10, { align: "center" });

  yPos += 20;

  // Table headers
  pdf.setFontSize(10);
  pdf.setTextColor(0, 0, 0);
  pdf.setFillColor(240, 240, 240);
  pdf.rect(10, yPos, pageWidth - 20, 10, "F");
  pdf.text("ID", 12, yPos + 7);
  pdf.text("Description", 30, yPos + 7);
  pdf.text("Status", 120, yPos + 7);
  pdf.text("Category", 150, yPos + 7);
  pdf.text("Assigned To", 180, yPos + 7);

  yPos += 12;

  // Table rows
  tasks.forEach((task, index) => {
    if (yPos > 280) {
      pdf.addPage();
      yPos = 20;
    }

    pdf.text(task.id.toString(), 12, yPos);
    pdf.text(task.description.substring(0, 50), 30, yPos); // Limit description length
    pdf.text(task.status, 120, yPos);
    pdf.text(task.category || "N/A", 150, yPos);
    pdf.text(task.assignedToId ? task.assignedToId.toString() : "Unassigned", 180, yPos);

    yPos += 10;
  });

  return pdf;
}