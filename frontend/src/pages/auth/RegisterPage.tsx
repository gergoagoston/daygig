import type { ChangeEvent, FormEvent } from 'react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';

import { registerUser } from '../../api/authApi';
import { isHttpConflictError } from '../../api/httpClient';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';

type FormValues = {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  email: string;
  password: string;
  confirmPassword: string;
};

type FormErrors = Partial<Record<keyof FormValues | 'form', string>>;

const initialFormValues: FormValues = {
  firstName: '',
  lastName: '',
  phoneNumber: '',
  email: '',
  password: '',
  confirmPassword: '',
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
        ...(formValues.phoneNumber.trim() && {
          phoneNumber: formValues.phoneNumber.trim(),
        }),
        email: formValues.email.trim(),
        password: formValues.password,
      });
      navigate('/login');
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
    <main className="min-h-screen cursor-default select-none bg-slate-100 px-4 py-8 text-slate-950 sm:px-6 lg:px-8">
      <section className="mx-auto grid min-h-[calc(100vh-4rem)] w-full max-w-6xl items-center gap-8 lg:grid-cols-[0.9fr_1.1fr]">
        <div className="hidden text-left lg:block">
          <div className="mb-6 h-2 w-20 rounded-full bg-indigo-600" />
          <h2 className="max-w-md text-4xl font-bold tracking-normal text-slate-950">
            {t('auth.register.hero_title')}
          </h2>
          <p className="mt-4 max-w-md text-lg text-slate-600">{t('auth.register.subtitle')}</p>
        </div>

        <div className="w-full rounded-lg border border-slate-200 bg-white p-6 text-left shadow-xl shadow-slate-200/70 ring-1 ring-slate-100 sm:p-8">
          <div className="mb-8 text-left">
            <h1 className="text-2xl font-bold tracking-normal text-slate-950 sm:text-3xl">
              {t('auth.register.title')}
            </h1>
          </div>

          <form className="space-y-4" onSubmit={handleSubmit} noValidate>
            <div className="grid gap-4 sm:grid-cols-2">
              <Input
                id="firstName"
                name="firstName"
                type="text"
                label={t('auth.register.first_name')}
                value={formValues.firstName}
                error={errors.firstName}
                onChange={handleChange}
                autoComplete="given-name"
                required
              />
              <Input
                id="lastName"
                name="lastName"
                type="text"
                label={t('auth.register.last_name')}
                value={formValues.lastName}
                error={errors.lastName}
                onChange={handleChange}
                autoComplete="family-name"
                required
              />
            </div>

            <Input
              id="email"
              name="email"
              type="email"
              label={t('auth.register.email')}
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
              label={t('auth.register.password')}
              value={formValues.password}
              error={errors.password}
              onChange={handleChange}
              autoComplete="new-password"
              required
            />

            <Input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              label={t('auth.register.confirm_password')}
              value={formValues.confirmPassword}
              error={errors.confirmPassword}
              onChange={handleChange}
              autoComplete="new-password"
              required
            />

            <Input
              id="phoneNumber"
              name="phoneNumber"
              type="tel"
              label={t('auth.register.phone_number')}
              value={formValues.phoneNumber}
              onChange={handleChange}
              autoComplete="tel"
            />

            {errors.form ? (
              <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{errors.form}</p>
            ) : null}

            <Button type="submit" isLoading={isSubmitting} className="mt-2">
              {isSubmitting ? t('auth.register.submitting') : t('auth.register.submit_button')}
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-slate-600">
            {t('auth.register.login_prompt')}{' '}
            <Link
              to="/login"
              className="cursor-pointer select-none font-semibold text-indigo-600 hover:text-indigo-700"
            >
              {t('auth.login.submit_button')}
            </Link>
          </p>
        </div>
      </section>
    </main>
  );
}

export default RegisterPage;
