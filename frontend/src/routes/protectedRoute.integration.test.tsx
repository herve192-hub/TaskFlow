import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import ProtectedRoute from './protectedRoute'
import { saveSession } from '../features/auth/session'
import type { AuthUser } from '../features/auth/types/auth.types'

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
})
