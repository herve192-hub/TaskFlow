import { defineConfig } from '@playwright/test'
import smokeConfig from './playwright.config'

// Reuse browser defaults while directing the scenario to the independently started Compose stack.
export default defineConfig({
  ...smokeConfig,
  testIgnore: [], // Clear the smoke config's exclusion of *.full-stack.spec.ts files.
  testMatch: '**/*.full-stack.spec.ts',
  globalSetup: './e2e/full-stack.setup.ts',
  webServer: undefined, // NGINX serves the built frontend; Playwright must not start Vite preview here.
  workers: 1,
  retries: 0, // Avoid replaying a stateful collaboration scenario against the same disposable services.
  timeout: 180_000,
  expect: { timeout: 15_000 },
  outputDir: 'test-results/full-stack',
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report/full-stack', open: 'never' }],
    ['junit', { outputFile: 'test-results/full-stack.xml' }],
  ],
  use: {
    ...smokeConfig.use,
    baseURL: process.env.TASKFLOW_E2E_BASE_URL || 'http://127.0.0.1:15173',
    actionTimeout: 15_000,
    navigationTimeout: 30_000,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
})
