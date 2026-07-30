import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import useAuth from '../../hooks/useAuth';
import UserAvatar from '../profile/UserAvatar';
import NavbarLanguageSelect from './NavbarLanguageSelect';

type MobileNavbarProps = {
  onCreateJob: () => void;
};

function MobileNavbar({ onCreateJob }: MobileNavbarProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { isAuthenticated, isInitializing, logout, user } = useAuth();
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const closeMenu = () => setIsMenuOpen(false);

  const handleLogout = async () => {
    closeMenu();
    await logout();
    navigate('/jobs', { replace: true });
  };

  const handleCreateJob = () => {
    closeMenu();
    onCreateJob();
  };

  return (
    <header
      className="mobile-navbar sticky top-0 z-50 min-h-18 grid-cols-[1fr_auto] items-center gap-3 border-b border-border bg-surface px-5 py-3 shadow-sm"
      onKeyDown={(event) => {
        if (event.key === 'Escape') {
          closeMenu();
        }
      }}
    >
      <NavLink
        className="justify-self-start text-lg font-extrabold text-heading !no-underline"
        to="/jobs"
        onClick={closeMenu}
      >
        DayGig
      </NavLink>

      <div className="flex items-center justify-end gap-2">
        {!isInitializing && isAuthenticated && user ? (
          <NavLink
            className="rounded-full !no-underline"
            to="/profile"
            aria-label={t('navigation.profile')}
            title={t('navigation.profile')}
            onClick={closeMenu}
          >
            <UserAvatar
              avatarUrl={user.avatarUrl}
              firstName={user.firstName}
              lastName={user.lastName}
              size="small"
            />
          </NavLink>
        ) : null}

        <button
          className="navbar-menu-gomb"
          type="button"
          aria-controls="mobile-navigation"
          aria-expanded={isMenuOpen}
          aria-label={t('navigation.menu')}
          title={t('navigation.menu')}
          onClick={() => setIsMenuOpen((isOpen) => !isOpen)}
        >
          <span aria-hidden="true" />
        </button>
      </div>

      {isMenuOpen ? (
        <nav
          id="mobile-navigation"
          className="col-span-2 grid gap-2 border-t border-border pt-3"
          aria-label={t('navigation.main')}
        >
          {!isInitializing && isAuthenticated ? (
            <>
              <NavLink
                className="navbar-gomb w-full justify-start"
                to="/my-jobs"
                onClick={closeMenu}
              >
                {t('navigation.my_jobs')}
              </NavLink>
              <NavLink
                className="navbar-gomb w-full justify-start"
                to="/my-applications"
                onClick={closeMenu}
              >
                {t('navigation.my_applications')}
              </NavLink>
              <button
                className="navbar-gomb w-full justify-start"
                type="button"
                onClick={handleCreateJob}
              >
                {t('jobs.create.nav_label')}
              </button>
              <NavLink
                className="navbar-gomb w-full justify-start"
                to="/profile"
                onClick={closeMenu}
              >
                {t('navigation.profile')}
              </NavLink>
              <button
                className="navbar-gomb w-full justify-start"
                type="button"
                onClick={() => void handleLogout()}
              >
                {t('navigation.logout')}
              </button>
            </>
          ) : null}

          {!isInitializing && !isAuthenticated ? (
            <>
              <NavLink className="navbar-gomb w-full justify-start" to="/login" onClick={closeMenu}>
                {t('navigation.login')}
              </NavLink>
              <NavLink
                className="navbar-gomb w-full justify-start"
                to="/register"
                onClick={closeMenu}
              >
                {t('navigation.register')}
              </NavLink>
            </>
          ) : null}

          <NavbarLanguageSelect showLabel />
        </nav>
      ) : null}
    </header>
  );
}

export default MobileNavbar;
