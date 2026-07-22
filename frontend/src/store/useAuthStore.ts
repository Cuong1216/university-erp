import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthState, AuthActions, JwtPayload, RoleType, UserInfo } from '../types/auth.types';

/**
 * Hàm giải mã JWT an toàn
 */
const decodeJwt = (token: string): JwtPayload | null => {
  try {
    const base64Url = token.split('.')[1];
    if (!base64Url) return null;
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload) as JwtPayload;
  } catch (error) {
    console.error('Invalid JWT Token:', error);
    return null;
  }
};

const initialState: AuthState = {
  isLoggedIn: false,
  accessToken: null,
  userInfo: null,
  roles: [],
  isInitialized: false,
};

export const useAuthStore = create<AuthState & AuthActions>()(
  persist(
    (set, get) => ({
      ...initialState,

      setToken: (token: string) => {
        const payload = decodeJwt(token);
        if (!payload) {
          get().logout();
          return;
        }

        // Kiểm tra token đã hết hạn chưa (exp tính bằng giây, Date.now() tính bằng mili-giây)
        if (payload.exp * 1000 < Date.now()) {
          get().logout();
          return;
        }

        // Lấy danh sách roles (hỗ trợ cả cấu trúc roles hoặc authorities)
        const extractedRoles: RoleType[] = (payload.roles || payload.authorities || []) as RoleType[];

        const userInfo: UserInfo = {
          id: payload.sub,
          username: payload.sub,
          fullName: payload.fullName || payload.sub,
          email: payload.email || '',
        };

        set({
          isLoggedIn: true,
          accessToken: token,
          userInfo,
          roles: extractedRoles,
          isInitialized: true,
        });
      },

      logout: () => {
        set({
          ...initialState,
          isInitialized: true, // Vẫn đánh dấu đã init xong sau logout
        });
      },

      initAuth: () => {
        const { accessToken, logout } = get();
        if (!accessToken) {
          set({ isInitialized: true });
          return;
        }

        const payload = decodeJwt(accessToken);
        if (!payload || payload.exp * 1000 < Date.now()) {
          logout();
        } else {
          set({ isInitialized: true });
        }
      },

      hasRole: (allowedRoles?: RoleType | RoleType[]) => {
        if (!allowedRoles) return true;
        const targetRoles = Array.isArray(allowedRoles) ? allowedRoles : [allowedRoles];
        if (targetRoles.length === 0) return true;
        
        const { roles } = get();
        return targetRoles.some((role) => roles.includes(role));
      },
    }),
    {
      name: 'erp_auth_storage', // Tên key trong localStorage
      partialize: (state) => ({ accessToken: state.accessToken }),
      onRehydrateStorage: () => (state) => {
        if (state && state.accessToken) {
          state.setToken(state.accessToken);
        } else if (state) {
          state.isInitialized = true;
        }
      },
    }
  )
);
