import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/useAuthStore';
import type { RoleType } from '../../types/auth.types';

interface ProtectedRouteProps {
  allowedRoles?: RoleType | RoleType[];
  children?: React.ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  allowedRoles,
  children,
}) => {
  const location = useLocation();
  const { isLoggedIn, isInitialized, hasRole } = useAuthStore();

  // Đợi kho lưu trữ Zustand khởi tạo xong token/jwt trước khi quyết định
  if (!isInitialized) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', fontFamily: 'sans-serif' }}>
        <span>Đang kiểm tra quyền truy cập...</span>
      </div>
    );
  }

  // 1. Chưa đăng nhập -> Chuyển hướng về /login và ghi nhớ đường dẫn định vào
  if (!isLoggedIn) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // 2. Đã đăng nhập nhưng không có role phù hợp -> Đẩy về trang /403
  if (allowedRoles && !hasRole(allowedRoles)) {
    return <Navigate to="/403" replace />;
  }

  // 3. Hợp lệ -> Render con hoặc Outlet cho Layout
  return children ? <>{children}</> : <Outlet />;
};
