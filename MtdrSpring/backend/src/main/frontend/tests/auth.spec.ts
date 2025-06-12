import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Navega a la página de login del frontend
    await page.goto('http://localhost:5173/login');

    // Espera a que el botón esté visible (no link)
    await expect(page.getByRole('button', { name: /Have a master code\?/i })).toBeVisible({ timeout: 15000 });

    // Clic en "Have a master code?"
    await page.getByRole('button', { name: /Have a master code\?/i }).click();

    // Llena el campo de master code
    await page.getByPlaceholder('Enter master code').fill('uwu');

    // Clic en "Validate Code"
    await page.getByRole('button', { name: /validate code/i }).click();

    // Espera a que el dashboard sea visible
    await expect(page.getByText('Developers')).toBeVisible();

    console.log(await page.content());
  });

  test('should access protected route after login [ID: AUTH-001]', async ({ page }) => {
    // Ya está autenticado por el beforeEach
    await page.goto('http://localhost:5173/dashboard');
    // Busca un texto único del dashboard, por ejemplo "Developers"
    await expect(page.getByText('Developers')).toBeVisible();
  });

  test('should logout successfully [ID: AUTH-002]', async ({ page }) => {
    await page.getByRole('button', { name: /logout/i }).click();
    await expect(page).toHaveURL('http://localhost:5173/login');
  });
}); 