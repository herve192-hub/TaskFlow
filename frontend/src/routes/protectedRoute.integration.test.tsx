/**
 * Verify router reactions to valid/missing/cleared display metadata and removal of
 * legacy token-based localStorage assumptions. Uses memory routing and browser storage;
 * the route guard itself does not validate the server cookie.
 */

import { act, cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import ProtectedRoute from './protectedRoute'
import { clearSession, saveSession } from '../features/auth/session'
import type { AuthUser } from '../features/auth/types/auth.types'

// Test the navigation/storage boundary with a real router; backend session validation is covered by full-stack E2E.
function renderProtectedPage() {
  render(
    <MemoryRouter initialEntries={['/dashboard']}>
      <Routes>
        <Route path="/login" element={<p>Sign in to TaskFlow</p>} />
        <Route path="/dashboard" element={
          <ProtectedRoute><p>Your TaskFlow dashboard</p></ProtectedRoute>
        } />
      </Routes>
    </MemoryRouter>,
  )
}

describe('protected routes with the router and browser storage', () => {
  beforeEach(() => { localStorage.clear(); sessionStorage.clear() })
  afterEach(() => {
    cleanup()
    localStorage.clear()
    sessionStorage.clear()
  })

  it('redirects an unauthenticated visitor to the sign-in page', async () => {
    renderProtectedPage()
    expect(await screen.findByText('Sign in to TaskFlow')).toBeTruthy()
    expect(screen.queryByText('Your TaskFlow dashboard')).toBeNull()
  })

  it('renders the protected page when sign-in metadata is available', async () => {
    saveSession({ id: 'user-1', firstname: 'Test', lastname: 'User' } as AuthUser)
    renderProtectedPage()
    expect(await screen.findByText('Your TaskFlow dashboard')).toBeTruthy()
    expect(screen.queryByText('Sign in to TaskFlow')).toBeNull()
  })

  it('a legacy token in localStorage does not authenticate a visitor', async () => {
    localStorage.setItem('accessToken', 'legacy-token')
    renderProtectedPage()
    expect(await screen.findByText('Sign in to TaskFlow')).toBeTruthy()
  })

  it('immediately removes a mounted protected page when session metadata is cleared', async () => {
    saveSession({ id: 'user-1', firstname: 'Test', lastname: 'User' } as AuthUser)
    renderProtectedPage()
    expect(await screen.findByText('Your TaskFlow dashboard')).toBeTruthy()
    act(() => clearSession())
    expect(await screen.findByText('Sign in to TaskFlow')).toBeTruthy()
    expect(screen.queryByText('Your TaskFlow dashboard')).toBeNull()
  })
})
