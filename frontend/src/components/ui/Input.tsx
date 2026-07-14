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
        {required ? <span className="text-danger"> *</span> : null}
      </label>
      <input
        id={id}
        aria-invalid={hasError}
        aria-describedby={hasError ? `${id}-error` : undefined}
        required={required}
        className={`w-full rounded-lg border bg-page px-3.5 py-3 text-heading outline-none transition placeholder:text-body/70 focus:ring-3 ${
          hasError
            ? 'border-danger focus:border-danger focus:ring-danger-soft'
            : 'border-border focus:border-primary focus:ring-primary-soft'
        }`}
        {...props}
      />
      {hasError ? (
        <p id={`${id}-error`} className="text-sm text-danger">
          {error}
        </p>
      ) : null}
    </div>
  );
}

export default Input;
