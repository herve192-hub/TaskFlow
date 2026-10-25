/**
 * Provide date formatting tied to reactive account preferences. Session changes
 * re-render consumers, so saved timezone/date/time settings apply across dashboard,
 * projects, tasks, and profile without a reload. preferences.ts handles defaults
 * and invalid values; API timestamps remain unmodified.
 */

import { useCurrentUser } from "../../auth/hooks/useCurrentUser";
import { formatAccountDate } from "../utils/preferences";

export function useDateFormatter() {
  const preferences = useCurrentUser()?.preferences;
  return (value: string, includeTime = false) => formatAccountDate(value, preferences, includeTime);
}
