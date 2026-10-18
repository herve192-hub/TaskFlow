import { describe, expect, it } from "vitest";
import { defaultPreferences, formatAccountDate, startPagePath } from "./preferences";
import type { UserPreferences } from "../types/user.types";

describe("account date and navigation preferences", () => {
  it("uses the selected timezone across a date and year boundary", () => {
    const settings: UserPreferences = { ...defaultPreferences, timeZone: "America/Los_Angeles", dateFormat: "YEAR_MONTH_DAY", use24HourTime: true };
    expect(formatAccountDate("2026-01-01T00:30:00Z", settings, true)).toBe("2025-12-31, 16:30");
  });
  it("supports day-first dates and 12-hour time", () => {
    expect(formatAccountDate("2026-10-16T17:05:00Z", { ...defaultPreferences, dateFormat: "DAY_MONTH_YEAR" }, true))
      .toBe("16/10/2026, 05:05 PM");
  });
  it("handles invalid timestamps and a stale timezone preference safely", () => {
    expect(formatAccountDate("invalid")).toBe("Date unavailable");
    expect(formatAccountDate("2026-01-01T00:30:00Z", { ...defaultPreferences, timeZone: "invalid" })).toBe("01/01/2026");
  });
  it("allows only built-in destinations after sign-in", () => {
    expect(startPagePath({ ...defaultPreferences, startPage: "PROJECTS" })).toBe("/dashboard/projects");
    for (const startPage of ["https://example.test", "__proto__", "constructor"]) {
      expect(startPagePath({ ...defaultPreferences, startPage } as UserPreferences)).toBe("/dashboard");
    }
  });
});
