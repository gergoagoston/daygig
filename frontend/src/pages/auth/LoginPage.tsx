import type { ChangeEvent, FormEvent } from 'react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useLocation, useNavigate } from 'react-router-dom';

import { loginUser } from '../../api/authApi';
import { isHttpUnauthorizedError } from '../../api/httpClient';
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
      login(response.data.token);
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
    <main className="min-h-screen cursor-default select-none bg-slate-100 px-4 py-8 text-slate-950 sm:px-6 lg:px-8">
      <section className="mx-auto grid min-h-[calc(100vh-4rem)] w-full max-w-6xl items-center gap-8 lg:grid-cols-[0.9fr_1.1fr]">
        <div className="hidden text-left lg:block">
          <div className="mb-6 h-2 w-20 rounded-full bg-indigo-600" />
          <h2 className="max-w-md text-4xl font-bold tracking-normal text-slate-950">
            {t('auth.login.hero_title')}
          </h2>
          <p className="mt-4 max-w-md text-lg text-slate-600">{t('auth.login.subtitle')}</p>
        </div>

        <div className="w-full rounded-lg border border-slate-200 bg-white p-6 text-left shadow-xl shadow-slate-200/70 ring-1 ring-slate-100 sm:p-8">
          <div className="mb-8 text-left">
            <h1 className="text-2xl font-bold tracking-normal text-slate-950 sm:text-3xl">
              {t('auth.login.title')}
            </h1>
          </div>

          <form className="space-y-4" onSubmit={handleSubmit} noValidate>
            <Input
              id="email"
              name="email"
              type="email"
              label={t('auth.login.email')}
              value={formValues.email}
              error={errors.email}
              onChange={handleChange}
              autoComplete="email"
              required
            />

            <Input
              id="password"
              name="password"
              type="password"
              label={t('auth.login.password')}
              value={formValues.password}
              error={errors.password}
              onChange={handleChange}
              autoComplete="current-password"
              required
            />

            {errors.form ? (
              <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{errors.form}</p>
            ) : null}

            <Button type="submit" isLoading={isSubmitting} className="mt-2">
              {isSubmitting ? t('auth.login.submitting') : t('auth.login.submit_button')}
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-slate-600">
            {t('auth.login.register_prompt')}{' '}
            <Link
              to="/register"
              className="cursor-pointer select-none font-semibold text-indigo-600 hover:text-indigo-700"
            >
              {t('auth.register.submit_button')}
            </Link>
          </p>
        </div>
      </section>
    </main>
  );
}

export default LoginPage;
