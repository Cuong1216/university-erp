import React from 'react';

export const SalaryConfigPage: React.FC = () => (
  <div>
    <h1 style={{ marginTop: 0, color: '#0f172a' }}>Cấu Hình Lương & Định Mức</h1>
    <div style={{ padding: 16, backgroundColor: '#fef3c7', color: '#92400e', borderRadius: 8, border: '1px solid #fde68a' }}>
      <strong>✓ Hợp lệ:</strong> Bạn đang truy cập dưới quyền cao nhất <code>ROLE_ADMIN</code>.
    </div>
  </div>
);

export const UsersListPage: React.FC = () => (
  <div>
    <h1 style={{ marginTop: 0, color: '#0f172a' }}>Danh Sách Tài Khoản Hệ Thống</h1>
    <p>Quản lý toàn bộ người dùng trong trường học.</p>
  </div>
);

export const RolesListPage: React.FC = () => (
  <div>
    <h1 style={{ marginTop: 0, color: '#0f172a' }}>Phân Quyền Hệ Thống</h1>
    <p>Cấu hình nhóm quyền và chức năng cho các vai trò.</p>
  </div>
);
