import { useNavigate } from 'react-router-dom';

import useAuth from '../hooks/useAuth';

function JobsPage() {
  const navigate = useNavigate();
  const { logout, user } = useAuth();

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <main className="page-shell">
      <section className="dashboard-panel">
        <p className="eyebrow">DayGig</p>
        <h1>Jobs</h1>
        <p>Signed in as {user?.email}.</p>
        <button className="secondary-button" type="button" onClick={handleLogout}>
          Log out
        </button>
      </section>
    </main>
  );
}

export default JobsPage;
