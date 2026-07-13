import type { ChangeEvent, FormEvent } from 'react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useLocation, useNavigate } from 'react-router-dom';

import { loginUser } from '../../api/authApi';
import { isHttpUnauthorizedError } from '../../api/httpClient';
import AuthPage from '../../components/auth/AuthPage';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
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
    <AuthPage
      title={t('auth.login.title')}
      subtitle={t('auth.login.subtitle')}
      onSubmit={handleSubmit}
    >
      <Input
        id="login-email"
        className="mb-5"
        name="email"
        label={t('auth.login.email')}
        type="email"
        value={formValues.email}
        onChange={handleChange}
        autoComplete="email"
        error={errors.email}
        required
      />

      <Input
        id="login-password"
        className="mb-5"
        name="password"
        label={t('auth.login.password')}
        type="password"
        value={formValues.password}
        onChange={handleChange}
        autoComplete="current-password"
        error={errors.password}
        required
      />

      {errors.form ? (
        <p className="mb-5 rounded-lg bg-red-100 px-3.5 py-3 text-red-700">{errors.form}</p>
      ) : null}

      <Button type="submit" isLoading={isSubmitting}>
        {isSubmitting ? t('auth.login.submitting') : t('auth.login.submit_button')}
      </Button>

      <p className="mt-5 text-center">
        {t('auth.login.register_prompt')} <Link to="/register">{t('auth.register.title')}</Link>
      </p>
    </AuthPage>
  );
}

export default LoginPage;
