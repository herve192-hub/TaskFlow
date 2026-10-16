import { execFile } from 'node:child_process'
import { randomUUID } from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { expect, request, test } from '@playwright/test'
import type { APIRequestContext, Page } from '@playwright/test'
import { gatewayURL } from './full-stack.setup'

interface Account {
  firstname: string
  lastname: string
  username: string
  email: string
  password: string
}

interface Session {
  id: string
  api: APIRequestContext
  refreshToken: string
  profileId: string
}

function account(role: string, suffix: string): Account {
  return {
    firstname: role, lastname: 'Workflow',
    username: `${role.toLowerCase().slice(0, 7)}_${randomUUID().replaceAll('-', '').slice(0, 12)}`,
    email: `${role.toLowerCase()}.${suffix}@example.test`, password: `E2e7!${randomUUID()}`,
  }
}

async function registerAndSignIn(page: Page, user: Account): Promise<Session> {
  await page.goto('/signup')
  await page.getByLabel('First Name', { exact: true }).fill(user.firstname)
  await page.getByLabel('Last Name', { exact: true }).fill(user.lastname)
  await page.getByLabel('Username', { exact: true }).fill(user.username)
  await page.getByLabel('Email', { exact: true }).fill(user.email)
  await page.getByLabel('Password', { exact: true }).fill(user.password)
  await page.getByLabel('Confirm Password', { exact: true }).fill(user.password)
  const registration = page.waitForResponse((response) =>
    new URL(response.url()).pathname === '/auth/register' && response.request().method() === 'POST')
  await page.getByRole('button', { name: 'Create Account', exact: true }).click()
  expect((await registration).status()).toBe(201)
  await expect(page).toHaveURL(/\/login$/)
  // Registration saves a session too. Clear it to prove the login really works.
  await page.evaluate(() => sessionStorage.clear())
  await page.getByLabel('Email', { exact: true }).fill(user.email)
  await page.getByLabel('Password', { exact: true }).fill(user.password)
  const login = page.waitForResponse((response) =>
    new URL(response.url()).pathname === '/auth/login' && response.request().method() === 'POST')
  await page.getByRole('button', { name: 'Sign In', exact: true }).click()
  const response = await login
  expect(response.status()).toBe(200)
  const auth = await response.json()
  expect(auth.user.id).toEqual(expect.any(String))
  await expect(page).toHaveURL(/\/dashboard$/)
  await expect(page.getByRole('progressbar', { name: 'Loading dashboard' })).toBeHidden()
  const api = await request.newContext({
    baseURL: gatewayURL, extraHTTPHeaders: { Authorization: `Bearer ${auth.accessToken}` },
  })
  const profile = await api.get('/api/v1/users/me')
  expect(profile.status()).toBe(200)
  expect(await profile.json()).toMatchObject({ authUserId: auth.user.id, email: user.email })
  return { id: auth.user.id, api, refreshToken: auth.refreshToken, profileId: (await profile.json()).id }
}

async function changeStatus(page: Page, status: string) {
  await page.getByRole('combobox', { name: 'Task status' }).click()
  await page.getByRole('option', { name: status, exact: true }).click()
  await expect(page.getByRole('status').filter({ hasText: 'Status updated.' })).toBeVisible()
  await expect(page.getByRole('combobox', { name: 'Task status' })).toHaveText(status)
}

async function kafkaMessages(topic: string): Promise<Record<string, unknown>[]> {
  const root = fileURLToPath(new URL('../../', import.meta.url))
  const args = [
    'compose', '--project-name', process.env.COMPOSE_PROJECT_NAME || 'taskflow-e2e',
    '--env-file', '.github/e2e/.env.runtime', '--file', '.github/e2e/compose.yml',
    'exec', '-T', 'kafka', '/opt/kafka/bin/kafka-console-consumer.sh',
    '--bootstrap-server', 'localhost:29092', '--topic', topic, '--from-beginning',
    '--timeout-ms', '10000', '--group', `workflow-${randomUUID()}`,
  ]
  return new Promise((resolve, reject) => {
    execFile('docker', args, { cwd: root, timeout: 30_000, maxBuffer: 2 * 1024 * 1024 },
      (error, stdout, stderr) => {
        // Kafka exits 1 on its idle timeout after consuming all available records.
        if (error && (error.killed || error.code !== 1 || !stderr.includes('TimeoutException'))) {
          reject(error)
          return
        }
        try {
          resolve(stdout.trim().split('\n').filter(Boolean).map((line) => JSON.parse(line)))
        } catch (parseError) {
          reject(parseError)
        }
      })
  })
}

