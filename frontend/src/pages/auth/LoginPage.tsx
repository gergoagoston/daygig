import type { ChangeEvent, FormEvent } from 'react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useLocation, useNavigate } from 'react-router-dom';

import { loginUser } from '../../api/authApi';
import { isHttpUnauthorizedError } from '../../api/httpClient';
import useAuth from '../../hooks/useAuth';

type FormValues = {
  email: string;
  password: string;
};

type FormErrors = Partial<Record<keyof FormValues | 'form', string>>;

type LoginLocationState = {
  from?: {
    pathname?: string;
    search?: string;
    hash?: string;
  };
};

const initialFormValues: FormValues = {
  email: '',
  password: '',
};

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function LoginPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [formValues, setFormValues] = useState<FormValues>(initialFormValues);
  const [errors, setErrors] = useState<FormErrors>({});
  const [hasSubmitted, setHasSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const getRedirectPath = () => {
    const from = (location.state as LoginLocationState | null)?.from;

    if (!from?.pathname || from.pathname === '/login') {
      return '/jobs';
    }

    return `${from.pathname}${from.search ?? ''}${from.hash ?? ''}`;
  };

  const validate = (values: FormValues): FormErrors => {
    const nextErrors: FormErrors = {};

    if (!values.email.trim()) {
      nextErrors.email = t('auth.login.errors.email_required');
    } else if (!emailPattern.test(values.email.trim())) {
      nextErrors.email = t('auth.login.errors.email_invalid');
    }

    if (!values.password) {
      nextErrors.password = t('auth.login.errors.password_required');
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
      const response = await loginUser({
        email: formValues.email.trim(),
        password: formValues.password,
      });
      login(response.data);
      navigate(getRedirectPath(), { replace: true });
    } catch (error) {
      setErrors((currentErrors) => ({
        ...currentErrors,
        form: isHttpUnauthorizedError(error)
          ? t('auth.login.errors.invalid_credentials')
          : t('auth.login.errors.generic'),
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
          <h1>{t('auth.login.title')}</h1>
          <p>{t('auth.login.subtitle')}</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <label className="field">
            <span>{t('auth.login.email')}</span>
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
            <span>{t('auth.login.password')}</span>
            <input
              name="password"
              type="password"
              value={formValues.password}
              onChange={handleChange}
              autoComplete="current-password"
              aria-invalid={Boolean(errors.password)}
              required
            />
            {errors.password ? <small>{errors.password}</small> : null}
          </label>

          {errors.form ? <p className="form-error">{errors.form}</p> : null}

          <button className="primary-button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? t('auth.login.submitting') : t('auth.login.submit_button')}
          </button>

          <p className="auth-link">
            {t('auth.login.register_prompt')} <Link to="/register">{t('auth.register.title')}</Link>
          </p>
        </form>
      </section>
    </main>
  );
}

export default LoginPage;
