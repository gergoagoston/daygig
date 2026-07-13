import type { InputHTMLAttributes } from 'react';

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  id: string;
  label: string;
  error?: string;
};

function Input({ id, label, error, className = '', required = false, ...props }: InputProps) {
  const hasError = Boolean(error);

  return (
    <div className={`flex flex-col gap-2 text-left ${className}`}>
      <label htmlFor={id} className="font-semibold text-heading">
        {label}
        {required ? <span className="text-red-600"> *</span> : null}
      </label>
      <input
        id={id}
        aria-invalid={hasError}
        aria-describedby={hasError ? `${id}-error` : undefined}
        required={required}
        className={`w-full rounded-lg border bg-page px-3.5 py-3 text-heading outline-none transition placeholder:text-body/70 focus:ring-3 ${
          hasError
            ? 'border-red-600 focus:border-red-600 focus:ring-red-100'
            : 'border-border focus:border-primary focus:ring-primary-soft'
        }`}
        {...props}
      />
      {hasError ? (
        <p id={`${id}-error`} className="text-sm text-red-700">
          {error}
        </p>
      ) : null}
    </div>
  );
}

export default Input;
