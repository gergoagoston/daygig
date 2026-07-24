import { useTranslation } from 'react-i18next';

import type { JobDetails } from '../../types/job';
import { formatDate } from '../../utils/formatDate';

type JobDetailsInfoProps = {
  job: JobDetails;
  language: string;
};

function JobDetailsInfo({ job, language }: JobDetailsInfoProps) {
  const { t } = useTranslation();

  return (
    <dl className="grid gap-4">
      <div>
        <dt className="text-sm font-bold text-heading">{t('jobs.details.owner')}</dt>
        <dd className="mt-1 text-body">
          {job.owner.firstName} {job.owner.lastName}
        </dd>
      </div>
      <div>
        <dt className="text-sm font-bold text-heading">{t('jobs.details.published_on')}</dt>
        <dd className="mt-1 text-body">{formatDate(job.createdAt, language)}</dd>
      </div>
    </dl>
  );
}

export default JobDetailsInfo;
