import { useTranslation } from 'react-i18next';

import type { JobDetails } from '../../types/job';

type JobDetailsHighlightsProps = {
  job: JobDetails;
};

function JobDetailsHighlights({ job }: JobDetailsHighlightsProps) {
  const { t } = useTranslation();

  return (
    <dl className="grid gap-3 sm:grid-cols-2">
      <div className="rounded-lg border border-border bg-page p-4">
        <dt className="text-sm font-bold text-heading">{t('jobs.details.hourly_rate')}</dt>
        <dd className="mt-1 text-lg font-bold text-heading">
          {job.hourlyRate} {job.currency}
        </dd>
      </div>
      <div className="rounded-lg border border-border bg-page p-4">
        <dt className="text-sm font-bold text-heading">{t('jobs.details.location')}</dt>
        <dd className="mt-1 text-body">{job.locationName}</dd>
      </div>
    </dl>
  );
}

export default JobDetailsHighlights;
