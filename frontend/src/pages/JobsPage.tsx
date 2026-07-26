import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';

import { getJobs } from '../api/jobsApi';
import JobsGrid from '../components/jobs/JobsGrid';
import JobSearchPanel from '../components/jobs/search/JobSearchPanel';
import {
  defaultJobSearchValues,
  hasJobFilters,
  toJobsQueryParams,
  type JobSearchFormValues,
} from '../components/jobs/search/jobSearchFormModel';
import EmptyState from '../components/ui/EmptyState';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';
import type { JobsQueryParams } from '../types/job';

const JOBS_PAGE_SIZE = 20;

function JobsPage() {
  const { t } = useTranslation();
  const [queryParams, setQueryParams] = useState<JobsQueryParams>(() =>
    toJobsQueryParams(defaultJobSearchValues, 0, JOBS_PAGE_SIZE)
  );
  const {
    data: jobsPage,
    isError,
    isFetching,
    isLoading,
  } = useQuery({
    queryKey: ['jobs', queryParams],
    queryFn: () => getJobs(queryParams),
  });
  const jobs = jobsPage?.content ?? [];
  const hasActiveFilters = hasJobFilters(queryParams);

  function handleSearch(values: JobSearchFormValues) {
    setQueryParams(toJobsQueryParams(values, 0, JOBS_PAGE_SIZE));
  }

  return (
    <main className="app-page">
      <div className="grid w-full gap-6">
        <JobSearchPanel isSearching={isFetching} onSearch={handleSearch} />

        <section className="w-full" aria-live="polite">
          {isLoading ? <LoadingState message={t('jobs.loading')} /> : null}

          {!isLoading && isError ? <ErrorState message={t('jobs.errors.load_failed')} /> : null}

          {!isLoading && !isError && jobs.length === 0 ? (
            <EmptyState
              title={t(hasActiveFilters ? 'jobs.empty.filtered_title' : 'jobs.empty.title')}
              description={t(
                hasActiveFilters ? 'jobs.empty.filtered_description' : 'jobs.empty.description'
              )}
            />
          ) : null}

          {!isLoading && !isError && jobs.length > 0 ? (
            <div className="grid gap-4">
              <p className="text-sm font-semibold text-body">
                {t('jobs.search.results_count', {
                  count: jobsPage?.totalElements ?? jobs.length,
                })}
              </p>
              <JobsGrid jobs={jobs} />
            </div>
          ) : null}
        </section>
      </div>
    </main>
  );
}

export default JobsPage;
