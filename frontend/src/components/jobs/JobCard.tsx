import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import type { Job } from '../../types/job';

type JobCardProps = {
  job: Job;
};

function formatRate(value: number, language: string) {
  return new Intl.NumberFormat(language, {
    maximumFractionDigits: 2,
    minimumFractionDigits: 0,
  }).format(value);
}

function JobCard({ job }: JobCardProps) {
  const { i18n, t } = useTranslation();

  return (
    <Link
      className="flex h-full flex-col rounded-lg border border-border bg-surface p-5 text-heading !no-underline shadow-panel transition hover:border-primary"
      to={`/jobs/${job.id}`}
    >
      <div className="mb-4 flex items-start justify-between gap-3">
        <h2 className="line-clamp-2 text-xl font-bold leading-snug text-heading">{job.title}</h2>
        <span className="shrink-0 rounded-full bg-primary-soft px-3 py-1 text-sm font-bold text-primary">
          {t(`jobs.categories.${job.category}`)}
        </span>
      </div>

      <p className="mb-5 line-clamp-3 text-body">{job.description}</p>

      <div className="mt-auto flex items-end justify-between gap-4 text-sm font-semibold text-body">
        <p>{job.locationName}</p>
        <p className="shrink-0 text-right font-bold text-heading">
          {t('jobs.card.hourly_rate', {
            currency: job.currency,
            rate: formatRate(job.hourlyRate, i18n.resolvedLanguage ?? i18n.language),
          })}
        </p>
      </div>
    </Link>
  );
}

export default JobCard;
