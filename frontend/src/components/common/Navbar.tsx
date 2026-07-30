import '../../styles/Navbar.css';
import DesktopNavbar from './DesktopNavbar';
import MobileNavbar from './MobileNavbar';

type NavbarProps = {
  onCreateJob: () => void;
};

function Navbar({ onCreateJob }: NavbarProps) {
  return (
    <>
      <MobileNavbar onCreateJob={onCreateJob} />
      <DesktopNavbar onCreateJob={onCreateJob} />
    </>
  );
}

export default Navbar;
