import React from 'react';

export type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  loading?: boolean;
}

export const Button: React.FC<ButtonProps> = ({ variant = 'primary', loading, disabled, children, style, ...props }) => {
  const getStyle = (): React.CSSProperties => {
    const baseStyle: React.CSSProperties = {
      padding: 'var(--space-sm) var(--space-md)',
      border: 'none',
      borderRadius: 'var(--radius-md)',
      fontWeight: 600,
      fontSize: 'var(--font-size-base)',
      cursor: disabled || loading ? 'not-allowed' : 'pointer',
      display: 'inline-flex',
      justifyContent: 'center',
      alignItems: 'center',
      gap: 'var(--space-sm)',
      ...style,
    };

    if (disabled || loading) {
      return {
        ...baseStyle,
        backgroundColor: '#94a3b8',
        color: '#fff',
      };
    }

    switch (variant) {
      case 'secondary':
        return { ...baseStyle, backgroundColor: '#e2e8f0', color: '#0f172a' };
      case 'danger':
        return { ...baseStyle, backgroundColor: 'var(--color-danger)', color: '#fff' };
      case 'ghost':
        return { ...baseStyle, backgroundColor: 'transparent', color: 'var(--color-primary)' };
      case 'primary':
      default:
        return { ...baseStyle, backgroundColor: 'var(--color-primary)', color: '#fff' };
    }
  };

  return (
    <button style={getStyle()} disabled={disabled || loading} {...props}>
      {children}
    </button>
  );
};
