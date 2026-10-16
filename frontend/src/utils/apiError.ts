import axios from "axios";

/** Read a backend message/problem detail when available; otherwise use the caller's UI-specific fallback. */
export function apiErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data;
    if (typeof data?.message === "string") return data.message;
    if (typeof data?.detail === "string") return data.detail;
  }
  return fallback;
}
