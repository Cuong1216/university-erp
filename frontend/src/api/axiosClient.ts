import axios, { AxiosError } from 'axios';
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { useAuthStore } from '../store/useAuthStore';

type OnUnauthorizedCallback = () => void;
let onUnauthorizedCallback: OnUnauthorizedCallback | null = null;

export const setOnUnauthorizedCallback = (cb: OnUnauthorizedCallback) => {
  onUnauthorizedCallback = cb;
};

// Cờ ngăn việc redirect lặp lại nhiều lần nếu nhiều request đồng thời lỗi 401
let isRedirectingToLogin = false;

const getBaseUrl = () => {
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL;
  }
  if (import.meta.env.PROD) {
    return '/api/v1';
  }
  return 'http://localhost:8080/api/v1';
};

export const axiosClient = axios.create({
  baseURL: getBaseUrl(),
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 1. Request Interceptor: Tự động gắn Authorization Bearer Token
axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = useAuthStore.getState().accessToken;
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// 2. Response Interceptor: Xử lý lỗi tập trung & tự động bắt 401 Unauthorized
axiosClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const status = error.response?.status;

    // Trường hợp 401: Hết hạn Token hoặc Token không hợp lệ
    if (status === 401) {
      if (!isRedirectingToLogin) {
        isRedirectingToLogin = true;
        
        // Xóa state trong store và localStorage
        useAuthStore.getState().logout();

        if (onUnauthorizedCallback) {
          onUnauthorizedCallback();
        } else if (window.location.pathname !== '/login') {
          window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname)}`;
        }

        setTimeout(() => {
          isRedirectingToLogin = false;
        }, 1000);
      }
    }

    return Promise.reject(error);
  }
);
