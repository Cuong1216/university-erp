export type RoleType = 'ROLE_ADMIN' | 'ROLE_GIANG_VIEN' | 'ROLE_SINH_VIEN' | 'ROLE_GIAO_VU';

export interface UserInfo {
  id: number | string;
  username: string;
  fullName: string;
  email: string;
  avatarUrl?: string;
}

export interface JwtPayload {
  sub: string;             // Username hoặc User ID
  exp: number;             // Timestamp hết hạn (giây)
  iat: number;             // Timestamp phát hành (giây)
  roles?: RoleType[];      // Mảng role (hoặc authorities tùy backend Spring Boot)
  authorities?: string[];  // Dự phòng nếu backend trả về authorities
  fullName?: string;
  email?: string;
}

export interface AuthState {
  isLoggedIn: boolean;
  accessToken: string | null;
  userInfo: UserInfo | null;
  roles: RoleType[];
  isInitialized: boolean;
}

export interface AuthActions {
  setToken: (token: string) => void;
  logout: () => void;
  initAuth: () => void;
  hasRole: (allowedRoles?: RoleType | RoleType[]) => boolean;
}
