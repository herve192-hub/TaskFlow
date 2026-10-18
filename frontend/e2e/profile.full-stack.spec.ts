import { randomUUID } from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { expect, request, test } from '@playwright/test'
import { gatewayURL } from './full-stack.setup'

test('profile edits, photos, and account preferences persist and remain private', async ({ page }) => {
  const suffix = randomUUID().replaceAll('-', '').slice(0, 12)
  const account = { firstname: 'Alex', lastname: 'Profile', username: `profile_${suffix}`,
    email: `profile.${suffix}@example.test`, password: `Demo-${randomUUID()}` }
  await page.goto('/signup')
  for (const [label, value] of [['First Name', account.firstname], ['Last Name', account.lastname],
    ['Username', account.username], ['Email', account.email], ['Password', account.password], ['Confirm Password', account.password]]) {
    await page.getByLabel(label, { exact: true }).fill(value)
  }
  await page.getByRole('button', { name: 'Create Account', exact: true }).click()
  await expect(page).toHaveURL(/\/login$/)
  const signIn = async () => {
    await page.getByLabel('Email', { exact: true }).fill(account.email)
    await page.getByLabel('Password', { exact: true }).fill(account.password)
    const signedIn = page.waitForResponse(result => new URL(result.url()).pathname === '/auth/login' && result.request().method() === 'POST')
    await page.getByRole('button', { name: 'Sign In', exact: true }).click()
    const result = await signedIn
    expect(result.status()).toBe(200)
    return result.json()
  }
  const auth = await signIn()
  await expect(page).toHaveURL(/\/dashboard$/)
  const api = await request.newContext({ baseURL: gatewayURL, extraHTTPHeaders: { Authorization: `Bearer ${auth.accessToken}` } })
  try {
    await test.step('Edit private profile details through the account menu', async () => {
      await page.getByRole('button', { name: 'Open account menu for Alex Profile', exact: true }).click()
      await page.getByRole('menuitem', { name: 'Profile', exact: true }).click()
      await expect(page.getByRole('heading', { name: 'My profile', exact: true })).toBeVisible()
      await page.getByRole('textbox', { name: 'First name', exact: true }).fill('Avery')
      await page.getByLabel('Phone number', { exact: true }).fill('+1 555 0100')
      await page.getByLabel('About you', { exact: true }).fill('Building thoughtful team workflows.')
      await expect(page.getByLabel('Sign-in email', { exact: true })).toHaveJSProperty('readOnly', true)
      await page.getByRole('button', { name: 'Save profile', exact: true }).click()
      await expect(page.getByRole('status').filter({ hasText: 'Profile updated.' })).toBeVisible()
      await expect(page.getByRole('button', { name: 'Open account menu for Avery Profile', exact: true })).toHaveText('AP')
      await page.reload()
      await expect(page.getByRole('textbox', { name: 'First name', exact: true })).toHaveValue('Avery')
      await expect(page.getByLabel('About you', { exact: true })).toHaveValue('Building thoughtful team workflows.')
    })
    await test.step('Reject unsupported photos and persist a real raster upload', async () => {
      const input = page.getByLabel('Choose profile photo', { exact: true })
      await input.setInputFiles({ name: 'unsafe.svg', mimeType: 'image/svg+xml', buffer: Buffer.from('<svg/>') })
      await expect(page.getByRole('alert').filter({ hasText: 'Choose a PNG or JPEG photo.' })).toBeVisible()
      await input.setInputFiles(fileURLToPath(new URL('../src/assets/TaskFlow%20Gradient%20Flow%20Logo.png', import.meta.url)))
      await expect(page.getByRole('status').filter({ hasText: 'Profile photo updated.' })).toBeVisible()
      await page.reload()
      await expect(page.getByRole('button', { name: 'Remove photo', exact: true })).toBeVisible()
      const stored = await (await api.get('/api/v1/users/me')).json()
      expect(stored.avatarUrl).toMatch(/^data:image\/png;base64,/)
    })
    await test.step('Save preferences and apply them to real task deadlines', async () => {
      await page.getByRole('link', { name: 'Account settings', exact: true }).click()
      await page.getByRole('combobox', { name: 'Time zone', exact: true }).fill('America/Chicago')
      await page.getByRole('option', { name: 'America/Chicago', exact: true }).click()
      await page.getByRole('combobox', { name: 'Date format', exact: true }).click()
      await page.getByRole('option', { name: 'Year - month - day', exact: true }).click()
      await page.getByRole('combobox', { name: 'Page after sign-in', exact: true }).click()
      await page.getByRole('option', { name: 'Projects', exact: true }).click()
      await page.getByLabel('Use 24-hour time', { exact: true }).check()
      await page.getByRole('button', { name: 'Save settings', exact: true }).click()
      await expect(page.getByRole('status').filter({ hasText: 'Settings saved.' })).toBeVisible()
      await page.reload()
      await expect(page.getByRole('combobox', { name: 'Time zone', exact: true })).toHaveValue('America/Chicago')
      await expect(page.getByLabel('Use 24-hour time', { exact: true })).toBeChecked()
      const project = await api.post('/api/v1/projects', { data: { name: `Profile preferences ${suffix}`, visibility: 'PRIVATE' } })
      expect(project.status()).toBe(201)
      expect((await api.post('/api/v1/tasks', { data: { projectId: (await project.json()).id, title: 'Check local deadline', priority: 'MEDIUM', type: 'TASK', dueDate: '2026-01-01T00:30:00Z' } })).status()).toBe(201)
      await page.goto('/dashboard')
      await expect(page.getByRole('heading', { name: 'Welcome back, Avery!', exact: true })).toBeVisible()
      await expect(page.getByText('Due 2025-12-31, 18:30', { exact: true })).toBeVisible()
    })
    await test.step('Keep private details out of directory summaries and validate settings server-side', async () => {
      const privateProfile = await (await api.get('/api/v1/users/me')).json()
      expect(privateProfile).toMatchObject({ firstName: 'Avery', phone: '+1 555 0100', preferences: { timeZone: 'America/Chicago', startPage: 'PROJECTS' } })
      const directory = await (await api.get('/api/v1/users/search', { params: { q: account.email } })).json()
      expect(directory.content).toHaveLength(1)
      for (const field of ['phone', 'bio', 'preferences']) expect(directory.content[0]).not.toHaveProperty(field)
      for (const data of [{ timeZone: 'not/a-zone' }, { startPage: 'https://outside.example.test' }, { dateFormat: null }]) {
        expect((await api.put('/api/v1/users/me/preferences', { data })).status()).toBe(400)
      }
      expect((await api.put('/api/v1/users/me/avatar', { data: { avatarUrl: 'https://outside.example.test/avatar.svg' } })).status()).toBe(400)
      const anonymous = await request.newContext({ baseURL: gatewayURL })
      try { expect((await anonymous.get('/api/v1/users/me')).status()).toBe(401) }
      finally { await anonymous.dispose() }
    })
    await test.step('Restore saved names and the chosen start page on a new sign-in', async () => {
      await page.goto('/dashboard/settings')
      await page.getByRole('button', { name: 'Sign out of this browser', exact: true }).click()
      await expect(page).toHaveURL(/\/login$/)
      await signIn()
      await expect(page).toHaveURL(/\/dashboard\/projects$/)
      await expect(page.getByRole('button', { name: 'Open account menu for Avery Profile', exact: true })).toBeVisible()
      await page.goto('/dashboard/profile')
      await page.getByRole('button', { name: 'Remove photo', exact: true }).click()
      await expect(page.getByRole('status').filter({ hasText: 'Profile photo removed.' })).toBeVisible()
      await page.reload()
      await expect(page.getByRole('button', { name: 'Remove photo', exact: true })).toHaveCount(0)
    })
  } finally { await api.dispose() }
})
