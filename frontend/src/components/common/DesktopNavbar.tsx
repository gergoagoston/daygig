import { NavLink, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import useAuth from '../../hooks/useAuth';
import NotificationMenu, { type NotificationMenuProps } from '../notifications/NotificationMenu';
import UserAvatar from '../profile/UserAvatar';
import NavbarLanguageSelect from './NavbarLanguageSelect';

type DesktopNavbarProps = {
  onCreateJob: () => void;
  notificationMenuProps: NotificationMenuProps;
};

function DesktopNavbar({ onCreateJob, notificationMenuProps }: DesktopNavbarProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { isAuthenticated, isInitializing, logout, user } = useAuth();
  const accountLabel = user ? `${user.firstName} ${user.lastName}`.trim() || user.email : '';

  const handleLogout = async () => {
    await logout();
    navigate('/jobs', { replace: true });
  };

  return (
    <header className="desktop-navbar sticky top-0 z-50 min-h-18 grid-cols-[1fr_auto_1fr] items-center gap-3 border-b border-border bg-surface px-8 py-4 shadow-sm">
      <nav className="flex items-center gap-2 justify-self-start" aria-label={t('navigation.main')}>
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
        className="justify-self-center text-lg font-extrabold text-heading !no-underline"
        to="/jobs"
      >
        DayGig
      </NavLink>

      <div className="flex flex-wrap items-center justify-end gap-3 justify-self-end">
        {isInitializing ? null : isAuthenticated ? (
          <div className="flex min-w-0 flex-wrap items-center justify-end gap-2">
            <NotificationMenu {...notificationMenuProps} />
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
                <span className="max-w-45 overflow-hidden text-ellipsis whitespace-nowrap font-bold text-heading">
                  {accountLabel}
                </span>
              </NavLink>
            ) : null}
            <button className="navbar-gomb" type="button" onClick={() => void handleLogout()}>
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

        <NavbarLanguageSelect />
      </div>
    </header>
  );
}

export default DesktopNavbar;
