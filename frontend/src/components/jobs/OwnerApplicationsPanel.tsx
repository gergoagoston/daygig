import { useTranslation } from 'react-i18next';

import useJobApplications from '../../hooks/useJobApplications';
import useCompleteJob from '../../hooks/useCompleteJob';
import useUpdateOwnerApplicationStatus from '../../hooks/useUpdateOwnerApplicationStatus';
import type { JobStatus } from '../../types/job';
import OwnerApplicationListItem from './OwnerApplicationListItem';

type OwnerApplicationsPanelProps = {
  jobId: string;
  jobStatus: JobStatus;
  completedApplicationId: string | null;
};

function OwnerApplicationsPanel({
  jobId,
  jobStatus,
  completedApplicationId,
}: OwnerApplicationsPanelProps) {
  const { t } = useTranslation();
  const isJobOpen = jobStatus === 'OPEN';
  const { applications, isError, isLoading } = useJobApplications(jobId);
  const { isUpdateError, isUpdating, updateStatus, updatingApplicationId } =
    useUpdateOwnerApplicationStatus(jobId);
  const { complete, completingApplicationId, isCompleteError, isCompleting } =
    useCompleteJob(jobId);

  const handleComplete = (applicationId: string) => {
    if (window.confirm(t('posted_jobs.applications.actions.complete_confirm'))) {
      complete(applicationId);
    }
  };

  return (
    <section className="grid gap-3" aria-labelledby={`job-${jobId}-applications`}>
      <div className="flex shrink-0 items-center justify-between gap-3">
        <h3 id={`job-${jobId}-applications`} className="text-sm font-bold text-heading">
          {t('posted_jobs.applications.title')}
        </h3>
        <span className="text-xs font-bold text-body">
          {t('posted_jobs.applications.count', { count: applications.length })}
        </span>
      </div>

      <div className="max-h-72 overflow-y-auto rounded-md border border-border bg-surface sm:max-h-80 lg:max-h-96">
        {isLoading ? (
          <p className="px-3 py-4 text-sm text-body">{t('posted_jobs.applications.loading')}</p>
        ) : null}

        {!isLoading && isError ? (
          <p className="px-3 py-4 text-sm text-danger">
            {t('posted_jobs.applications.errors.load_failed')}
          </p>
        ) : null}

        {!isLoading && !isError && applications.length === 0 ? (
          <p className="px-3 py-4 text-sm text-body">{t('posted_jobs.applications.empty')}</p>
        ) : null}

        {!isLoading && !isError && applications.length > 0 ? (
          <ul className="divide-y divide-border">
            {applications.map((application) => (
              <OwnerApplicationListItem
                key={application.id}
                application={application}
                completedApplicationId={completedApplicationId}
                isCompleting={isCompleting && completingApplicationId === application.id}
                isJobOpen={isJobOpen}
                isRejecting={isUpdating && updatingApplicationId === application.id}
                onComplete={handleComplete}
                onReject={(applicationId) => updateStatus(applicationId, 'REJECTED')}
              />
            ))}
          </ul>
        ) : null}
      </div>

      {isUpdateError ? (
        <p className="text-sm text-danger">{t('posted_jobs.applications.errors.update_failed')}</p>
      ) : null}

      {isCompleteError ? (
        <p className="text-sm text-danger">
          {t('posted_jobs.applications.errors.complete_failed')}
        </p>
      ) : null}
    </section>
  );
}

export default OwnerApplicationsPanel;
