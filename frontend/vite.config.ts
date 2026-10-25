/**
 * Local development/build configuration for the React SPA. Vite proxies /auth and
 * /api to the local gateway so cookies and CSRF remain same-origin in development.
 * Production serves dist through NGINX instead; changes to API prefixes must remain
 * aligned with the gateway routes and NGINX locations.
 */

import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Development uses the same /auth and /api paths that NGINX proxies in container deployments.
export default defineConfig({
  plugins: [react()],
  server: {
    // Keep browser requests on the Vite origin so cookie authentication and CSRF can work together.
    proxy: {
      '/auth': 'http://127.0.0.1:8080',
      '/api': 'http://127.0.0.1:8080',
    },
  },
})
