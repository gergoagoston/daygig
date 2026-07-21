import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import Navbar from './components/common/Navbar';
import ProtectedRoute from './components/common/ProtectedRoute';
import JobDetailsPage from './pages/JobDetailsPage';
import JobsPage from './pages/JobsPage';
import MyApplicationsPage from './pages/MyApplicationsPage';
import MyJobsPage from './pages/MyJobsPage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';

function App() {
  const location = useLocation();
  const { i18n } = useTranslation();
  const currentLanguage = (i18n.resolvedLanguage ?? i18n.language).slice(0, 2);

  return (
    <div className="min-h-svh bg-page">
      <div key={`${location.pathname}-${currentLanguage}`} className="view-transition">
        <Navbar />
        <Routes location={location}>
          <Route path="/" element={<Navigate to="/jobs" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/jobs" element={<JobsPage />} />
          <Route path="/jobs/:id" element={<JobDetailsPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/my-jobs" element={<MyJobsPage />} />
            <Route path="/my-applications" element={<MyApplicationsPage />} />
            {/* Bármilyen új védett oldalt ide tehetsz a jövőben */}
          </Route>
        </Routes>
      </div>
    </div>
  );
}

export default App;
