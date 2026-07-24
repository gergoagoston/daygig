import type { InputHTMLAttributes } from 'react';

import Field from './Field';

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  error?: string;
  id: string;
  label: string;
};

function Input({ id, label, error, className = '', required = false, ...props }: InputProps) {
  return (
    <Field className={className} error={error} id={id} label={label} required={required}>
      {({ controlClassName, ...fieldProps }) => (
        <input
          id={id}
          required={required}
          className={controlClassName}
          {...fieldProps}
          {...props}
        />
      )}
    </Field>
  );
}

export default Input;
