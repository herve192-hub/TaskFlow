import type { AuthUser } from './types/auth.types'

const sessionKey = 'taskflow.user'
const sessionChanged = 'taskflow:session-changed'

// Remove credentials left by older builds; authentication now uses HttpOnly cookies.
for (const key of ['accessToken', 'refreshToken', 'user']) localStorage.removeItem(key)

/** Keep account display metadata for this tab; never persist access or refresh tokens here. */
export function saveSession(user?: AuthUser): void {
  if (user) {
    sessionStorage.setItem(sessionKey, JSON.stringify(user))
    window.dispatchEvent(new Event(sessionChanged))
  }
}

export function clearSession(): void {
  sessionStorage.removeItem(sessionKey)
  window.dispatchEvent(new Event(sessionChanged))
}

/** A primitive snapshot stays stable between changes for React's external-store subscription. */
export function sessionSnapshot(): string | null {
  try {
    return sessionStorage.getItem(sessionKey)
  } catch {
    return null
  }
}

/** Notify mounted views after sign-in/sign-out and same-tab session-storage changes. */
export function subscribeToSession(onChange: () => void): () => void {
  const onStorage = (event: StorageEvent) => {
    if (event.storageArea === sessionStorage && (event.key === sessionKey || event.key === null)) onChange()
  }
  window.addEventListener(sessionChanged, onChange)
  window.addEventListener('storage', onStorage)
  return () => {
    window.removeEventListener(sessionChanged, onChange)
    window.removeEventListener('storage', onStorage)
  }
}

/** Recover a basic metadata shape, treating malformed storage as an absent navigation session. */
export function currentUser(snapshot = sessionSnapshot()): AuthUser | null {
  try {
    const user = JSON.parse(snapshot ?? 'null') as AuthUser | null
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
