import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { useAuthStore } from './store/useAuthStore';
import { ProtectedRoute } from './components/common/ProtectedRoute';
import { Sidebar } from './components/layout/Sidebar';
import { LoginPage } from './pages/LoginPage';
import { ForbiddenPage } from './pages/ForbiddenPage';
import { DashboardPage } from './pages/DashboardPage';
import { GradingPage } from './pages/GradingPage';
import { SchedulePage } from './pages/SchedulePage';
import { StudentGradesPage } from './pages/StudentGradesPage';
import { TeacherSalaryPage } from './pages/TeacherSalaryPage';
import { AcademicSalaryPage } from './pages/AcademicSalaryPage';
import { SalaryConfigPage, UsersListPage, RolesListPage } from './pages/admin';
import { setOnUnauthorizedCallback } from './api/axiosClient';

const AxiosInterceptorSetup: React.FC = () => {
  const navigate = useNavigate();
  useEffect(() => {
    setOnUnauthorizedCallback(() => {
      navigate('/login', { replace: true });
    });
  }, [navigate]);
  return null;
};

const MainLayout: React.FC = () => (
  <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: '#f1f5f9', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
    <Sidebar />
    <main style={{ flex: 1, padding: 32, overflowY: 'auto' }}>
      <Routes>
        <Route path="/dashboard" element={<DashboardPage />} />
        
        <Route
          path="/student/grades"
          element={
            <ProtectedRoute allowedRoles="ROLE_SINH_VIEN">
              <StudentGradesPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/grading"
          element={
            <ProtectedRoute allowedRoles={['ROLE_GIANG_VIEN', 'ROLE_GIAO_VU']}>
              <GradingPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/teacher/salary"
          element={
            <ProtectedRoute allowedRoles="ROLE_GIANG_VIEN">
              <TeacherSalaryPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/schedule"
          element={
            <ProtectedRoute allowedRoles={['ROLE_SINH_VIEN', 'ROLE_GIANG_VIEN', 'ROLE_GIAO_VU']}>
              <SchedulePage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/academic/salary-management"
          element={
            <ProtectedRoute allowedRoles={['ROLE_GIAO_VU', 'ROLE_ADMIN']}>
              <AcademicSalaryPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/salary-config"
          element={
            <ProtectedRoute allowedRoles="ROLE_ADMIN">
              <SalaryConfigPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/users/list"
          element={
            <ProtectedRoute allowedRoles="ROLE_ADMIN">
              <UsersListPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/users/roles"
          element={
            <ProtectedRoute allowedRoles="ROLE_ADMIN">
              <RolesListPage />
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </main>
  </div>
);

export const App: React.FC = () => {
  const initAuth = useAuthStore((state) => state.initAuth);

  useEffect(() => {
    initAuth();
  }, [initAuth]);

  return (
    <BrowserRouter>
      <AxiosInterceptorSetup />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/403" element={<ForbiddenPage />} />

        <Route element={<ProtectedRoute />}>
          <Route path="/*" element={<MainLayout />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};

export default App;
