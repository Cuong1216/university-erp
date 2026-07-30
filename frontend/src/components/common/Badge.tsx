import React from 'react';

export type BadgeVariant = 'success' | 'warning' | 'danger' | 'info';

export interface BadgeProps {
  variant: BadgeVariant;
  children: React.ReactNode;
}

export const Badge: React.FC<BadgeProps> = ({ variant, children }) => {
  const getStyle = (): React.CSSProperties => {
    const baseStyle: React.CSSProperties = {
      padding: 'var(--space-xs) 12px',
      borderRadius: '20px',
      fontWeight: 700,
      fontSize: '12px',
      display: 'inline-block',
    };

    switch (variant) {
      case 'success':
        return { ...baseStyle, backgroundColor: 'var(--color-success-light)', color: 'var(--color-success)' };
      case 'warning':
        return { ...baseStyle, backgroundColor: 'var(--color-warning-light)', color: 'var(--color-warning)' };
      case 'danger':
        return { ...baseStyle, backgroundColor: 'var(--color-danger-light)', color: 'var(--color-danger)' };
      case 'info':
      default:
        return { ...baseStyle, backgroundColor: 'var(--color-primary-light)', color: 'var(--color-primary)' };
    }
  };

  return <span style={getStyle()}>{children}</span>;
};
