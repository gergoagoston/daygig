import { forwardRef } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import useAuth from '../../hooks/useAuth';
import NotificationMenu, { type NotificationMenuProps } from '../notifications/NotificationMenu';
import UserAvatar from '../profile/UserAvatar';
import NavbarLanguageSelect from './NavbarLanguageSelect';

type DesktopNavbarProps = {
  active: boolean;
  onCreateJob: () => void;
  notificationMenuProps: NotificationMenuProps;
};

const DesktopNavbar = forwardRef<HTMLElement, DesktopNavbarProps>(function DesktopNavbar(
  { active, onCreateJob, notificationMenuProps },
  ref
) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { isAuthenticated, isInitializing, logout, user } = useAuth();
  const accountLabel = user ? `${user.firstName} ${user.lastName}`.trim() || user.email : '';

  const handleLogout = async () => {
    await logout();
    navigate('/jobs', { replace: true });
  };

  return (
    <header
      ref={ref}
      className="desktop-navbar sticky top-0 z-50 min-h-18 shrink-0 grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-3 border-b border-border bg-surface px-8 py-4 shadow-sm"
      data-active={active}
      aria-hidden={active ? undefined : true}
      inert={!active}
    >
      <nav
        className="flex items-center gap-2 justify-self-start"
        aria-label={t('navigation.main')}
        data-navbar-section="start"
      >
        {isAuthenticated ? (
          <>
            <NavLink className="navbar-gomb" to="/my-jobs">
              {t('navigation.my_jobs')}
            </NavLink>
            <NavLink className="navbar-gomb" to="/my-applications">
              {t('navigation.my_applications')}
            </NavLink>
            <NavLink className="navbar-gomb" to="/conversations">
              {t('navigation.messages')}
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
        data-navbar-section="center"
      >
        {t('navigation.brand')}
      </NavLink>

      <div
        className="flex items-center justify-end gap-3 justify-self-end"
        data-navbar-section="end"
      >
        {isInitializing ? null : isAuthenticated ? (
          <div className="flex min-w-0 items-center justify-end gap-2">
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
          <div className="flex items-center gap-2">
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
});

export default DesktopNavbar;
