import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';

import { getMyApplications } from '../api/applicationsApi';
import ApplicationCard from '../components/applications/ApplicationCard';
import EmptyState from '../components/ui/EmptyState';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';

function MyApplicationsPage() {
  const { t } = useTranslation();
  const {
    data: applications = [],
    isError,
    isLoading,
  } = useQuery({
    queryKey: ['applications', 'mine'],
    queryFn: getMyApplications,
  });

  return (
    <main className="app-page">
      <section className="w-full">
        {isLoading ? <LoadingState message={t('applications.loading')} /> : null}

        {!isLoading && isError ? (
          <ErrorState message={t('applications.errors.load_failed')} />
        ) : null}

        {!isLoading && !isError && applications.length === 0 ? (
          <EmptyState
            title={t('applications.empty.title')}
            description={t('applications.empty.description')}
          />
        ) : null}

        {!isLoading && !isError && applications.length > 0 ? (
          <div className="grid grid-cols-[repeat(auto-fit,minmax(400px,600px))] justify-center gap-4">
            {applications.map((application) => (
              <ApplicationCard key={application.id} application={application} />
            ))}
          </div>
        ) : null}
      </section>
    </main>
  );
}

export default MyApplicationsPage;
