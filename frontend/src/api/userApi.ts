/**
 * Transport contract for the user service's /me profile, avatar, preferences,
 * active-directory search, and bounded batch lookup endpoints. Lookup keys are
 * canonical auth account IDs, not user-profile document IDs. Private /me data
 * must not be substituted for the public summaries used by membership screens.
 */

import api from "./axios";
import type { UserPreferences, UserProfile, UpdateProfileRequest, UserSearchPage, UserSummary } from "../features/users/types/user.types";

/** /me resolves the profile from the authenticated account rather than a browser-supplied user ID. */
export const getCurrentUser = async (): Promise<UserProfile> => {
  return (await api.get<UserProfile>("/api/v1/users/me")).data;
};

export const createCurrentUser = async (request: {
  firstName: string; lastName: string; email: string;
}): Promise<UserProfile> => {
  return (await api.post<UserProfile>("/api/v1/users/me", request)).data;
};

export const updateCurrentUser = async (request: UpdateProfileRequest): Promise<UserProfile> =>
  (await api.put<UserProfile>("/api/v1/users/me", request)).data;

export const updateCurrentAvatar = async (avatarUrl: string | null): Promise<UserProfile> =>
  (await api.put<UserProfile>("/api/v1/users/me/avatar", { avatarUrl })).data;

export const updateCurrentPreferences = async (preferences: UserPreferences): Promise<UserProfile> =>
  (await api.put<UserProfile>("/api/v1/users/me/preferences", preferences)).data;

export const searchUsers = async (query: string, page = 0): Promise<UserSearchPage> => {
  return (await api.get<UserSearchPage>("/api/v1/users/search", {
    params: { q: query, page, size: 20, sort: "firstName,asc" },
  })).data;
};

/** Resolve canonical auth IDs to directory summaries; callers must respect the backend's 100-ID limit. */
export const lookupUsers = async (authUserIds: string[]): Promise<UserSummary[]> => {
  if (!authUserIds.length) return [];
  return (await api.get<UserSummary[]>("/api/v1/users/lookup", {
    params: { authUserIds: authUserIds.join(",") },
  })).data;
};
