import '../../styles/Navbar.css';
import useAuth from '../../hooks/useAuth';
import useNotifications from '../../hooks/useNotifications';
import DesktopNavbar from './DesktopNavbar';
import MobileNavbar from './MobileNavbar';

type NavbarProps = {
  onCreateJob: () => void;
};

function Navbar({ onCreateJob }: NavbarProps) {
  const { isAuthenticated, isInitializing } = useAuth();
  const notificationMenuProps = useNotifications(isAuthenticated && !isInitializing);

  return (
    <>
      <MobileNavbar onCreateJob={onCreateJob} notificationMenuProps={notificationMenuProps} />
      <DesktopNavbar onCreateJob={onCreateJob} notificationMenuProps={notificationMenuProps} />
    </>
  );
}

export default Navbar;
