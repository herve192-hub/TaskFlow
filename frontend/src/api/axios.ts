import axios, { AxiosHeaders } from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8081",
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

export default api;