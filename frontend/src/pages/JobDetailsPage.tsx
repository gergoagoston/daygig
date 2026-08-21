import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { useLocation, useNavigate, useParams } from 'react-router-dom';

import { getJobById } from '../api/jobsApi';
import JobApplyPanel from '../components/jobs/JobApplyPanel';
import JobDetailsHeader from '../components/jobs/JobDetailsHeader';
import JobDetailsHighlights from '../components/jobs/JobDetailsHighlights';
import JobDetailsInfo from '../components/jobs/JobDetailsInfo';
import ApproximateLocationMap from '../components/map/ApproximateLocationMap';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';
import useApplyToJob from '../hooks/useApplyToJob';
import useAuth from '../hooks/useAuth';

function JobDetailsPage() {
  const { i18n, t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated, isInitializing, user } = useAuth();
  const currentJobId = id ?? '';
  const hasJobId = Boolean(id);
  const {
    data: job,
    isError,
    isLoading,
  } = useQuery({
    queryKey: ['job', id],
    queryFn: () => getJobById(id ?? ''),
    enabled: hasJobId,
  });
  const {
    apply,
    applicationId,
    applicationStatus,
    applyMessage,
    hasApplied,
    isApplying,
    isCheckingApplicationStatus,
  } = useApplyToJob({
    jobId: currentJobId,
    enabled: hasJobId && isAuthenticated && !isInitializing,
  });
  const language = i18n.resolvedLanguage ?? i18n.language;
  const isOwnJob = Boolean(job?.owner.id && user?.id && job.owner.id === user.id);

  const handleApply = () => {
    if (!id) {
      return;
    }

    if (!isAuthenticated) {
      navigate('/login', { state: { from: location } });
      return;
    }

    apply();
  };

  return (
    <main className="app-page">
      <div className="mx-auto w-full max-w-6xl">
        {isLoading ? <LoadingState message={t('jobs.details.loading')} /> : null}

        {(!hasJobId || isError) && !isLoading ? (
          <ErrorState message={t('jobs.details.errors.load_failed')} />
        ) : null}

        {job && !isLoading && !isError ? (
          <section className="overflow-hidden rounded-lg border border-border bg-surface shadow-panel">
            <div className="border-b border-border bg-page px-6 py-7 sm:px-8">
              <JobDetailsHeader category={job.category} title={job.title} />
            </div>

            <div className="grid gap-0 lg:grid-cols-[minmax(0,1fr)_400px]">
              <article className="grid min-w-0 gap-7 px-6 py-7 sm:px-8">
                <div className="max-w-3xl">
                  <p className="whitespace-pre-line text-lg leading-relaxed text-body">
                    {job.description}
                  </p>
                </div>

                <ApproximateLocationMap
                  fallback={t('jobs.details.map.missing_location')}
                  label={t('jobs.details.map.approximate_location')}
                  latitude={job.approximateLatitude}
                  longitude={job.approximateLongitude}
                  radiusMeters={job.locationRadiusMeters}
                />

                <JobDetailsHighlights job={job} />
              </article>

              <aside className="border-t border-border bg-page px-6 py-7 sm:px-8 lg:border-l lg:border-t-0">
                <div className="flex flex-col gap-6 lg:sticky lg:top-24">
                  <JobDetailsInfo isOwnJob={isOwnJob} job={job} language={language} />

                  <div>
                    <JobApplyPanel
                      applyMessage={applyMessage}
                      applicationId={applicationId}
                      applicationStatus={applicationStatus}
                      canRate={
                        hasJobId &&
                        isAuthenticated &&
                        !isInitializing &&
                        !isOwnJob &&
                        job.status === 'COMPLETED'
                      }
                      hasApplied={hasApplied}
                      isApplyDisabled={!hasJobId || isInitializing}
                      isApplying={isApplying}
                      isCheckingApplicationStatus={isCheckingApplicationStatus}
                      isOwnJob={isOwnJob}
                      jobId={job.id}
                      jobStatus={job.status}
                      completedApplicationId={job.completedApplicationId}
                      onApply={handleApply}
                    />
                  </div>
                </div>
              </aside>
            </div>
          </section>
        ) : null}
      </div>
    </main>
  );
}

export default JobDetailsPage;
