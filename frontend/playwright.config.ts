/**
 * Browser smoke suite against a locally started Vite preview of the built SPA.
 * Excludes *.full-stack.spec.ts; those need an independently started Compose stack.
 * CI bounds workers/retries and records failure diagnostics plus JUnit/HTML reports.
 * Keep generated traces and reports out of version control.
 */

import { defineConfig, devices } from '@playwright/test'

// Smoke-test the compiled frontend through Vite preview; real services use the full-stack config.
export default defineConfig({
  testDir: './e2e',
  testIgnore: '**/*.full-stack.spec.ts', // The Compose-backed scenario has its own startup and artifacts.
  outputDir: 'test-results/playwright',
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 2 : undefined,
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['junit', { outputFile: 'test-results/e2e.xml' }],
  ],
  use: {
    baseURL: 'http://127.0.0.1:4173',
    trace: 'on-first-retry', // Capture retry diagnostics without recording every successful smoke test.
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'npm run preview -- --host 127.0.0.1 --port 4173 --strictPort',
    url: 'http://127.0.0.1:4173',
    reuseExistingServer: !process.env.CI, // CI must test the preview server started for this checkout.
    timeout: 30_000,
  },
})
