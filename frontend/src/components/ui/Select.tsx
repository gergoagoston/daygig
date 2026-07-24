import type { ReactNode, SelectHTMLAttributes } from 'react';

import Field from './Field';

type SelectProps = SelectHTMLAttributes<HTMLSelectElement> & {
  children: ReactNode;
  error?: string;
  id: string;
  label: string;
};

function Select({
  children,
  className = '',
  error,
  id,
  label,
  required = false,
  ...props
}: SelectProps) {
  return (
    <Field className={className} error={error} id={id} label={label} required={required}>
      {({ controlClassName, ...fieldProps }) => (
        <select id={id} required={required} className={controlClassName} {...fieldProps} {...props}>
          {children}
        </select>
      )}
    </Field>
  );
}

export default Select;
