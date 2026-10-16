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
import api from '../../../api/axios'
import { clearSession, currentUser, isAuthenticated, saveSession } from '../session'


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

    saveSession(auth.user);
    await initializeProfile(auth);

    return auth;
  },

  async login(request: LoginRequest): Promise<AuthResponse> {
    const auth = await loginApi(request);

    saveSession(auth.user);
    await initializeProfile(auth);

    return auth;
  },

  async forgotPassword(): Promise<void> {
    throw new Error('Password recovery is not configured. Contact your administrator.')
  },

  async logout(): Promise<void> {
    try {
      await api.post('/auth/logout')
    } finally {
      clearSession()
    }
  },

  getCurrentUser(): AuthUser | null {
    return currentUser()
  },

  isAuthenticated(): boolean {
    return isAuthenticated()
  },
};
