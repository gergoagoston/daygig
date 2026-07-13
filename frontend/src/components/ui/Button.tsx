import type { ButtonHTMLAttributes, ReactNode } from 'react';

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  children: ReactNode;
  isLoading?: boolean;
};

function Button({
  children,
  className = '',
  isLoading = false,
  disabled = false,
  type = 'button',
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      disabled={disabled || isLoading}
      className={`inline-flex w-full items-center justify-center rounded-lg bg-action px-4.5 py-3 font-bold text-action-text transition focus:outline-none focus:ring-3 focus:ring-primary-soft disabled:opacity-65 ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}

export default Button;