test('a team completes a task, sees dashboard progress, and enforces project access', async ({ browser, page }, testInfo) => {
  const suffix = `${Date.now()}_${randomUUID().slice(0, 8)}`
  const ownerAccount = account('Owner', suffix)
  const memberAccount = account('Member', suffix)
  const outsiderAccount = account('Outsider', suffix)
  const memberContext = await browser.newContext({ baseURL: testInfo.project.use.baseURL })
  const outsiderContext = await browser.newContext({ baseURL: testInfo.project.use.baseURL })
  const memberPage = await memberContext.newPage()
  const outsiderPage = await outsiderContext.newPage()
  const sessions: Session[] = []
  let owner: Session
  let member: Session
  let outsider: Session
  let projectId: string
  let memberId: string
  let taskId: string
  const projectName = `Collaboration ${suffix}`
  const taskTitle = `Complete workflow ${suffix}`

  try {
    await test.step('Register and sign in three real users; initialize their directory profiles', async () => {
      owner = await registerAndSignIn(page, ownerAccount)
      sessions.push(owner)
      member = await registerAndSignIn(memberPage, memberAccount)
      sessions.push(member)
      outsider = await registerAndSignIn(outsiderPage, outsiderAccount)
      sessions.push(outsider)
    })

    await test.step('Enforce browser session, CSRF, token-purpose, privacy, and gateway boundaries', async () => {
      const cookies = await page.context().cookies()
      const access = cookies.find((cookie) => cookie.name === 'taskflow_access')
      expect(access).toMatchObject({ httpOnly: true, sameSite: 'Strict' })
      expect(access!.expires - Date.now() / 1000).toBeLessThanOrEqual(900)
      const storesContainTokens = await page.evaluate(() => [localStorage, sessionStorage].some((storage) =>
        Object.keys(storage).some((key) => /accessToken|refreshToken/.test(key)
          || /^eyJ[^.]+\.[^.]+\./.test(storage.getItem(key) ?? ''))))
      expect(storesContainTokens).toBe(false)
      const frontend = await page.request.get('/')
      expect(frontend.headers()['content-security-policy']).toContain("script-src 'self'")
      expect(frontend.headers()['x-content-type-options']).toBe('nosniff')
      expect(frontend.headers()['x-frame-options']).toBe('DENY')
      expect((await page.request.post('/api/v1/projects', { data: { name: 'Blocked CSRF project', visibility: 'PRIVATE' } })).status()).toBe(403)
      const refresh = await request.newContext({ baseURL: gatewayURL,
        extraHTTPHeaders: { Authorization: `Bearer ${owner.refreshToken}` } })
      try {
        for (const path of ['/api/v1/users/me', '/api/v1/projects', '/api/v1/tasks/me']) {
          expect((await refresh.get(path)).status(), path).toBe(401)
        }
      } finally { await refresh.dispose() }
      expect((await outsider.api.get(`/api/v1/users/${owner.profileId}`)).status()).toBe(403)
      for (const path of ['/config-server/auth-service/default', '/auth-service/actuator/env',
        '/user-service/api/v1/users/me', '/project-service/api/v1/admin/projects', '/actuator/env']) {
        expect((await owner.api.get(path)).status(), path).toBe(404)
      }
    })

    await test.step('The owner creates a private project and adds a registered teammate through the UI', async () => {
      await page.getByRole('link', { name: 'View Projects', exact: true }).click()
      await expect(page.getByText('No projects yet', { exact: true })).toBeVisible()
      await page.getByRole('button', { name: 'Create Project', exact: true }).first().click()
      const dialog = page.getByRole('dialog')
      await dialog.getByLabel('Project name').fill(projectName)
      await dialog.getByLabel('Description').fill('Exercise the real gateway, services, persistence, and events.')
      const created = page.waitForResponse((response) =>
        new URL(response.url()).pathname === '/api/v1/projects' && response.request().method() === 'POST')
      await dialog.getByRole('button', { name: 'Create Project', exact: true }).click()
      const response = await created
      expect(response.status()).toBe(201)
      projectId = (await response.json()).id
      await page.getByRole('link', { name: `View team for ${projectName}`, exact: true }).click()
      await expect(page.getByText('Owner', { exact: true })).toBeVisible()
      await page.getByRole('button', { name: 'Add member', exact: true }).click()
      const addDialog = page.getByRole('dialog', { name: 'Add a team member' })
      await addDialog.getByLabel('Search by name or email').fill(memberAccount.email)
      await addDialog.getByRole('button', { name: 'Select Member Workflow', exact: true }).click()
      const added = page.waitForResponse((result) =>
        new URL(result.url()).pathname === `/api/v1/projects/${projectId}/members` && result.request().method() === 'POST')
      await addDialog.getByRole('button', { name: 'Add member', exact: true }).click()
      const membership = await added
      expect(membership.status()).toBe(201)
      const body = await membership.json()
      expect(body).toMatchObject({ userId: member.id, role: 'MEMBER' })
      memberId = body.id
      await expect(page.getByText('2 team members', { exact: true })).toBeVisible()
      await expect(page.getByText(memberAccount.email, { exact: true })).toBeVisible()
      const access = await member.api.get(`/api/v1/projects/${projectId}/access`)
      expect(access.status()).toBe(200)
      expect(await access.json()).toMatchObject({ member: true, role: 'MEMBER', canManageMembers: false })
    })

    await test.step('Create a task in the browser and assign it through the real gateway API', async () => {
      await page.getByRole('link', { name: 'View tasks', exact: true }).click()
      await expect(page.getByText('No tasks yet', { exact: true })).toBeVisible()
      await page.getByRole('button', { name: 'Create Task', exact: true }).first().click()
      const dialog = page.getByRole('dialog', { name: 'Create Task' })
      await dialog.getByLabel('Task title').fill(taskTitle)
      const created = page.waitForResponse((response) =>
        new URL(response.url()).pathname === '/api/v1/tasks' && response.request().method() === 'POST')
      await dialog.getByRole('button', { name: 'Create Task', exact: true }).click()
      const response = await created
      expect(response.status()).toBe(201)
      const task = await response.json()
      expect(task).toMatchObject({ title: taskTitle, projectId, status: 'TODO', createdBy: owner.id })
      taskId = task.id
      await expect(page.getByRole('heading', { name: taskTitle, exact: true })).toBeVisible()
      // No assignment control exists in the UI yet. Use the existing authenticated API.
      expect((await member.api.patch(`/api/v1/tasks/${taskId}/status`, { data: { status: 'IN_PROGRESS' } })).status()).toBe(403)
      const assigned = await owner.api.put(`/api/v1/tasks/${taskId}/assignee/${member.id}`)
      expect(assigned.status()).toBe(200)
      expect(await assigned.json()).toMatchObject({ id: taskId, assigneeId: member.id })
    })

    await test.step('Reject outsider access, member privilege escalation, and unauthenticated requests', async () => {
      for (const path of [`/api/v1/projects/${projectId}`, `/api/v1/projects/${projectId}/members`,
        `/api/v1/tasks/project/${projectId}`, `/api/v1/tasks/${taskId}`]) {
        expect((await outsider.api.get(path)).status(), path).toBe(403)
      }
      expect((await outsider.api.patch(`/api/v1/tasks/${taskId}/status`, { data: { status: 'IN_PROGRESS' } })).status()).toBe(403)
      expect((await member.api.put(`/api/v1/projects/${projectId}/members/${memberId}`, { data: { role: 'ADMIN' } })).status()).toBe(403)
      expect((await member.api.put(`/api/v1/tasks/${taskId}/assignee/${owner.id}`)).status()).toBe(403)
      const anonymous = await request.newContext({ baseURL: gatewayURL })
      try {
        expect((await anonymous.get(`/api/v1/tasks/${taskId}`)).status()).toBe(401)
      } finally {
        await anonymous.dispose()
      }
      await outsiderPage.goto(`/dashboard/projects/${projectId}/tasks`)
      await expect(outsiderPage.getByRole('alert').filter({ hasText: 'You do not have access to this project' })).toBeVisible()
      await expect(outsiderPage.getByRole('heading', { name: taskTitle, exact: true })).toHaveCount(0)
    })

    await test.step('The assignee completes the lifecycle in the browser and it persists after reload', async () => {
      await memberPage.goto(`/dashboard/projects/${projectId}/tasks`)
      await expect(memberPage.getByRole('heading', { name: taskTitle, exact: true })).toBeVisible()
      await changeStatus(memberPage, 'IN PROGRESS')
      expect((await member.api.patch(`/api/v1/tasks/${taskId}/status`, { data: { status: 'CANCELLED' } })).status()).toBe(403)
      await changeStatus(memberPage, 'IN REVIEW')
      await changeStatus(memberPage, 'COMPLETED')
      await memberPage.reload()
      await expect(memberPage.getByRole('combobox', { name: 'Task status' })).toHaveText('COMPLETED')
      const persisted = await owner.api.get(`/api/v1/tasks/${taskId}`)
      expect(persisted.status()).toBe(200)
      expect(await persisted.json()).toMatchObject({ status: 'COMPLETED', assigneeId: member.id })
    })

    await test.step('Both users see the completed task and 100% progress on their dashboards', async () => {
      for (const dashboard of [page, memberPage]) {
        await dashboard.goto('/dashboard')
        await expect(dashboard.getByText('1 of 1 tasks completed (100%)', { exact: true })).toBeVisible()
        await expect(dashboard.getByRole('progressbar', { name: 'Task completion' })).toHaveAttribute('aria-valuenow', '100')
        await expect(dashboard.getByRole('link', { name: taskTitle, exact: true })).toBeVisible()
      }
    })

    await test.step('Kafka receives all three status transitions and the completion event', async () => {
      const [statuses, completions] = await Promise.all([
        kafkaMessages('task.status.changed.v1'), kafkaMessages('task.completed.v1'),
      ])
      const transitions = statuses.filter((event) => event.taskId === taskId)
      expect(transitions.map((event) => event.newStatus)).toEqual(['IN_PROGRESS', 'IN_REVIEW', 'COMPLETED'])
      for (const event of transitions) {
        expect(event).toMatchObject({ projectId, changedBy: member.id, eventType: 'task.status.changed' })
      }
      expect(completions.filter((event) => event.taskId === taskId)).toEqual([
        expect.objectContaining({ projectId, assigneeId: member.id, completedBy: member.id, eventType: 'task.completed' }),
      ])
      await testInfo.attach('task-events', {
        body: JSON.stringify({ transitions, completions: completions.filter((event) => event.taskId === taskId) }, null, 2),
        contentType: 'application/json',
      })
    })

    await test.step('Removing the teammate revokes project and task access without losing owner data', async () => {
      await page.goto(`/dashboard/projects/${projectId}/members`)
      await page.getByRole('button', { name: 'Remove Member Workflow', exact: true }).click()
      await page.getByRole('dialog', { name: 'Remove team member?' }).getByRole('button', { name: 'Remove member', exact: true }).click()
      await expect(page.getByText('1 team member', { exact: true })).toBeVisible()
      expect((await member.api.get(`/api/v1/projects/${projectId}`)).status()).toBe(403)
      expect((await member.api.get(`/api/v1/tasks/${taskId}`)).status()).toBe(403)
      expect((await member.api.patch(`/api/v1/tasks/${taskId}/status`, { data: { status: 'IN_PROGRESS' } })).status()).toBe(403)
      await memberPage.goto('/dashboard')
      await expect(memberPage.getByText('0 of 0 tasks completed (0%)', { exact: true })).toBeVisible()
      await expect(memberPage.getByRole('link', { name: taskTitle, exact: true })).toHaveCount(0)
      expect((await owner.api.get(`/api/v1/tasks/${taskId}`)).status()).toBe(200)
    })
  } finally {
    await Promise.all(sessions.map((session) => session.api.dispose()))
    await memberContext.close()
    await outsiderContext.close()
  }
})
