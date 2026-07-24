import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';

import { getMyJobs } from '../api/jobsApi';
import JobsGrid from '../components/jobs/JobsGrid';
import EmptyState from '../components/ui/EmptyState';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';

function MyJobsPage() {
  const { t } = useTranslation();
  const {
    data: jobs = [],
    isError,
    isLoading,
  } = useQuery({
    queryKey: ['jobs', 'mine'],
    queryFn: getMyJobs,
  });

  return (
    <main className="app-page">
      <section className="w-full">
        {isLoading ? <LoadingState message={t('posted_jobs.loading')} /> : null}

        {!isLoading && isError ? (
          <ErrorState message={t('posted_jobs.errors.load_failed')} />
        ) : null}

        {!isLoading && !isError && jobs.length === 0 ? (
          <EmptyState
            title={t('posted_jobs.empty.title')}
            description={t('posted_jobs.empty.description')}
          />
        ) : null}

        {!isLoading && !isError && jobs.length > 0 ? <JobsGrid jobs={jobs} /> : null}
      </section>
    </main>
  );
}

export default MyJobsPage;
