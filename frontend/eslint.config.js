/**
 * Flat ESLint configuration for authored React/TypeScript source and tooling.
 * Applies TypeScript, Hooks, and React Refresh rules; generated build/coverage and
 * browser reports are ignored. Keep file globs aligned with new source extensions
 * so CI's npm run lint continues to cover maintained code.
 */

import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import { defineConfig, globalIgnores } from 'eslint/config'

// Apply React/TypeScript checks to source and tooling while leaving generated artifacts out of linting.
export default defineConfig([
  globalIgnores(['dist', 'coverage', 'test-results', 'playwright-report']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      globals: globals.browser,
    },
  },
])
