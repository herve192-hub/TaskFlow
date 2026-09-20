import axios, { AxiosHeaders } from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");

  if (token) {
    config.headers = config.headers ?? new AxiosHeaders();
    const headers = config.headers as AxiosHeaders;

    headers.set("Authorization", `Bearer ${token}`);
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("user");

      const currentPath = window.location.pathname;

      if (!currentPath.startsWith("/login") && !currentPath.startsWith("/signup")) {
        window.location.assign("/login");
      }
    }

    return Promise.reject(error);
  }
);

export default api;