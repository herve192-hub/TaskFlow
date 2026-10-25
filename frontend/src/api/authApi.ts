/**
 * Thin /auth login and registration calls using the shared cookie/CSRF transport.
 * Returns the server envelope without persisting credentials; authService decides
 * which account metadata to expose to React. Endpoint changes must match AuthController.
 */

import api from "./axios";

import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
} from "../features/auth/types/auth.types";

/** Account HTTP calls return the API payload; authService handles display metadata and profile setup. */
export const register = async (
  request: RegisterRequest
): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>(
    "/auth/register",
    request
  );

  return response.data;
};

export const login = async (
  request: LoginRequest
): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>(
    "/auth/login",
    request
  );

  return response.data;
};
