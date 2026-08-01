import React from 'react';
import { axiosClient } from '../../api/axiosClient';

export const SalaryConfigPage: React.FC = () => (
  <div>
    <h1 style={{ marginTop: 0, color: '#0f172a' }}>Cấu Hình Lương & Định Mức</h1>
    <div style={{ padding: 16, backgroundColor: '#fef3c7', color: '#92400e', borderRadius: 8, border: '1px solid #fde68a' }}>
      <strong>✓ Hợp lệ:</strong> Bạn đang truy cập dưới quyền cao nhất <code>ROLE_ADMIN</code>.
    </div>
  </div>
);

export const UsersListPage: React.FC = () => {
  const [users, setUsers] = React.useState<any[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    const fetchUsers = async () => {
      try {
        const response = await axiosClient.get('/users');
        setUsers(response.data);
      } catch (err: any) {
        setError(err.response?.data?.message || 'Không thể tải danh sách tài khoản');
      } finally {
        setLoading(false);
      }
    };
    fetchUsers();
  }, []);

  return (
    <div>
      <h1 style={{ marginTop: 0, color: '#0f172a' }}>Danh Sách Tài Khoản Hệ Thống</h1>
      <p style={{ color: '#64748b' }}>Quản lý toàn bộ người dùng trong trường học.</p>
      
      {error && <div style={{ padding: 12, backgroundColor: '#fef2f2', color: '#dc2626', borderRadius: 8, marginBottom: 16 }}>⚠️ {error}</div>}
      
      {loading ? (
        <div style={{ padding: 40, textAlign: 'center', color: '#64748b' }}>Đang tải dữ liệu...</div>
      ) : (
        <div style={{ backgroundColor: '#ffffff', borderRadius: 8, border: '1px solid #e2e8f0', overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
            <thead>
              <tr style={{ backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0', textAlign: 'left', color: '#475569' }}>
                <th style={{ padding: '12px 16px' }}>Tài khoản</th>
                <th style={{ padding: '12px 16px' }}>Họ tên</th>
                <th style={{ padding: '12px 16px' }}>Email</th>
                <th style={{ padding: '12px 16px' }}>Quyền</th>
                <th style={{ padding: '12px 16px' }}>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                  <td style={{ padding: '12px 16px', fontWeight: 600 }}>{user.username}</td>
                  <td style={{ padding: '12px 16px' }}>{user.fullName}</td>
                  <td style={{ padding: '12px 16px' }}>{user.email || '-'}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                      {user.roles.map((role: string) => (
                        <span key={role} style={{ padding: '2px 8px', borderRadius: 12, fontSize: 12, backgroundColor: '#e0f2fe', color: '#0369a1' }}>
                          {role.replace('ROLE_', '')}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td style={{ padding: '12px 16px' }}>
                    {user.isActive ? (
                      <span style={{ padding: '2px 8px', borderRadius: 12, fontSize: 12, backgroundColor: '#dcfce7', color: '#166534' }}>Hoạt động</span>
                    ) : (
                      <span style={{ padding: '2px 8px', borderRadius: 12, fontSize: 12, backgroundColor: '#fef2f2', color: '#dc2626' }}>Bị khóa</span>
                    )}
                  </td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr>
                  <td colSpan={5} style={{ padding: '24px', textAlign: 'center', color: '#64748b' }}>Không có tài khoản nào trong hệ thống</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export const RolesListPage: React.FC = () => (
  <div>
    <h1 style={{ marginTop: 0, color: '#0f172a' }}>Phân Quyền Hệ Thống</h1>
    <p>Cấu hình nhóm quyền và chức năng cho các vai trò.</p>
  </div>
);
