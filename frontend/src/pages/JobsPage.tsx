import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';

import { getJobs } from '../api/jobsApi';
import JobCard from '../components/jobs/JobCard';
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

        {!isLoading && !isError && jobs.length > 0 ? (
          <div className="grid grid-cols-[repeat(auto-fit,minmax(400px,600px))] justify-center gap-4">
            {jobs.map((job) => (
              <JobCard key={job.id} job={job} />
            ))}
          </div>
        ) : null}
      </section>
    </main>
  );
}

export default JobsPage;
