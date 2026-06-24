import { test, expect } from '@playwright/test';

test.describe('Data Principal Portal - Home Page', () => {
  test('should display home page with all sections', async ({ page }) => {
    await page.goto('http://localhost:4201/home');
    
    await expect(page.locator('h1')).toContainText('DataShield India');
    await expect(page.locator('h2')).toContainText('Take Control of Your Personal Data');
    
    // Check feature cards
    await expect(page.locator('text=My Consents')).toBeVisible();
    await expect(page.locator('text=Data Requests')).toBeVisible();
    await expect(page.locator('text=Grievances')).toBeVisible();
    await expect(page.locator('text=My Profile')).toBeVisible();
  });

  test('should display DPDP rights section', async ({ page }) => {
    await page.goto('http://localhost:4201/home');
    
    await expect(page.locator('text=Your Rights Under DPDP Act 2023')).toBeVisible();
    await expect(page.locator('text=Right to Access')).toBeVisible();
    await expect(page.locator('text=Right to Correction')).toBeVisible();
    await expect(page.locator('text=Right to Erasure')).toBeVisible();
    await expect(page.locator('text=Right to Grievance Redressal')).toBeVisible();
  });

  test('should navigate to consent management', async ({ page }) => {
    await page.goto('http://localhost:4201/home');
    
    await page.click('text=Manage Consents →');
    await expect(page).toHaveURL(/\/consents/);
  });
});

test.describe('Data Principal Portal - Consent Management', () => {
  test('should display consent list', async ({ page }) => {
    await page.goto('http://localhost:4201/consents');
    
    await expect(page.locator('h1')).toContainText('My Consents');
    await expect(page.locator('text=Total Consents')).toBeVisible();
    await expect(page.locator('text=Active Consents')).toBeVisible();
    await expect(page.locator('text=Withdrawn')).toBeVisible();
  });

  test('should withdraw consent with confirmation', async ({ page }) => {
    await page.goto('http://localhost:4201/consents');
    
    // Set up dialog handler
    page.on('dialog', dialog => dialog.accept());
    
    // Click first withdraw button
    const withdrawButton = page.locator('button:has-text("Withdraw")').first();
    if (await withdrawButton.isVisible()) {
      await withdrawButton.click();
      
      // Check if status changed
      await expect(page.locator('text=WITHDRAWN')).toBeVisible();
    }
  });

  test('should navigate back to home', async ({ page }) => {
    await page.goto('http://localhost:4201/consents');
    
    await page.click('text=← Back to Home');
    await expect(page).toHaveURL(/\/home/);
  });
});

test.describe('Data Principal Portal - Data Requests (DSAR)', () => {
  test('should display request list', async ({ page }) => {
    await page.goto('http://localhost:4201/requests');
    
    await expect(page.locator('h1')).toContainText('My Data Requests');
    await expect(page.locator('text=Request History')).toBeVisible();
  });

  test('should navigate to new request form', async ({ page }) => {
    await page.goto('http://localhost:4201/requests');
    
    await page.click('text=+ New Request');
    await expect(page).toHaveURL(/\/requests\/new/);
  });

  test('should submit data access request', async ({ page }) => {
    await page.goto('http://localhost:4201/requests/new');
    
    // Fill form
    await page.selectOption('select[name="type"]', 'ACCESS');
    await page.fill('textarea[name="description"]', 'I would like to access all my personal data stored in your systems.');
    await page.fill('input[name="email"]', 'user@example.com');
    await page.fill('input[name="phone"]', '+91 98765 43210');
    await page.check('input[name="verified"]');
    
    // Submit
    page.on('dialog', dialog => dialog.accept());
    await page.click('button:has-text("Submit Request")');
    
    // Should redirect to requests list
    await page.waitForURL(/\/requests$/);
  });

  test('should validate required fields', async ({ page }) => {
    await page.goto('http://localhost:4201/requests/new');
    
    // Try to submit without filling
    await page.click('button:has-text("Submit Request")');
    
    // Button should be disabled
    const submitButton = page.locator('button:has-text("Submit Request")');
    await expect(submitButton).toBeDisabled();
  });
});

test.describe('Data Principal Portal - Grievances', () => {
  test('should display grievance list with SLA tracking', async ({ page }) => {
    await page.goto('http://localhost:4201/grievances');
    
    await expect(page.locator('h1')).toContainText('My Grievances');
    await expect(page.locator('text=30-day SLA')).toBeVisible();
  });

  test('should show SLA countdown for active grievances', async ({ page }) => {
    await page.goto('http://localhost:4201/grievances');
    
    // Check for SLA timeline elements
    const slaElements = page.locator('text=/\\d+ days remaining/');
    if (await slaElements.count() > 0) {
      await expect(slaElements.first()).toBeVisible();
    }
  });
});

test.describe('Data Principal Portal - Profile', () => {
  test('should display profile form', async ({ page }) => {
    await page.goto('http://localhost:4201/profile');
    
    await expect(page.locator('h1')).toContainText('My Profile');
    await expect(page.locator('text=Personal Information')).toBeVisible();
  });

  test('should update profile information', async ({ page }) => {
    await page.goto('http://localhost:4201/profile');
    
    // Update fields
    await page.fill('input[name="name"]', 'Test User Updated');
    await page.selectOption('select[name="language"]', 'hi');
    
    // Save
    page.on('dialog', dialog => dialog.accept());
    await page.click('button:has-text("Save Changes")');
  });

  test('should display account summary', async ({ page }) => {
    await page.goto('http://localhost:4201/profile');
    
    await expect(page.locator('text=Account Summary')).toBeVisible();
    await expect(page.locator('text=Active Consents')).toBeVisible();
    await expect(page.locator('text=Requests Submitted')).toBeVisible();
    await expect(page.locator('text=Open Grievances')).toBeVisible();
  });

  test('should display danger zone', async ({ page }) => {
    await page.goto('http://localhost:4201/profile');
    
    await expect(page.locator('text=Danger Zone')).toBeVisible();
    await expect(page.locator('button:has-text("Delete My Account")')).toBeVisible();
  });
});

test.describe('Data Principal Portal - Responsive Design', () => {
  test('should work on mobile viewport', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('http://localhost:4201/home');
    
    await expect(page.locator('h1')).toBeVisible();
    await expect(page.locator('text=My Consents')).toBeVisible();
  });

  test('should work on tablet viewport', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.goto('http://localhost:4201/home');
    
    await expect(page.locator('h1')).toBeVisible();
  });
});
