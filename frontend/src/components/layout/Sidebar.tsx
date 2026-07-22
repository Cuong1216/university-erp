import React, { useMemo } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuthStore } from '../../store/useAuthStore';
import { SIDEBAR_MENU_CONFIG, filterMenuItemsByRoles } from '../../config/menuConfig';
import type { MenuItem } from '../../config/menuConfig';
import type { RoleType } from '../../types/auth.types';

export const Sidebar: React.FC = () => {
  const { roles, userInfo, logout } = useAuthStore();

  // Dùng useMemo để chỉ tính toán lại menu khi roles thay đổi
  const visibleMenuItems = useMemo(() => {
    return filterMenuItemsByRoles(SIDEBAR_MENU_CONFIG, roles);
  }, [roles]);

  const renderMenuItems = (items: MenuItem[]) => {
    return (
      <ul style={{ listStyle: 'none', paddingLeft: 12, margin: 0 }}>
        {items.map((item) => (
          <li key={item.key} style={{ margin: '8px 0' }}>
            <NavLink
              to={item.path}
              style={({ isActive }) => ({
                display: 'block',
                padding: '10px 14px',
                borderRadius: '6px',
                textDecoration: 'none',
                color: isActive ? '#ffffff' : '#334155',
                backgroundColor: isActive ? '#3b82f6' : 'transparent',
                fontWeight: isActive ? 600 : 400,
                transition: 'all 0.2s ease',
              })}
            >
              <span>{item.label}</span>
            </NavLink>

            {/* Render menu con nếu có */}
            {item.children && item.children.length > 0 && (
              <div style={{ marginLeft: 16, borderLeft: '2px solid #e2e8f0', marginTop: 4 }}>
                {renderMenuItems(item.children)}
              </div>
            )}
          </li>
        ))}
      </ul>
    );
  };

  return (
    <aside
      style={{
        width: 280,
        borderRight: '1px solid #e2e8f0',
        height: '100vh',
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: '#f8fafc',
        fontFamily: 'system-ui, -apple-system, sans-serif',
      }}
    >
      <div style={{ padding: '20px 20px', borderBottom: '1px solid #e2e8f0' }}>
        <h3 style={{ margin: 0, color: '#1e293b', fontSize: 20 }}>University ERP</h3>
        {userInfo && (
          <div style={{ marginTop: 8, fontSize: 13, color: '#64748b' }}>
            Xin chào, <strong style={{ color: '#0f172a' }}>{userInfo.fullName}</strong>
            <div style={{ marginTop: 4, display: 'flex', gap: 4, flexWrap: 'wrap' }}>
              {roles.map((r: RoleType) => (
                <span
                  key={r}
                  style={{
                    backgroundColor: '#e2e8f0',
                    color: '#334155',
                    padding: '2px 6px',
                    borderRadius: 4,
                    fontSize: 11,
                    fontWeight: 600,
                  }}
                >
                  {r.replace('ROLE_', '')}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>

      <nav style={{ flex: 1, overflowY: 'auto', padding: '16px 12px' }}>
        {renderMenuItems(visibleMenuItems)}
      </nav>

      <div style={{ padding: '16px 20px', borderTop: '1px solid #e2e8f0' }}>
        <button
          onClick={() => {
            logout();
            window.location.href = '/login';
          }}
          style={{
            width: '100%',
            padding: '10px 14px',
            backgroundColor: '#ef4444',
            color: '#ffffff',
            border: 'none',
            borderRadius: '6px',
            fontWeight: 600,
            cursor: 'pointer',
            transition: 'background-color 0.2s',
          }}
        >
          Đăng xuất
        </button>
      </div>
    </aside>
  );
};
