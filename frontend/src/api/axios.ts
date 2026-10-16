import axios, { AxiosHeaders } from "axios";
import { clearSession } from '../features/auth/session'

// Browser authentication uses the server's HttpOnly cookie; VITE_* settings are public build inputs.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/",
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

// Share one nonce-initialization request when several writes start before the CSRF cookie exists.
let csrfRequest: Promise<unknown> | null = null

/** Read the JavaScript-accessible nonce cookie; the access-token cookie remains HttpOnly. */
function csrfCookie(): string | undefined {
  for (const name of ['__Host-XSRF-TOKEN', 'XSRF-TOKEN']) {
    const cookie = document.cookie.split('; ').find((value) => value.startsWith(`${name}=`))
    if (cookie) return decodeURIComponent(cookie.slice(name.length + 1))
  }
}

// Unsafe requests carry the cookie/header nonce pair required by the backend security chain.
api.interceptors.request.use(async (config) => {
  if (!['get', 'head', 'options'].includes(config.method ?? 'get')) {
    if (!csrfCookie()) {
      csrfRequest ??= api.get('/auth/csrf').finally(() => { csrfRequest = null })
      await csrfRequest
    }
    config.headers = config.headers ?? new AxiosHeaders();
    const headers = config.headers as AxiosHeaders;
    const nonce = csrfCookie()
    if (!nonce) throw new Error('Unable to initialize request protection')
    headers.set('X-XSRF-TOKEN', nonce)
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      // Expired/rejected authentication invalidates navigation metadata as well as the request.
      clearSession()

      const currentPath = window.location.pathname;

      // Leave credential-entry pages in place so a failed login cannot cause a redirect loop.
      if (currentPath !== '/' && !currentPath.startsWith("/login") && !currentPath.startsWith("/signup")) {
        window.location.assign("/login");
      }
    }

    return Promise.reject(error);
  }
);

export default api;
