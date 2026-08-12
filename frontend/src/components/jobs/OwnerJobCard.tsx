import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import type { OwnerJob } from '../../types/job';

type OwnerJobCardProps = {
  job: OwnerJob;
};

function formatRate(value: number, language: string) {
  return new Intl.NumberFormat(language, {
    maximumFractionDigits: 2,
    minimumFractionDigits: 0,
  }).format(value);
}

function OwnerJobCard({ job }: OwnerJobCardProps) {
  const { i18n, t } = useTranslation();
  const language = i18n.resolvedLanguage ?? i18n.language;

  const hasApplications = job.applicationCount > 0;

  return (
    <Link
      className="flex h-full flex-col rounded-lg border border-border bg-surface p-5 text-heading no-underline! shadow-panel transition hover:border-primary"
      to={`/jobs/${job.id}`}
    >
      <div className="mb-4 flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h2 className="line-clamp-2 text-xl font-bold leading-snug text-heading">{job.title}</h2>
          <p className="mt-2 text-sm font-bold text-primary">
            {t(`jobs.categories.${job.category}`)}
          </p>
        </div>
        <span className="shrink-0 rounded-full bg-primary-soft px-3 py-1 text-sm font-bold text-primary">
          {t(`jobs.statuses.${job.status}`)}
        </span>
      </div>

      <p className="mb-4 line-clamp-3 text-body">{job.description}</p>

      <dl className="mb-5 grid gap-3 text-sm text-body sm:grid-cols-2">
        <div>
          <dt className="font-bold text-heading">{t('posted_jobs.card.location')}</dt>
          <dd>{job.locationName}</dd>
        </div>
        <div>
          <dt className="font-bold text-heading">{t('posted_jobs.card.hourly_rate')}</dt>
          <dd>
            {t('jobs.card.hourly_rate', {
              currency: job.currency,
              rate: formatRate(job.hourlyRate, language),
            })}
          </dd>
        </div>
      </dl>

      {hasApplications ? (
        <div className="mt-auto flex items-center justify-between gap-3 rounded-md border border-border px-3 py-2">
          <span className="text-sm font-bold text-heading">
            {t('posted_jobs.applications.title')}
          </span>
          <span className="text-xs font-bold text-body">
            {t('posted_jobs.applications.count', { count: job.applicationCount })}
          </span>
        </div>
      ) : null}
    </Link>
  );
}

export default OwnerJobCard;
