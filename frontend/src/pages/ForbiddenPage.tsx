import React from 'react';

export const ForbiddenPage: React.FC = () => (
  <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', height: '100vh', fontFamily: 'sans-serif', backgroundColor: '#fdf2f2' }}>
    <h1 style={{ fontSize: 64, margin: 0, color: '#ef4444' }}>403</h1>
    <h3 style={{ color: '#991b1b', marginTop: 8 }}>Từ chối truy cập (Access Forbidden)</h3>
    <p style={{ color: '#7f1d1d', maxWidth: 450, textAlign: 'center' }}>
      Tài khoản của bạn không có đủ thẩm quyền (Role) để truy cập vào chức năng hoặc trang này.
    </p>
    <a href="/dashboard" style={{ marginTop: 16, padding: '10px 20px', backgroundColor: '#ef4444', color: '#fff', borderRadius: 6, textDecoration: 'none', fontWeight: 600 }}>
      Quay về Tổng quan
    </a>
  </div>
);
