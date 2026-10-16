import type { AuthUser } from './types/auth.types'

const sessionKey = 'taskflow.user'

// Remove credentials left by older builds; authentication now uses HttpOnly cookies.
for (const key of ['accessToken', 'refreshToken', 'user']) localStorage.removeItem(key)

export function saveSession(user?: AuthUser): void {
  if (user) sessionStorage.setItem(sessionKey, JSON.stringify(user))
}

export function clearSession(): void {
  sessionStorage.removeItem(sessionKey)
}

export function currentUser(): AuthUser | null {
  try {
    const user = JSON.parse(sessionStorage.getItem(sessionKey) ?? 'null') as AuthUser | null
    return user && typeof user.id === 'string' && typeof user.firstname === 'string'
      && typeof user.lastname === 'string' ? user : null
  } catch {
    return null
  }
}

// This controls navigation only. Every backend request verifies the server cookie.
export function isAuthenticated(): boolean {
  return currentUser() !== null
}
