import React from 'react';
import { useAuthStore } from '../store/useAuthStore';

export const DashboardPage: React.FC = () => {
  const { userInfo, roles } = useAuthStore();
  return (
    <div>
      <h1 style={{ marginTop: 0, color: '#0f172a' }}>Trang Tổng Quan (Dashboard)</h1>
      <p style={{ color: '#475569' }}>Trang này dành cho tất cả mọi người khi đăng nhập thành công.</p>
      <div style={{ backgroundColor: '#ffffff', padding: 20, borderRadius: 8, border: '1px solid #e2e8f0', marginTop: 16 }}>
        <h4 style={{ margin: '0 0 12px 0' }}>Thông tin Token & Quyền hiện tại:</h4>
        <pre style={{ backgroundColor: '#f8fafc', padding: 12, borderRadius: 6, overflowX: 'auto', border: '1px solid #f1f5f9' }}>
          {JSON.stringify({ userInfo, roles }, null, 2)}
        </pre>
      </div>
    </div>
  );
};
