import { test, expect } from '@playwright/test';

test.describe('Compliance Dashboard - Authentication', () => {
  test('should display login page', async ({ page }) => {
    await page.goto('/login');
    
    await expect(page).toHaveTitle(/DataShield India/);
    await expect(page.locator('h1')).toContainText('DataShield India');
    await expect(page.locator('h2')).toContainText('DPO Dashboard Login');
  });

  test('should show validation error for empty fields', async ({ page }) => {
    await page.goto('/login');
    
    await page.click('button[type="submit"]');
    
    // Should show error message
    await expect(page.locator('text=Please enter email and password')).toBeVisible();
  });

  test('should login with demo credentials', async ({ page }) => {
    await page.goto('/login');
    
    // Fill login form
    await page.fill('input[type="email"]', 'dpo@example.com');
    await page.fill('input[type="password"]', 'demo123');
    
    // Submit form
    await page.click('button[type="submit"]');
    
    // Wait for navigation to dashboard
    await page.waitForURL('/dashboard');
    
    // Verify dashboard elements
    await expect(page.locator('h1')).toContainText('Compliance Dashboard');
    await expect(page.locator('text=Overall Compliance Score')).toBeVisible();
  });

  test('should show error for invalid credentials', async ({ page }) => {
    await page.goto('/login');
    
    await page.fill('input[type="email"]', 'invalid@example.com');
    await page.fill('input[type="password"]', 'wrongpassword');
    
    await page.click('button[type="submit"]');
    
    // Should show error message
    await expect(page.locator('text=/Login failed/i')).toBeVisible();
  });

  test('should logout successfully', async ({ page }) => {
    // Login first
    await page.goto('/login');
    await page.fill('input[type="email"]', 'dpo@example.com');
    await page.fill('input[type="password"]', 'demo123');
    await page.click('button[type="submit"]');
    await page.waitForURL('/dashboard');
    
    // Logout
    await page.click('button:has-text("Logout")');
    
    // Should redirect to login
    await page.waitForURL('/login');
    await expect(page.locator('h2')).toContainText('DPO Dashboard Login');
  });
});

test.describe('Compliance Dashboard - Dashboard Features', () => {
  test.beforeEach(async ({ page }) => {
    // Login before each test
    await page.goto('/login');
    await page.fill('input[type="email"]', 'dpo@example.com');
    await page.fill('input[type="password"]', 'demo123');
    await page.click('button[type="submit"]');
    await page.waitForURL('/dashboard');
  });

  test('should display compliance score card', async ({ page }) => {
    await expect(page.locator('text=Overall Compliance Score')).toBeVisible();
    
    // Check for score display
    const scoreElement = page.locator('text=/\\d+%/').first();
    await expect(scoreElement).toBeVisible();
  });

  test('should display key metrics cards', async ({ page }) => {
    // Check for all metric cards
    await expect(page.locator('text=Total Consents')).toBeVisible();
    await expect(page.locator('text=Pending Grievances')).toBeVisible();
    await expect(page.locator('text=Open Breaches')).toBeVisible();
    await expect(page.locator('text=Critical Risk Vendors')).toBeVisible();
  });

  test('should navigate to different modules', async ({ page }) => {
    // Test navigation to Grievances
    await page.click('text=View Grievances');
    await expect(page).toHaveURL(/\/grievances/);
    
    // Navigate back
    await page.goto('/dashboard');
    
    // Test navigation to Breaches
    await page.click('text=Breach Management');
    await expect(page).toHaveURL(/\/breaches/);
  });

  test('should display recent alerts', async ({ page }) => {
    await expect(page.locator('text=Recent Alerts')).toBeVisible();
  });

  test('should display quick actions', async ({ page }) => {
    await expect(page.locator('text=Quick Actions')).toBeVisible();
    await expect(page.locator('text=View Grievances')).toBeVisible();
    await expect(page.locator('text=Breach Management')).toBeVisible();
    await expect(page.locator('text=Vendor Risk Assessment')).toBeVisible();
  });
});
