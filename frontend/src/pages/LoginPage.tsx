import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { axiosClient } from '../api/axiosClient';
import { useAuthStore } from '../store/useAuthStore';
import type { AxiosError } from 'axios';

interface LoginApiResponse {
  token: string;
  tokenType: string;
  userId: string;
  username: string;
  roles: string[];
}

interface ApiError {
  message: string;
  status: number;
}

export const LoginPage: React.FC = () => {
  const { setToken } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const { data } = await axiosClient.post<LoginApiResponse>('/auth/login', {
        username,
        password,
      });
      setToken(data.token);

      const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/dashboard';
      navigate(from, { replace: true });
    } catch (err) {
      const axiosErr = err as AxiosError<ApiError>;
      if (!axiosErr.response) {
        setError('⚠️ Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng hoặc liên hệ quản trị viên.');
      } else {
        setError(
          axiosErr.response?.data?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại tài khoản hoặc mật khẩu.'
        );
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#f1f5f9', fontFamily: 'sans-serif' }}>
      <div style={{ backgroundColor: '#ffffff', padding: 32, borderRadius: 12, boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)', width: 400 }}>
        <h2 style={{ margin: '0 0 8px 0', color: '#0f172a' }}>University ERP</h2>
        <p style={{ margin: '0 0 24px 0', color: '#64748b', fontSize: 14 }}>Đăng nhập vào hệ thống quản lý đại học</p>

        {error && (
          <div style={{ padding: '10px 14px', backgroundColor: '#fef2f2', color: '#dc2626', borderRadius: 6, marginBottom: 16, border: '1px solid #fecaca', fontSize: 14 }}>
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: '#334155', marginBottom: 6 }}>Tên đăng nhập</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Nhập tên đăng nhập..."
              style={{ width: '100%', padding: '10px 12px', borderRadius: 6, border: '1px solid #cbd5e1', boxSizing: 'border-box' }}
              required
              autoFocus
            />
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: '#334155', marginBottom: 6 }}>Mật khẩu</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Nhập mật khẩu..."
              style={{ width: '100%', padding: '10px 12px', borderRadius: 6, border: '1px solid #cbd5e1', boxSizing: 'border-box' }}
              required
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            style={{
              marginTop: 8,
              padding: '12px 16px',
              backgroundColor: loading ? '#93c5fd' : '#3b82f6',
              color: '#ffffff',
              border: 'none',
              borderRadius: 6,
              fontWeight: 600,
              cursor: loading ? 'not-allowed' : 'pointer',
              fontSize: 15,
            }}
          >
            {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>
        </form>
      </div>
    </div>
  );
};
