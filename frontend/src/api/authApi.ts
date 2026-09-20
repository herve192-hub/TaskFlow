import api from "./axios";

import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
} from "../features/auth/types/auth.types";

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