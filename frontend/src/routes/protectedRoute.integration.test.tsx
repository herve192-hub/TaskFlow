import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import ProtectedRoute from './protectedRoute'

function renderProtectedPage() {
  render(
    <MemoryRouter initialEntries={['/dashboard']}>
      <Routes>
        <Route path="/" element={<p>Sign in to TaskFlow</p>} />
        <Route path="/dashboard" element={
          <ProtectedRoute><p>Your TaskFlow dashboard</p></ProtectedRoute>
        } />
      </Routes>
    </MemoryRouter>,
  )
}

describe('protected routes with the router and browser storage', () => {
  beforeEach(() => localStorage.clear())
  afterEach(() => {
    cleanup()
    localStorage.clear()
  })

  it('redirects an unauthenticated visitor to the sign-in page', async () => {
    renderProtectedPage()
    expect(await screen.findByText('Sign in to TaskFlow')).toBeTruthy()
    expect(screen.queryByText('Your TaskFlow dashboard')).toBeNull()
  })

  it('renders the protected page when an access token is stored', async () => {
    localStorage.setItem('accessToken', 'test-session')
    renderProtectedPage()
    expect(await screen.findByText('Your TaskFlow dashboard')).toBeTruthy()
    expect(screen.queryByText('Sign in to TaskFlow')).toBeNull()
  })

  it('redirects when the stored access token is empty', async () => {
    localStorage.setItem('accessToken', '')
    renderProtectedPage()
    expect(await screen.findByText('Sign in to TaskFlow')).toBeTruthy()
  })
})
