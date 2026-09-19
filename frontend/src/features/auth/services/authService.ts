import {
  login as loginApi,
  register as registerApi,
} from "../../../api/authApi";

import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
} from "../types/auth.types";

const saveSession = (auth: AuthResponse): void => {
  localStorage.setItem("accessToken", auth.accessToken);
  localStorage.setItem("refreshToken", auth.refreshToken);
  localStorage.setItem("user", JSON.stringify(auth.user));
};

export const authService = {
  async register(request: RegisterRequest): Promise<AuthResponse> {
    const auth = await registerApi(request);

    saveSession(auth);

    return auth;
  },

  async login(request: LoginRequest): Promise<AuthResponse> {
    const auth = await loginApi(request);

    saveSession(auth);

    return auth;
  },

  logout(): void {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
  },

  getAccessToken(): string | null {
    return localStorage.getItem("accessToken");
  },

  isAuthenticated(): boolean {
    return Boolean(localStorage.getItem("accessToken"));
  },
};