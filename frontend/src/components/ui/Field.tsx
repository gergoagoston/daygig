import type { ReactNode } from 'react';

export type FieldRenderProps = {
  'aria-describedby'?: string;
  'aria-invalid': boolean;
  controlClassName: string;
};

type FieldProps = {
  children: (props: FieldRenderProps) => ReactNode;
  className?: string;
  error?: string;
  id: string;
  label: string;
  required?: boolean;
};

const fieldControlClasses =
  'min-w-0 w-full rounded-lg border bg-page px-3.5 py-3 text-heading outline-none transition placeholder:text-body/70 focus:ring-2';

function getFieldControlClasses(hasError: boolean) {
  return `${fieldControlClasses} ${
    hasError
      ? 'border-danger focus:border-danger focus:ring-danger-soft'
      : 'border-border focus:border-primary focus:ring-primary-soft'
  }`;
}

function Field({ children, className = '', error, id, label, required = false }: FieldProps) {
  const hasError = Boolean(error);
  const errorId = hasError ? `${id}-error` : undefined;

  return (
    <div className={`flex min-w-0 flex-col gap-2 text-left ${className}`}>
      <label htmlFor={id} className="font-semibold text-heading">
        {label}
        {required ? <span className="text-danger"> *</span> : null}
      </label>
      {children({
        'aria-describedby': errorId,
        'aria-invalid': hasError,
        controlClassName: getFieldControlClasses(hasError),
      })}
      {hasError ? (
        <p id={errorId} className="text-sm text-danger">
          {error}
        </p>
      ) : null}
    </div>
  );
}

export default Field;
