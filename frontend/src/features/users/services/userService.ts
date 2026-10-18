import axios from "axios";
import { createCurrentUser, getCurrentUser, lookupUsers, searchUsers, updateCurrentUser, updateCurrentAvatar, updateCurrentPreferences } from "../../../api/userApi";
import type { AuthUser } from "../../auth/types/auth.types";
import { currentUser, saveSession } from "../../auth/session";
import type { UserPreferences, UserProfile, UpdateProfileRequest } from "../types/user.types";

/** Update display metadata only when this response still belongs to the active account. */
function syncProfile(profile: UserProfile): UserProfile {
  const user = currentUser();
  if (user?.id === profile.authUserId) saveSession({
    ...user, firstname: profile.firstName ?? "", lastname: profile.lastName ?? "",
    profileImage: profile.avatarUrl, preferences: profile.preferences,
  });
  return profile;
}

export const userService = {
  searchUsers,
  lookupUsers,

  async getCurrentProfile(): Promise<UserProfile> {
    return syncProfile(await getCurrentUser());
  },

  async updateProfile(request: UpdateProfileRequest): Promise<UserProfile> {
    return syncProfile(await updateCurrentUser(request));
  },

  async updateAvatar(avatarUrl: string | null): Promise<UserProfile> {
    return syncProfile(await updateCurrentAvatar(avatarUrl));
  },

  async updatePreferences(preferences: UserPreferences): Promise<UserProfile> {
    return syncProfile(await updateCurrentPreferences(preferences));
  },

  /** Create a missing profile after sign-in; only an actual 404 triggers provisioning. */
  async ensureCurrentProfile(user: AuthUser): Promise<void> {
    try {
      syncProfile(await getCurrentUser());
      return;
    } catch (error) {
      if (!axios.isAxiosError(error) || error.response?.status !== 404) throw error;
    }

    try {
      syncProfile(await createCurrentUser({ firstName: user.firstname, lastName: user.lastname, email: user.email }));
    } catch (error) {
      // Another sign-in may have created the same profile while this request was in flight.
      if (!axios.isAxiosError(error) || ![400, 409].includes(error.response?.status ?? 0)) throw error;
      syncProfile(await getCurrentUser());
    }
  },
};
