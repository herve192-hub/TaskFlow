/**
 * Verify /me-to-session synchronization, private-field exclusion, and stale-account
 * protection. Mock HTTP responses exercise account ID matching so a previous user's
 * late profile response cannot overwrite the current identity.
 */

import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as api from "../../../api/userApi";
import { currentUser, clearSession, saveSession } from "../../auth/session";
import type { AuthUser } from "../../auth/types/auth.types";
import type { UserProfile } from "../types/user.types";
import { defaultPreferences } from "../utils/preferences";
import { userService } from "./userService";

vi.mock("../../../api/userApi", () => ({
  getCurrentUser: vi.fn(), createCurrentUser: vi.fn(), updateCurrentUser: vi.fn(), updateCurrentAvatar: vi.fn(),
  updateCurrentPreferences: vi.fn(), lookupUsers: vi.fn(), searchUsers: vi.fn(),
}));

const account = { id: "account-alex", firstname: "Alex", lastname: "Morgan", email: "alex@example.test", username: "alex_morgan" } as AuthUser;
const profile: UserProfile = { id: "profile-alex", authUserId: account.id, firstName: "Avery", lastName: "Morgan", fullName: "Avery Morgan",
  email: account.email, phone: "+1 555 0100", bio: "Private profile biography.", avatarUrl: null, preferences: defaultPreferences,
  createdAt: "2026-01-01T00:00:00Z", updatedAt: "2026-01-01T00:00:00Z", status: "ACTIVE" };

describe("profile synchronization with the active account", () => {
  beforeEach(() => { vi.clearAllMocks(); clearSession(); saveSession(account); });
  afterEach(() => clearSession());
  it("restores saved profile names and preferences at sign-in without caching private contact fields", async () => {
    vi.mocked(api.getCurrentUser).mockResolvedValue(profile);
    await userService.ensureCurrentProfile(account);
    expect(currentUser()?.firstname).toBe("Avery");
    expect(currentUser()?.preferences).toEqual(defaultPreferences);
    const metadata = sessionStorage.getItem("taskflow.user")!;
    expect(metadata).not.toContain(profile.bio!);
    expect(metadata).not.toContain(profile.phone!);
    expect(api.createCurrentUser).not.toHaveBeenCalled();
  });
  it("does not let a previous account's late save overwrite a newly signed-in identity", async () => {
    let finish: (value: UserProfile) => void = () => {};
    vi.mocked(api.updateCurrentUser).mockReturnValue(new Promise(resolve => { finish = resolve; }));
    const update = userService.updateProfile({ firstName: "Avery", lastName: "Morgan", bio: "", phone: "" });
    saveSession({ ...account, id: "account-jordan", firstname: "Jordan", email: "jordan@example.test" });
    finish(profile);
    await update;
    expect(currentUser()?.id).toBe("account-jordan");
    expect(currentUser()?.firstname).toBe("Jordan");
  });
});
