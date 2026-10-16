import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

// Split browser-environment tests into separately reported unit and integration projects.
export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    projects: [
      {
        extends: true,
        test: {
          name: 'unit',
          include: ['src/**/*.test.{ts,tsx}'],
          exclude: ['src/**/*.integration.test.{ts,tsx}'], // Prevent integration tests running in both projects.
        },
      },
      {
        extends: true,
        test: {
          name: 'integration',
          include: ['src/**/*.integration.test.{ts,tsx}'],
        },
      },
    ],
    reporters: ['default', 'junit'],
    outputFile: { junit: 'test-results/junit.xml' },
    coverage: {
      // CI overrides report/output paths per project to preserve both suites' results.
      provider: 'v8',
      include: ['src/**/*.{ts,tsx}'],
      exclude: ['src/**/*.d.ts', 'src/main.tsx'],
      reporter: ['text', 'lcov', 'html', 'json-summary'],
    },
  },
})
