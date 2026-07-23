import type { TextareaHTMLAttributes } from 'react';

import Field from './Field';

type TextareaProps = TextareaHTMLAttributes<HTMLTextAreaElement> & {
  error?: string;
  id: string;
  label: string;
};

function Textarea({ className = '', error, id, label, required = false, ...props }: TextareaProps) {
  return (
    <Field className={className} error={error} id={id} label={label} required={required}>
      {({ controlClassName, ...fieldProps }) => (
        <textarea
          id={id}
          required={required}
          className={`${controlClassName} min-h-32 resize-y`}
          {...fieldProps}
          {...props}
        />
      )}
    </Field>
  );
}

export default Textarea;
