/**
 * Defaults and presentation rules for saved account preferences. Start pages map
 * to an allowlist of local routes, preventing arbitrary redirects after sign-in.
 * Intl formats ISO timestamps in the saved timezone with UTC fallback; malformed
 * instants produce a readable placeholder. No helper changes stored task instants.
 */

import type { UserPreferences } from "../types/user.types";

export const defaultPreferences: UserPreferences = {
  timeZone: "UTC", dateFormat: "MONTH_DAY_YEAR", startPage: "DASHBOARD", use24HourTime: false,
};

export function startPagePath(preferences?: UserPreferences): string {
  const paths = { DASHBOARD: "/dashboard", PROJECTS: "/dashboard/projects", TASKS: "/dashboard/tasks", TEAMS: "/dashboard/teams" };
  return preferences && Object.hasOwn(paths, preferences.startPage) ? paths[preferences.startPage] : paths.DASHBOARD;
}

/** All workspace dates use the saved account timezone; timestamps remain UTC on the API. */
export function formatAccountDate(value: string, preferences = defaultPreferences, includeTime = false): string {
  const date = new Date(value);
  if (!Number.isFinite(date.getTime())) return "Date unavailable";
  const options: Intl.DateTimeFormatOptions = { timeZone: preferences.timeZone, year: "numeric", month: "2-digit", day: "2-digit" };
  let parts: Intl.DateTimeFormatPart[];
  try { parts = new Intl.DateTimeFormat("en-US", options).formatToParts(date); }
  catch { parts = new Intl.DateTimeFormat("en-US", { ...options, timeZone: "UTC" }).formatToParts(date); }
  const part = (type: string) => parts.find(item => item.type === type)?.value ?? "";
  const dateText = preferences.dateFormat === "YEAR_MONTH_DAY" ? `${part("year")}-${part("month")}-${part("day")}`
    : preferences.dateFormat === "DAY_MONTH_YEAR" ? `${part("day")}/${part("month")}/${part("year")}`
    : `${part("month")}/${part("day")}/${part("year")}`;
  if (!includeTime) return dateText;
  let time: string;
  const timeOptions: Intl.DateTimeFormatOptions = { timeZone: preferences.timeZone, hour: "2-digit", minute: "2-digit", hour12: !preferences.use24HourTime };
  try { time = new Intl.DateTimeFormat("en-US", timeOptions).format(date); }
  catch { time = new Intl.DateTimeFormat("en-US", { ...timeOptions, timeZone: "UTC" }).format(date); }
  return `${dateText}, ${time}`;
}
