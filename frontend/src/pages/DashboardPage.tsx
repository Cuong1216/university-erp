import React from 'react';
import { useAuthStore } from '../store/useAuthStore';
import { AdminDashboard } from '../components/dashboard/AdminDashboard';

export const DashboardPage: React.FC = () => {
  const { userInfo, roles } = useAuthStore();

  const isAdminOrGiaoVu = roles.some((role) =>
    ['ROLE_ADMIN', 'ROLE_GIAO_VU'].includes(role)
  );

  if (isAdminOrGiaoVu) {
    return <AdminDashboard />;
  }

  return (
    <div>
      <h1 style={{ marginTop: 0, color: '#0f172a' }}>Trang Tổng Quan (Dashboard)</h1>
      <p style={{ color: '#475569' }}>
        Xin chào, <strong>{userInfo?.fullName || userInfo?.username}</strong>! Chào mừng bạn đến với hệ thống University ERP.
      </p>
      <div style={{ backgroundColor: '#ffffff', padding: 20, borderRadius: 12, border: '1px solid #e2e8f0', marginTop: 16 }}>
        <h4 style={{ margin: '0 0 12px 0', color: '#1e293b' }}>Thông tin quyền hạn tài khoản:</h4>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          {roles.map((r) => (
            <span
              key={r}
              style={{
                backgroundColor: '#eff6ff',
                color: '#2563eb',
                border: '1px solid #bfdbfe',
                padding: '4px 10px',
                borderRadius: 6,
                fontWeight: 600,
                fontSize: 13,
              }}
            >
              {r.replace('ROLE_', '')}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
};
