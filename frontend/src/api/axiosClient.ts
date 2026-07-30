import axios, { AxiosError } from 'axios';
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { useAuthStore } from '../store/useAuthStore';
import { useNotificationStore } from '../store/useNotificationStore';

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

// 1. Request Interceptor: Tự động gắn Authorization Bearer Token & X-Tenant-ID
axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = useAuthStore.getState().accessToken;
    if (config.headers) {
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      // Đọc tenantId từ localStorage (nếu có, ví dụ do người dùng chọn trường đại học khi đăng nhập)
      const tenantId = localStorage.getItem('tenantId') || 'public';
      config.headers['X-Tenant-ID'] = tenantId;
      config.headers['X-Request-ID'] = crypto.randomUUID().split('-')[0].toUpperCase();
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
    const config = error.config as InternalAxiosRequestConfig & { _retryCount?: number };

    // Retry mechanism cho network errors hoặc 5xx trên GET requests
    const isNetworkError = !error.response && (error.code === 'ECONNABORTED' || error.code === 'ERR_NETWORK');
    const isServerError = status && status >= 500;
    const isGetRequest = config?.method?.toLowerCase() === 'get';

    if ((isNetworkError || isServerError) && isGetRequest && config) {
      config._retryCount = config._retryCount || 0;
      if (config._retryCount < 2) {
        config._retryCount += 1;
        // Exponential backoff: 1s, 2s
        const backoff = Math.pow(2, config._retryCount - 1) * 1000;
        await new Promise((resolve) => setTimeout(resolve, backoff));
        return axiosClient(config);
      }
    }

    if (status) {
      if (status === 401) {
        const originalRequest = config;
        const refreshToken = useAuthStore.getState().refreshToken;

        // Bỏ qua retry nếu API lỗi là /auth/refresh để tránh loop vô hạn
        if (originalRequest.url !== '/auth/refresh' && refreshToken && !isRedirectingToLogin) {
          try {
            const res = await axios.post(`${getBaseUrl()}/auth/refresh`, { refreshToken });
            const { token, refreshToken: newRefreshToken } = res.data;
            useAuthStore.getState().setTokens(token, newRefreshToken);
            
            // Gắn lại token mới và retry request
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return axiosClient(originalRequest);
          } catch (refreshError) {
            // Refresh thất bại => logout
            if (!isRedirectingToLogin) {
              isRedirectingToLogin = true;
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
            return Promise.reject(refreshError);
          }
        }

        if (!isRedirectingToLogin) {
          isRedirectingToLogin = true;
          
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
      } else if (status === 403) {
        window.location.href = '/403'; // Use /403 as defined in App.tsx routing instead of /forbidden
      } else if (status === 429) {
        const retryAfter = error.response?.headers['retry-after'] || 60;
        useNotificationStore.getState().showError(
          `Quá nhiều yêu cầu. Vui lòng thử lại sau ${retryAfter} giây.`
        );
      } else if (status >= 500) {
        useNotificationStore.getState().showError(
          'Hệ thống đang gặp sự cố. Đội kỹ thuật đã được thông báo.'
        );
      }
    } else if (isNetworkError) {
      useNotificationStore.getState().showError(
        'Lỗi kết nối mạng. Vui lòng kiểm tra lại đường truyền của bạn.'
      );
    }

    return Promise.reject(error);
  }
);
