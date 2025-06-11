import { test, expect } from '@playwright/test';

// Etiquetas para categorizar
// @tasks @filter @download

test.describe('Task Management E2E', () => {

  test.beforeEach(async ({ page }) => {
    // Login reutilizando el flujo real
    await page.goto('http://localhost:5173/login');
    await expect(page.getByRole('button', { name: /Have a master code\?/i })).toBeVisible({ timeout: 15000 });
    await page.getByRole('button', { name: /Have a master code\?/i }).click();
    await page.getByPlaceholder('Enter master code').fill('uwu');
    await page.getByRole('button', { name: /validate code/i }).click();
    await expect(page.getByText('Tasks')).toBeVisible();
    // Navega a la sección de tareas
    await page.getByRole('link', { name: /Tasks/i }).click();
    await expect(page.getByRole('heading', { name: /Tasks/i })).toBeVisible();
  });

  test('should filter tasks by category and status [@filter @param]', async ({ page }) => {
    // --- Primer filtro ---
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

    // Screenshot visual del resultado filtrado
    await expect(page).toHaveScreenshot('tasks-filtered.png');
  });

  test('should add a new task and see it in the list [@add]', async ({ page }) => {
    // Abre el modal
    await page.getByRole('button', { name: /add task/i }).click();

    // Espera a que el modal esté visible
    const modal = page.locator('.bg-white').filter({ hasText: 'Add New Task' });
    await expect(modal).toBeVisible();

    // Llena la descripción
    await modal.locator('input[type="text"]').first().fill('Nueva tarea Playwright');

    // Selecciona categoría
    await modal.locator('select').nth(0).selectOption('feature');

    // Selecciona status
    await modal.locator('select').nth(1).selectOption('created');

    // Selecciona sprint "Sprint 1"
    await modal.locator('select').nth(2).selectOption('Sprint 1');

    // Llena estimate (hours)
    await modal.locator('input[type="number"]').nth(0).fill('2');

    // Selecciona usuario asignado "Axel Padilla"
    await modal.locator('select').nth(3).selectOption('Axel Padilla');

    // Log page content before clicking "Add Task"
    console.log('Page content before clicking Add Task:', await page.content());

    // Click "Add Task"
    await modal.getByRole('button', { name: /add task/i }).click();

    // Espera a que la llamada a la API para crear la tarea sea exitosa
    const response = await page.waitForResponse(resp =>
      resp.url().includes('/api/v1/tasks') &&
      resp.request().method() === 'POST'
    );

    // Log the response status
    console.log(`API response status for /api/v1/tasks POST: ${response.status()}`);

    // Assert that the response status is 201 (Created)
    expect(response.status()).toBe(201); // Este expect fallará si el status no es 201

    // Espera a que el modal de añadir tarea se cierre
    await expect(modal).not.toBeVisible({ timeout: 10000 });

    // Espera a que la tarea aparezca en la lista
    await expect(page.getByText('Nueva tarea Playwright')).toBeVisible({ timeout: 15000 });

    // Screenshot visual del modal cerrado y tarea visible
    await expect(page).toHaveScreenshot('task-added.png');
  });

  test('should download the task list as PDF [@download @mock]', async ({ page, context }) => {
    // Mock de la descarga (HAR o interceptar la petición)
    // Aquí solo verificamos que el botón existe y se puede clickear
    const [download] = await Promise.all([
      page.waitForEvent('download'),
      page.getByRole('button', { name: /download task list/i }).click(),
    ]);
    // Verifica que el archivo se descargó
    const path = await download.path();
    expect(path).toBeTruthy();
    // Screenshot visual tras la descarga
    await expect(page).toHaveScreenshot('tasklist-download.png');
  });
}); 