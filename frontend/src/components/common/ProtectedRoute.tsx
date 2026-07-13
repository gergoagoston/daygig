import { Navigate, Outlet, useLocation } from 'react-router-dom';

import useAuth from '../../hooks/useAuth';

function ProtectedRoute() {
  const { isAuthenticated, isInitializing } = useAuth();
  const location = useLocation();

  if (isInitializing) {
    return <main className="app-page-centered">Loading...</main>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return <Outlet />;
}

export default ProtectedRoute;
