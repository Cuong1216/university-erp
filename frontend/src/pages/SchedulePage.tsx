import React from 'react';

export const SchedulePage: React.FC = () => (
  <div>
    <h1 style={{ marginTop: 0, color: '#0f172a' }}>Xem Lịch Học / Lịch Thi & Sắp Xếp Lịch</h1>
    <div style={{ padding: 16, backgroundColor: '#e0f2fe', color: '#0369a1', borderRadius: 8, border: '1px solid #bae6fd' }}>
      <strong>✓ Hợp lệ:</strong> Bạn đang truy cập dưới quyền <code>ROLE_SINH_VIEN</code>, <code>ROLE_GIANG_VIEN</code> hoặc <code>ROLE_GIAO_VU</code>.
    </div>
  </div>
);

