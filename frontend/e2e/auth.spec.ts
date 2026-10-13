import { expect, test } from '@playwright/test'

test('the production build renders the sign-in form', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByLabel('Email', { exact: true })).toBeVisible()
  await expect(page.getByLabel('Password', { exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Sign In', exact: true })).toBeVisible()
})

test('a visitor can navigate from sign-in to registration', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('link', { name: 'Create one' }).click()
  await expect(page).toHaveURL(/\/signup$/)
  await expect(page.getByLabel('First Name', { exact: true })).toBeVisible()
  await expect(page.getByLabel('Last Name', { exact: true })).toBeVisible()
})

test('a direct request to a protected page returns to sign-in', async ({ page }) => {
  await page.goto('/dashboard/projects')
  await expect(page).toHaveURL('http://127.0.0.1:4173/')
  await expect(page.getByRole('button', { name: 'Sign In', exact: true })).toBeVisible()
})
