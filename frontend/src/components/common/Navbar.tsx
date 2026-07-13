import { NavLink, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import useAuth from '../../hooks/useAuth';

const languages = [
  { code: 'hu', label: 'HU' },
  { code: 'en', label: 'EN' },
  { code: 'ro', label: 'RO' },
] as const;

function navLinkClass({ isActive }: { isActive: boolean }) {
  return [
    'rounded-lg border px-3 py-2 text-sm font-bold !no-underline transition-colors',
    isActive
      ? 'border-primary bg-primary-soft text-heading'
      : 'border-transparent text-body hover:bg-primary-soft hover:text-heading',
  ].join(' ');
}

function Navbar() {
  const { i18n, t } = useTranslation();
  const navigate = useNavigate();
  const { isAuthenticated, isInitializing, logout, user } = useAuth();
  const currentLanguage = i18n.resolvedLanguage ?? i18n.language;
  const accountLabel = user ? `${user.firstName} ${user.lastName}`.trim() || user.email : '';

  const handleLogout = async () => {
    await logout();
    navigate('/jobs', { replace: true });
  };

  return (
    <header className="grid min-h-18 gap-3 border-b border-border bg-surface px-5 py-4 md:grid-cols-[1fr_auto_1fr] md:items-center md:px-8">
      <nav
        className="flex flex-wrap items-center gap-2 md:justify-self-start"
        aria-label={t('navigation.main')}
      >
        {isAuthenticated ? (
          <>
            <NavLink className={navLinkClass} to="/my-jobs">
              {t('navigation.my_jobs')}
            </NavLink>
            <NavLink className={navLinkClass} to="/my-applications">
              {t('navigation.my_applications')}
            </NavLink>
          </>
        ) : null}
      </nav>

      <NavLink
        className="text-center text-lg font-extrabold text-heading no-underline! md:justify-self-center"
        to="/jobs"
      >
        DayGig
      </NavLink>

      <div className="flex flex-wrap items-center justify-between gap-3 md:justify-self-end">
        <label>
          <span className="sr-only">{t('navigation.language')}</span>
          <select
            className="rounded-lg border border-border bg-surface px-3 py-2 text-sm font-bold text-heading outline-none hover:border-primary focus:border-primary"
            value={currentLanguage.slice(0, 2)}
            aria-label={t('navigation.language')}
            onChange={(event) => void i18n.changeLanguage(event.target.value)}
          >
            {languages.map((language) => (
              <option key={language.code} value={language.code}>
                {language.label}
              </option>
            ))}
          </select>
        </label>

        {isInitializing ? null : isAuthenticated ? (
          <div className="flex min-w-0 flex-wrap items-center gap-2">
            <span className="max-w-[180px] overflow-hidden text-ellipsis whitespace-nowrap font-bold text-body">
              {accountLabel}
            </span>
            <button
              className="rounded-lg border border-border bg-surface px-3 py-2 text-sm font-bold text-heading hover:border-primary"
              type="button"
              onClick={handleLogout}
            >
              {t('navigation.logout')}
            </button>
          </div>
        ) : (
          <div className="flex flex-wrap items-center gap-2">
            <NavLink className={navLinkClass} to="/login">
              {t('navigation.login')}
            </NavLink>
            <NavLink className={navLinkClass} to="/register">
              {t('navigation.register')}
            </NavLink>
          </div>
        )}
      </div>
    </header>
  );
}

export default Navbar;
