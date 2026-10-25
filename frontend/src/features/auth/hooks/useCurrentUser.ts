/**
 * React bridge to the session display cache. useSyncExternalStore subscribes to
 * identity changes and useMemo parses each snapshot once. Topbar, dashboard, and
 * route guards update together after login, logout, or a saved profile change.
 * This hook reads metadata; the server validates authentication on every API call.
 */

import { useMemo, useSyncExternalStore } from 'react'
import { currentUser, sessionSnapshot, subscribeToSession } from '../session'

/** Observe display metadata only; API authentication remains in the server's HttpOnly cookie. */
export function useCurrentUser() {
  const snapshot = useSyncExternalStore(subscribeToSession, sessionSnapshot)
  return useMemo(() => currentUser(snapshot), [snapshot])
}
