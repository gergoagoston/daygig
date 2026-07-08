import type { InputHTMLAttributes } from 'react';

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  id: string;
  label: string;
  error?: string;
};

function Input({ id, label, error, className = '', required = false, ...props }: InputProps) {
  const hasError = Boolean(error);

  return (
    <div className={`flex flex-col gap-1.5 text-left ${className}`}>
      <label
        id={`${id}-label`}
        className="cursor-default select-none text-sm font-semibold text-slate-700"
      >
        {label}
        {required ? <span className="text-red-600"> *</span> : null}
      </label>
      <input
        id={id}
        aria-labelledby={`${id}-label`}
        aria-invalid={hasError}
        aria-describedby={hasError ? `${id}-error` : undefined}
        required={required}
        className={`w-full cursor-text select-text rounded-lg border px-4 py-3 text-base text-slate-950 outline-none transition placeholder:text-slate-400 focus:bg-white focus:ring-4 ${
          hasError
            ? 'border-red-500 bg-red-50/40 focus:border-red-500 focus:ring-red-100'
            : 'border-slate-200 bg-slate-50 focus:border-indigo-600 focus:ring-indigo-100'
        }`}
        {...props}
      />
      {hasError ? (
        <p id={`${id}-error`} className="cursor-default text-sm text-red-600">
          {error}
        </p>
      ) : null}
    </div>
  );
}

export default Input;
