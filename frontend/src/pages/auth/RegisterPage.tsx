import type { ChangeEvent, FormEvent } from 'react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';

import { registerUser } from '../../api/authApi';
import { isHttpConflictError } from '../../api/httpClient';

type FormValues = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
  phoneNumber: string;
};

type FormErrors = Partial<Record<keyof FormValues | 'form', string>>;

const initialFormValues: FormValues = {
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
  phoneNumber: '',
};

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function RegisterPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [formValues, setFormValues] = useState<FormValues>(initialFormValues);
  const [errors, setErrors] = useState<FormErrors>({});
  const [hasSubmitted, setHasSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validate = (values: FormValues): FormErrors => {
    const nextErrors: FormErrors = {};

    if (!values.firstName.trim()) {
      nextErrors.firstName = t('auth.register.errors.first_name_required');
    }

    if (!values.lastName.trim()) {
      nextErrors.lastName = t('auth.register.errors.last_name_required');
    }

    if (!values.email.trim()) {
      nextErrors.email = t('auth.register.errors.email_required');
    } else if (!emailPattern.test(values.email.trim())) {
      nextErrors.email = t('auth.register.errors.email_invalid');
    }

    if (!values.password) {
      nextErrors.password = t('auth.register.errors.password_required');
    } else if (values.password.length < 8) {
      nextErrors.password = t('auth.register.errors.password_min_length');
    }

    if (!values.confirmPassword) {
      nextErrors.confirmPassword = t('auth.register.errors.confirm_password_required');
    } else if (values.confirmPassword !== values.password) {
      nextErrors.confirmPassword = t('auth.register.errors.passwords_do_not_match');
    }

    return nextErrors;
  };

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target;
    const nextValues = { ...formValues, [name]: value };

    setFormValues(nextValues);

    if (hasSubmitted) {
      setErrors(validate(nextValues));
    }
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const validationErrors = validate(formValues);
    setHasSubmitted(true);
    setErrors(validationErrors);

    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);

    try {
      await registerUser({
        firstName: formValues.firstName.trim(),
        lastName: formValues.lastName.trim(),
        email: formValues.email.trim(),
        password: formValues.password,
        phoneNumber: formValues.phoneNumber.trim(),
      });

      navigate('/login', { replace: true });
    } catch (error) {
      if (isHttpConflictError(error)) {
        setErrors((currentErrors) => ({
          ...currentErrors,
          email: t('auth.register.errors.email_exists'),
        }));
        return;
      }

      setErrors((currentErrors) => ({
        ...currentErrors,
        form: t('auth.register.errors.generic'),
      }));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-panel">
        <div className="auth-copy">
          <div className="accent-line" />
          <h1>{t('auth.register.title')}</h1>
          <p>{t('auth.register.subtitle')}</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="field-grid">
            <label className="field">
              <span>{t('auth.register.first_name')}</span>
              <input
                name="firstName"
                type="text"
                value={formValues.firstName}
                onChange={handleChange}
                autoComplete="given-name"
                aria-invalid={Boolean(errors.firstName)}
                required
              />
              {errors.firstName ? <small>{errors.firstName}</small> : null}
            </label>

            <label className="field">
              <span>{t('auth.register.last_name')}</span>
              <input
                name="lastName"
                type="text"
                value={formValues.lastName}
                onChange={handleChange}
                autoComplete="family-name"
                aria-invalid={Boolean(errors.lastName)}
                required
              />
              {errors.lastName ? <small>{errors.lastName}</small> : null}
            </label>
          </div>

          <label className="field">
            <span>{t('auth.register.email')}</span>
            <input
              name="email"
              type="email"
              value={formValues.email}
              onChange={handleChange}
              autoComplete="email"
              aria-invalid={Boolean(errors.email)}
              required
            />
            {errors.email ? <small>{errors.email}</small> : null}
          </label>

          <label className="field">
            <span>{t('auth.register.password')}</span>
            <input
              name="password"
              type="password"
              value={formValues.password}
              onChange={handleChange}
              autoComplete="new-password"
              aria-invalid={Boolean(errors.password)}
              required
            />
            {errors.password ? <small>{errors.password}</small> : null}
          </label>

          <label className="field">
            <span>{t('auth.register.confirm_password')}</span>
            <input
              name="confirmPassword"
              type="password"
              value={formValues.confirmPassword}
              onChange={handleChange}
              autoComplete="new-password"
              aria-invalid={Boolean(errors.confirmPassword)}
              required
            />
            {errors.confirmPassword ? <small>{errors.confirmPassword}</small> : null}
          </label>

          <label className="field">
            <span>{t('auth.register.phone_number')}</span>
            <input
              name="phoneNumber"
              type="tel"
              value={formValues.phoneNumber}
              onChange={handleChange}
              autoComplete="tel"
            />
          </label>

          {errors.form ? <p className="form-error">{errors.form}</p> : null}

          <button className="primary-button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? t('auth.register.submitting') : t('auth.register.submit_button')}
          </button>

          <p className="auth-link">
            {t('auth.register.login_prompt')}{' '}
            <Link to="/login">{t('auth.login.submit_button')}</Link>
          </p>
        </form>
      </section>
    </main>
  );
}

export default RegisterPage;
