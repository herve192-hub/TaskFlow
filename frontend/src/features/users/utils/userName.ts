interface PersonName {
  firstName?: string | null;
  lastName?: string | null;
  firstname?: string | null;
  lastname?: string | null;
  fullName?: string | null;
}

export function userDisplayName(user?: PersonName | null, fallback = "Name unavailable"): string {
  const firstName = (user?.firstName ?? user?.firstname)?.trim();
  const lastName = (user?.lastName ?? user?.lastname)?.trim();
  return [firstName, lastName].filter(Boolean).join(" ") || user?.fullName?.trim() || fallback;
}
