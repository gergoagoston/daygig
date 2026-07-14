import type { ChangeEvent, FormEvent } from 'react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';

import { registerUser } from '../../api/authApi';
import { isHttpConflictError } from '../../api/httpClient';
import AuthPage from '../../components/auth/AuthPage';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';

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
    <AuthPage
      title={t('auth.register.title')}
      subtitle={t('auth.register.subtitle')}
      onSubmit={handleSubmit}
    >
      <div className="grid gap-0 md:grid-cols-2 md:gap-4">
        <Input
          id="register-first-name"
          className="mb-5"
          name="firstName"
          label={t('auth.register.first_name')}
          type="text"
          value={formValues.firstName}
          onChange={handleChange}
          autoComplete="given-name"
          error={errors.firstName}
          required
        />

        <Input
          id="register-last-name"
          className="mb-5"
          name="lastName"
          label={t('auth.register.last_name')}
          type="text"
          value={formValues.lastName}
          onChange={handleChange}
          autoComplete="family-name"
          error={errors.lastName}
          required
        />
      </div>

      <Input
        id="register-email"
        className="mb-5"
        name="email"
        label={t('auth.register.email')}
        type="email"
        value={formValues.email}
        onChange={handleChange}
        autoComplete="email"
        error={errors.email}
        required
      />

      <Input
        id="register-password"
        className="mb-5"
        name="password"
        label={t('auth.register.password')}
        type="password"
        value={formValues.password}
        onChange={handleChange}
        autoComplete="new-password"
        error={errors.password}
        required
      />

      <Input
        id="register-confirm-password"
        className="mb-5"
        name="confirmPassword"
        label={t('auth.register.confirm_password')}
        type="password"
        value={formValues.confirmPassword}
        onChange={handleChange}
        autoComplete="new-password"
        error={errors.confirmPassword}
        required
      />

      <Input
        id="register-phone-number"
        className="mb-2"
        name="phoneNumber"
        label={t('auth.register.phone_number')}
        type="tel"
        value={formValues.phoneNumber}
        onChange={handleChange}
        autoComplete="tel"
      />

      {errors.form ? (
        <p className="mb-4 rounded-lg bg-surface px-3.5 py-3 font-semibold text-danger">
          {errors.form}
        </p>
      ) : null}

      <Button type="submit" isLoading={isSubmitting}>
        {isSubmitting ? t('auth.register.submitting') : t('auth.register.submit_button')}
      </Button>

      <p className="mt-5 text-center">
        {t('auth.register.login_prompt')} <Link to="/login">{t('auth.login.submit_button')}</Link>
      </p>
    </AuthPage>
  );
}

export default RegisterPage;
