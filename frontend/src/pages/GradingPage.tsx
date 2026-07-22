import React from 'react';

export const GradingPage: React.FC = () => (
  <div>
    <h1 style={{ marginTop: 0, color: '#0f172a' }}>Nhập Điểm Học Phần</h1>
    <div style={{ padding: 16, backgroundColor: '#dcfce7', color: '#166534', borderRadius: 8, border: '1px solid #bbf7d0' }}>
      <strong>✓ Hợp lệ:</strong> Bạn đang truy cập dưới quyền <code>ROLE_GIANG_VIEN</code> hoặc <code>ROLE_GIAO_VU</code>.
    </div>
  </div>
);
