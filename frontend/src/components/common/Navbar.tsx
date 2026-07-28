import { NavLink, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import useAuth from '../../hooks/useAuth';
import '../../styles/Navbar.css';
import UserAvatar from '../profile/UserAvatar';

const languages = [
  { code: 'hu', label: 'HU' },
  { code: 'en', label: 'EN' },
  { code: 'ro', label: 'RO' },
] as const;

type NavbarProps = {
  onCreateJob: () => void;
};

function Navbar({ onCreateJob }: NavbarProps) {
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
    <header className="sticky top-0 z-50 grid min-h-18 grid-cols-[1fr_auto] gap-3 border-b border-border bg-surface px-5 py-4 shadow-sm md:grid-cols-[1fr_auto_1fr] md:items-center md:px-8">
      <nav
        className="order-3 col-span-2 flex flex-wrap items-center justify-center gap-2 md:order-1 md:col-span-1 md:justify-self-start"
        aria-label={t('navigation.main')}
      >
        {isAuthenticated ? (
          <>
            <NavLink className="navbar-gomb" to="/my-jobs">
              {t('navigation.my_jobs')}
            </NavLink>
            <NavLink className="navbar-gomb" to="/my-applications">
              {t('navigation.my_applications')}
            </NavLink>
            <button
              className="navbar-create-gomb"
              type="button"
              aria-label={t('jobs.create.nav_label')}
              title={t('jobs.create.nav_label')}
              onClick={onCreateJob}
            />
          </>
        ) : null}
      </nav>

      <NavLink
        className="order-1 justify-self-start text-lg font-extrabold text-heading !no-underline md:order-2 md:justify-self-center"
        to="/jobs"
      >
        DayGig
      </NavLink>

      <div className="order-2 flex flex-wrap items-center justify-end gap-3 md:order-3 md:justify-self-end">
        {isInitializing ? null : isAuthenticated ? (
          <div className="flex min-w-0 flex-wrap items-center justify-end gap-2">
            {user ? (
              <NavLink
                className="flex min-w-0 items-center gap-2 rounded-full !no-underline"
                to="/profile"
                aria-label={t('navigation.profile')}
                title={t('navigation.profile')}
              >
                <UserAvatar
                  avatarUrl={user.avatarUrl}
                  firstName={user.firstName}
                  lastName={user.lastName}
                  size="small"
                />
                <span className="hidden max-w-28 overflow-hidden text-ellipsis whitespace-nowrap font-bold text-heading sm:block sm:max-w-45">
                  {accountLabel}
                </span>
              </NavLink>
            ) : null}
            <button className="navbar-gomb" type="button" onClick={handleLogout}>
              {t('navigation.logout')}
            </button>
          </div>
        ) : (
          <div className="flex flex-wrap items-center gap-2">
            <NavLink className="navbar-gomb" to="/login">
              {t('navigation.login')}
            </NavLink>
            <NavLink className="navbar-gomb" to="/register">
              {t('navigation.register')}
            </NavLink>
          </div>
        )}

        <label>
          <span className="sr-only">{t('navigation.language')}</span>
          <select
            className="rounded-lg border border-border bg-surface px-3 py-2 text-sm font-bold text-heading outline-none hover:border-primary focus:border-primary"
            value={currentLanguage.slice(0, 2)}
            onChange={(event) => void i18n.changeLanguage(event.target.value)}
          >
            {languages.map((language) => (
              <option key={language.code} value={language.code}>
                {language.label}
              </option>
            ))}
          </select>
        </label>
      </div>
    </header>
  );
}

export default Navbar;
