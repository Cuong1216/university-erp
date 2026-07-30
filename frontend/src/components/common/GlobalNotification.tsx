import React, { useEffect, useState } from 'react';
import { useNotificationStore } from '../../store/useNotificationStore';

export const GlobalNotification: React.FC = () => {
  const notifications = useNotificationStore((state) => state.notifications);
  const dismiss = useNotificationStore((state) => state.dismiss);

  // Prevent rendering on SSR
  const [mounted, setMounted] = useState(false);
  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted || notifications.length === 0) return null;

  return (
    <div
      style={{
        position: 'fixed',
        top: '20px',
        right: '20px',
        zIndex: 9999,
        display: 'flex',
        flexDirection: 'column',
        gap: '10px',
      }}
    >
      {notifications.map((notification) => {
        let backgroundColor = '#333';
        switch (notification.type) {
          case 'success':
            backgroundColor = '#22c55e'; // Green
            break;
          case 'error':
            backgroundColor = '#ef4444'; // Red
            break;
          case 'warning':
            backgroundColor = '#f97316'; // Orange
            break;
          case 'info':
            backgroundColor = '#3b82f6'; // Blue
            break;
        }

        return (
          <div
            key={notification.id}
            style={{
              backgroundColor,
              color: 'white',
              padding: '12px 20px',
              borderRadius: '6px',
              boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              minWidth: '250px',
              maxWidth: '400px',
              animation: 'slideIn 0.3s ease-out forwards',
              fontFamily: 'system-ui, -apple-system, sans-serif',
            }}
          >
            <span style={{ marginRight: '12px', fontSize: '14px', lineHeight: '1.4' }}>
              {notification.message}
            </span>
            <button
              onClick={() => dismiss(notification.id)}
              style={{
                background: 'transparent',
                border: 'none',
                color: 'white',
                cursor: 'pointer',
                fontSize: '18px',
                padding: '0 4px',
                opacity: 0.8,
              }}
              onMouseOver={(e) => (e.currentTarget.style.opacity = '1')}
              onMouseOut={(e) => (e.currentTarget.style.opacity = '0.8')}
            >
              &times;
            </button>
          </div>
        );
      })}
      <style>
        {`
          @keyframes slideIn {
            from {
              transform: translateX(100%);
              opacity: 0;
            }
            to {
              transform: translateX(0);
              opacity: 1;
            }
          }
        `}
      </style>
    </div>
  );
};
