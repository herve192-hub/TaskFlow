import {
  login as loginApi,
  register as registerApi,
} from "../../../api/authApi";

import type {
  AuthResponse,
  AuthUser,
  LoginRequest,
  RegisterRequest,
} from "../types/auth.types";
import { userService } from "../../users/services/userService";

const saveSession = (auth: AuthResponse): void => {
  localStorage.setItem("accessToken", auth.accessToken);
  localStorage.setItem("refreshToken", auth.refreshToken);
  localStorage.setItem("user", JSON.stringify(auth.user));
};

const initializeProfile = async (auth: AuthResponse): Promise<void> => {
  if (!auth.user) return;
  try {
    await userService.ensureCurrentProfile(auth.user);
  } catch {
    // Keep sign-in available during a user-service outage; the next sign-in retries.
    console.warn("TaskFlow profile initialization is unavailable. Team membership may require signing in again.");
  }
};

export const authService = {
  async register(request: RegisterRequest): Promise<AuthResponse> {
    const auth = await registerApi(request);

    saveSession(auth);
    await initializeProfile(auth);

    return auth;
  },

  async login(request: LoginRequest): Promise<AuthResponse> {
    const auth = await loginApi(request);

    saveSession(auth);
    await initializeProfile(auth);

    return auth;
  },

  async forgotPassword(email: string): Promise<void> {
    await Promise.resolve();
    console.log("Reset email sent to:", email);
  },

  logout(): void {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
  },

  getAccessToken(): string | null {
    return localStorage.getItem("accessToken");
  },

  getCurrentUser(): AuthUser | null {
    try {
      const user = JSON.parse(localStorage.getItem("user") ?? "null") as AuthUser | null;
      return user && typeof user.id === "string"
        && typeof user.firstname === "string" && typeof user.lastname === "string"
        ? user : null;
    } catch {
      return null;
    }
  },

  isAuthenticated(): boolean {
    return Boolean(localStorage.getItem("accessToken"));
  },
};
