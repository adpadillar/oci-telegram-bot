import { test, expect } from '@playwright/test';

test.describe('Task Management E2E', () => {

  test.beforeEach(async ({ page, context }) => {
    // Login reutilizando el flujo de auth.spec.ts
    await page.goto('http://localhost:5173/login');
    await expect(page.getByRole('button', { name: /Have a master code\?/i })).toBeVisible({ timeout: 15000 });
    await page.getByRole('button', { name: /Have a master code\?/i }).click();
    await page.getByPlaceholder('Enter master code').fill('uwu');
    await page.getByRole('button', { name: /validate code/i }).click();
    await expect(page.getByText('Tasks')).toBeVisible({ timeout: 15000 });

    // Otro proceso si es mobile
    const mobileMenuButton = page.getByRole('button', { name: 'Toggle menu' });
    if (await mobileMenuButton.isVisible()) {
      await mobileMenuButton.click();
    }
    // Navega a la sección de tareas
    await page.getByRole('link', { name: /Tasks/i }).click();
    await expect(page.getByRole('heading', { name: /Tasks/i })).toBeVisible();
  });

  test('should filter tasks by category and status [ID: FILTER-001]', async ({ page }) => {
    // Filtrar por categoría y status
    await page.getByRole('button', { name: /^filter/i }).first().click();
    const selects = page.locator('select');
    await expect(selects.first()).toBeVisible({ timeout: 10000 });
    await selects.nth(0).selectOption('feature');
    await selects.nth(1).selectOption('created');
    await page.getByRole('button', { name: /apply filters/i }).click();
    await expect(page.getByText(/feature/i)).toBeVisible();
    await expect(page.getByText(/created/i)).toBeVisible();

    // Limpia filtros
    await page.getByRole('button', { name: /^filter/i }).first().click();
    await expect(selects.first()).toBeVisible({ timeout: 10000 });
    await page.getByRole('button', { name: /clear all/i }).first().click();

    // Screenshot
    await expect(page).toHaveScreenshot('tasks-filtered.png');
  });

  test('should add a new task and see it in the list [ID: ADD-001]', async ({ page }) => {
    const timestamp = Date.now();
    const taskDescription = `Nueva tarea Playwright - ${timestamp}`;

    // Abre el modal
    await page.getByRole('button', { name: /add/i }).click();

    // Espera a que el modal esté visible
    const modal = page.locator('.bg-white').filter({ hasText: 'Add New Task' });
    await expect(modal).toBeVisible();

    // Llena el modal
    const taskDescriptionInput = modal.locator('input[type="text"]').first();
    await taskDescriptionInput.fill(taskDescription);

    await modal.locator('select').nth(0).selectOption('feature');

    await modal.locator('select').nth(1).selectOption('created');

    await modal.locator('select').nth(2).selectOption('Sprint 1');

    await modal.locator('input[type="number"]').nth(0).fill('2');

    await modal.locator('select').nth(3).selectOption('Axel Padilla');

    // Re-fill Task Description justo antes de hacer clic en Añadir Tarea (workaround por que a veces el campo se vaciaba cuando re renderizaba)
    await taskDescriptionInput.fill(taskDescription);
    await expect(taskDescriptionInput).toHaveValue(taskDescription, { timeout: 5000 });

    // Click en "Add Task"
    await modal.getByRole('button', { name: /add task/i }).click();

    // Espera a que el modal de añadir tarea desaparezca
    await expect(modal).not.toBeVisible({ timeout: 15000 });

    // Filtra por el nombre de la tarea recién creada
    await page.getByPlaceholder('Search tasks...').fill(taskDescription);
    await page.keyboard.press('Enter'); // Simula presionar Enter para aplicar el filtro

    // Espera a que la tarea aparezca en la lista
    await expect(page.getByText(taskDescription, { exact: true })).toBeVisible({ timeout: 15000 });

    // Screenshot enmascarando elementos dinámicos
    await expect(page).toHaveScreenshot('task-added.png', {
      mask: [
        page.locator('h1:has-text("Tasks") span.text-sm'),

        page.getByPlaceholder('Search tasks...'),

        page.getByText(taskDescription, { exact: true }).locator('xpath=ancestor::tr'),
      ],
      maxDiffPixelRatio: 0.05,
    });
  });


}); 