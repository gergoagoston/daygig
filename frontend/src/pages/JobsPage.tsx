import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';

import { getJobs } from '../api/jobsApi';
import JobsGrid from '../components/jobs/JobsGrid';
import EmptyState from '../components/ui/EmptyState';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';

const JOBS_PAGE_SIZE = 20;
const JOBS_SORT = 'createdAt,desc';

function JobsPage() {
  const { t } = useTranslation();
  const {
    data: jobsPage,
    isError,
    isLoading,
  } = useQuery({
    queryKey: ['jobs', { page: 0, size: JOBS_PAGE_SIZE, sort: JOBS_SORT }],
    queryFn: () => getJobs({ page: 0, size: JOBS_PAGE_SIZE, sort: JOBS_SORT }),
  });
  const jobs = jobsPage?.content ?? [];

  return (
    <main className="app-page">
      <section className="w-full">
        {isLoading ? <LoadingState message={t('jobs.loading')} /> : null}

        {!isLoading && isError ? <ErrorState message={t('jobs.errors.load_failed')} /> : null}

        {!isLoading && !isError && jobs.length === 0 ? (
          <EmptyState title={t('jobs.empty.title')} description={t('jobs.empty.description')} />
        ) : null}

        {!isLoading && !isError && jobs.length > 0 ? <JobsGrid jobs={jobs} /> : null}
      </section>
    </main>
  );
}

export default JobsPage;
