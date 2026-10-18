import { useMemo, useSyncExternalStore } from 'react'
import { currentUser, sessionSnapshot, subscribeToSession } from '../session'

/** Observe display metadata only; API authentication remains in the server's HttpOnly cookie. */
export function useCurrentUser() {
  const snapshot = useSyncExternalStore(subscribeToSession, sessionSnapshot)
  return useMemo(() => currentUser(snapshot), [snapshot])
}
