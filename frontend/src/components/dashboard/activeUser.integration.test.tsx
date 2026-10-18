import { act, cleanup, fireEvent, render, screen, within } from '@testing-library/react'
import { ThemeProvider } from '@mui/material/styles'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { clearSession, saveSession } from '../../features/auth/session'
import type { AuthUser } from '../../features/auth/types/auth.types'
import { theme } from '../../theme/theme'
import Dashboard from './Dashboard'
import Topbar from './Topbar'

// Isolate identity presentation from workspace network requests; use the real session store and router.
vi.mock('../../features/tasks/hooks/useWorkspace', () => ({
  useWorkspace: () => ({ data: { projects: [], tasks: [] }, loading: false, error: null, refresh: vi.fn() }),
}))

const alex: AuthUser = {
  id: 'account-alex', firstname: 'Alex', lastname: 'Morgan', username: 'alex_morgan',
  email: 'alex.morgan@example.test', roles: ['ROLE_USER'], provider: 'LOCAL',
  emailVerified: false, createdAt: '2026-01-01T00:00:00Z',
}
const jordan: AuthUser = {
  ...alex, id: 'account-jordan', firstname: 'Jordan', lastname: 'Lee', username: 'jordan_lee',
  email: 'jordan.lee@example.test',
}

function renderAccountViews() {
  return render(<MemoryRouter><ThemeProvider theme={theme}>
    <Topbar collapsed={false} />
    <Dashboard />
  </ThemeProvider></MemoryRouter>)
}

describe('the active account in the header and dashboard', () => {
  beforeEach(() => { localStorage.clear(); clearSession() })
  afterEach(() => { cleanup(); clearSession(); localStorage.clear() })

  it('shows the signed-in name, email, initials, and personalized welcome after a reload', () => {
    saveSession(alex)
    const firstRender = renderAccountViews()
    firstRender.unmount()
    renderAccountViews()

    expect(screen.getByRole('heading', { name: 'Welcome back, Alex!' })).toBeTruthy()
    const account = screen.getByRole('button', { name: 'Open account menu for Alex Morgan' })
    expect(account.textContent).toBe('AM')
    fireEvent.click(account)
    const menu = within(screen.getByRole('menu'))
    expect(menu.getByText('Alex Morgan', { exact: true })).toBeTruthy()
    expect(menu.getByText(alex.email, { exact: true })).toBeTruthy()
  })

  it('replaces the previous identity immediately when the active session changes', async () => {
    saveSession(alex)
    renderAccountViews()
    fireEvent.click(screen.getByRole('button', { name: 'Open account menu for Alex Morgan' }))

    act(() => saveSession(jordan))

    const menu = within(screen.getByRole('menu'))
    expect(menu.getByText('Jordan Lee', { exact: true })).toBeTruthy()
    expect(menu.getByText(jordan.email, { exact: true })).toBeTruthy()
    expect(screen.queryByText(alex.email, { exact: true })).toBeNull()
    fireEvent.keyDown(screen.getByRole('menu'), { key: 'Escape' })
    expect(await screen.findByRole('heading', { name: 'Welcome back, Jordan!' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Open account menu for Jordan Lee' }).textContent).toBe('JL')

    act(() => clearSession())
    expect(screen.getByRole('heading', { name: 'Welcome back!' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Open account menu for Your account' }).textContent).toBe('?')
    expect(screen.queryByText('Welcome back, Jordan!', { exact: true })).toBeNull()
  })

  it('uses the active username when name fields are blank', () => {
    saveSession({ ...alex, firstname: ' ', lastname: '' })
    renderAccountViews()
    expect(screen.getByRole('heading', { name: 'Welcome back, alex_morgan!' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Open account menu for alex_morgan' }).textContent).toBe('A')
  })

  it('uses the signed-in profile image when one is available', () => {
    saveSession({ ...jordan, profileImage: '/avatars/jordan.png' })
    renderAccountViews()
    const account = screen.getByRole('button', { name: 'Open account menu for Jordan Lee' })
    expect(within(account).getByRole('img', { name: 'Jordan Lee' }).getAttribute('src')).toBe('/avatars/jordan.png')
  })
})
