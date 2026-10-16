import { expect } from '@playwright/test'
import type { FullConfig } from '@playwright/test'

export const gatewayURL = process.env.TASKFLOW_E2E_API_URL || 'http://127.0.0.1:18080'

export default async function waitForStack(config: FullConfig) {
  const frontendURL = config.projects[0].use.baseURL!
  // A healthy gateway alone does not mean Eureka has discovered each service.
  // A validation/authentication response proves that the real route is ready.
  await expect.poll(async () => {
    try {
      const responses = await Promise.all([
        fetch(frontendURL, { signal: AbortSignal.timeout(5_000) }),
        fetch(`${gatewayURL}/auth/login`, {
          method: 'POST', headers: { 'Content-Type': 'application/json' },
          body: '{}', signal: AbortSignal.timeout(5_000),
        }),
        ...['/api/v1/users/me', '/api/v1/projects', '/api/v1/tasks/me'].map((path) =>
          fetch(`${gatewayURL}${path}`, { signal: AbortSignal.timeout(5_000) })),
      ])
      return {
        frontend: responses[0].status === 200,
        // Auth currently secures its /error dispatch, so validation can return 403.
        auth: [400, 403].includes(responses[1].status),
        users: [401, 403].includes(responses[2].status),
        projects: [401, 403].includes(responses[3].status),
        tasks: [401, 403].includes(responses[4].status),
      }
    } catch {
      return {}
    }
  }, {
    message: 'Wait for NGINX and all four gateway routes to reach the real services',
    timeout: 120_000,
    intervals: [1_000, 2_000, 5_000],
  }).toEqual({ frontend: true, auth: true, users: true, projects: true, tasks: true })
}
