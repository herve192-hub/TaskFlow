import { defineConfig } from '@playwright/test'
import smokeConfig from './playwright.config'

export default defineConfig({
  ...smokeConfig,
  testIgnore: [],
  testMatch: '**/*.full-stack.spec.ts',
  globalSetup: './e2e/full-stack.setup.ts',
  webServer: undefined,
  workers: 1,
  retries: 0,
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
