import { Navigate, Route, Routes } from 'react-router-dom';

import Navbar from './components/common/Navbar';
import ProtectedRoute from './components/common/ProtectedRoute';
import JobDetailsPage from './pages/JobDetailsPage';
import JobsPage from './pages/JobsPage';
import MyApplicationsPage from './pages/MyApplicationsPage';
import MyJobsPage from './pages/MyJobsPage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';

function App() {
  return (
    <div className="min-h-svh bg-page">
      <Navbar />
      <Routes>
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
  );
}

export default App;
