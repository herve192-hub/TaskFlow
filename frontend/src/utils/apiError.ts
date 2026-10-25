/**
 * Normalize expected Axios error payloads for caller-specific UI feedback. Prefer
 * a string message, then RFC-style detail, then a safe supplied fallback.
 * Pages decide how to display the result; this helper neither retries requests nor
 * logs request/response bodies that may contain private data.
 */

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
