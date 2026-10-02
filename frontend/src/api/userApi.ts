import api from "./axios";
import type { UserSearchPage, UserSummary } from "../features/users/types/user.types";

export const getCurrentUser = async (): Promise<UserSummary> => {
  return (await api.get<UserSummary>("/api/v1/users/me")).data;
};

export const createCurrentUser = async (request: {
  firstName: string; lastName: string; email: string;
}): Promise<UserSummary> => {
  return (await api.post<UserSummary>("/api/v1/users/me", request)).data;
};

export const searchUsers = async (query: string, page = 0): Promise<UserSearchPage> => {
  return (await api.get<UserSearchPage>("/api/v1/users/search", {
    params: { q: query, page, size: 20, sort: "firstName,asc" },
  })).data;
};

export const lookupUsers = async (authUserIds: string[]): Promise<UserSummary[]> => {
  if (!authUserIds.length) return [];
  return (await api.get<UserSummary[]>("/api/v1/users/lookup", {
    params: { authUserIds: authUserIds.join(",") },
  })).data;
};
