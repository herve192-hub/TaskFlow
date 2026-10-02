import axios from "axios";

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data;
    if (typeof data?.message === "string") return data.message;
    if (typeof data?.detail === "string") return data.detail;
  }
  return fallback;
}
