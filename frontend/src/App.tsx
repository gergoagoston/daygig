import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useState } from 'react';

import Navbar from './components/common/Navbar';
import ProtectedRoute from './components/common/ProtectedRoute';
import JobCreateModal from './components/jobs/JobCreateModal';
import ConversationsPage from './pages/ConversationsPage';
import JobDetailsPage from './pages/JobDetailsPage';
import JobsPage from './pages/JobsPage';
import MyApplicationsPage from './pages/MyApplicationsPage';
import MyJobsPage from './pages/MyJobsPage';
import ProfileSettingsPage from './pages/ProfileSettingsPage';
import TermsAndConditionsPage from './pages/TermsAndConditionsPage';
import UserProfilePage from './pages/UserProfilePage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';

function App() {
  const location = useLocation();
  const { i18n } = useTranslation();
  const [isCreateJobOpen, setIsCreateJobOpen] = useState(false);
  const currentLanguage = (i18n.resolvedLanguage ?? i18n.language).slice(0, 2);
  const isConversationsPage =
    location.pathname === '/conversations' || location.pathname.startsWith('/conversations/');

  return (
    <div
      className={`${isConversationsPage ? 'flex h-svh flex-col overflow-hidden' : 'min-h-svh'} bg-page`}
    >
      <Navbar onCreateJob={() => setIsCreateJobOpen(true)} />
      <div
        key={`${location.pathname}-${currentLanguage}`}
        className={`view-transition ${isConversationsPage ? 'min-h-0 flex-1 overflow-hidden' : ''}`}
      >
        <Routes location={location}>
          <Route path="/" element={<Navigate to="/jobs" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/terms-and-conditions" element={<TermsAndConditionsPage />} />
          <Route path="/jobs" element={<JobsPage />} />
          <Route path="/jobs/:id" element={<JobDetailsPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/conversations" element={<ConversationsPage />} />
            <Route path="/conversations/:conversationId" element={<ConversationsPage />} />
            <Route path="/my-jobs" element={<MyJobsPage />} />
            <Route path="/my-applications" element={<MyApplicationsPage />} />
            <Route path="/profile" element={<ProfileSettingsPage />} />
            <Route path="/users/:id" element={<UserProfilePage />} />
          </Route>
        </Routes>
      </div>
      <JobCreateModal isOpen={isCreateJobOpen} onClose={() => setIsCreateJobOpen(false)} />
    </div>
  );
}

export default App;
