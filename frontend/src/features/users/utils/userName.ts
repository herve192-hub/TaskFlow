/**
 * Display-name normalization for profile camelCase fields and auth lowercase fields.
 * Trim names, then use fullName or a caller-provided label when needed. Membership
 * screens should pass a human-readable fallback rather than exposing internal IDs.
 * This presentation helper does not resolve or validate account identity.
 */

interface PersonName {
  firstName?: string | null;
  lastName?: string | null;
  firstname?: string | null;
  lastname?: string | null;
  fullName?: string | null;
}

/** Normalize auth's lowercase name fields and profile-service camelCase fields into one display name. */
export function userDisplayName(user?: PersonName | null, fallback = "Name unavailable"): string {
  const firstName = (user?.firstName ?? user?.firstname)?.trim();
  const lastName = (user?.lastName ?? user?.lastname)?.trim();
  return [firstName, lastName].filter(Boolean).join(" ") || user?.fullName?.trim() || fallback;
}
