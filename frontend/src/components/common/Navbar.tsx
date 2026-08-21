import '../../styles/Navbar.css';
import { useRef } from 'react';

import useAuth from '../../hooks/useAuth';
import useDesktopNavbarFit from '../../hooks/useDesktopNavbarFit';
import useNotifications from '../../hooks/useNotifications';
import DesktopNavbar from './DesktopNavbar';
import MobileNavbar from './MobileNavbar';

type NavbarProps = {
  onCreateJob: () => void;
};

function Navbar({ onCreateJob }: NavbarProps) {
  const { isAuthenticated, isInitializing } = useAuth();
  const notificationMenuProps = useNotifications(isAuthenticated && !isInitializing);
  const desktopNavbarRef = useRef<HTMLElement>(null);
  const desktopNavbarFits = useDesktopNavbarFit(desktopNavbarRef);

  return (
    <>
      {!desktopNavbarFits ? (
        <MobileNavbar onCreateJob={onCreateJob} notificationMenuProps={notificationMenuProps} />
      ) : null}
      <DesktopNavbar
        ref={desktopNavbarRef}
        active={desktopNavbarFits}
        onCreateJob={onCreateJob}
        notificationMenuProps={notificationMenuProps}
      />
    </>
  );
}

export default Navbar;
