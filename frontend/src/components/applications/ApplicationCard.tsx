import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import type { Application } from '../../types/application';
import { formatDate } from '../../utils/formatDate';

type ApplicationCardProps = {
  application: Application;
};

function formatRate(value: number, language: string) {
  return new Intl.NumberFormat(language, {
    maximumFractionDigits: 2,
    minimumFractionDigits: 0,
  }).format(value);
}

function ApplicationCard({ application }: ApplicationCardProps) {
  const { i18n, t } = useTranslation();
  const language = i18n.resolvedLanguage ?? i18n.language;
  const canChat = application.status === 'ACCEPTED';

  return (
    <article className="group flex h-full flex-col rounded-lg border border-border bg-surface p-5 text-heading shadow-panel">
      <div className="mb-4 flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h2 className="line-clamp-2 text-xl font-bold leading-snug text-heading">
            {application.job.title}
          </h2>
          <p className="mt-2 text-sm font-bold text-primary">
            {t(`jobs.categories.${application.job.category}`)}
          </p>
        </div>
        <span className="shrink-0 rounded-full bg-primary-soft px-3 py-1 text-sm font-bold text-primary">
          {t(`application.status.${application.status}`)}
        </span>
      </div>

      <dl className="grid gap-3 text-sm text-body sm:grid-cols-2">
        <div>
          <dt className="font-bold text-heading">{t('applications.card.location')}</dt>
          <dd>{application.job.locationName}</dd>
        </div>
        <div>
          <dt className="font-bold text-heading">{t('applications.card.hourly_rate')}</dt>
          <dd>
            {t('jobs.card.hourly_rate', {
              currency: application.job.currency,
              rate: formatRate(application.job.hourlyRate, language),
            })}
          </dd>
        </div>
        <div>
          <dt className="font-bold text-heading">{t('applications.card.job_status')}</dt>
          <dd>{t(`jobs.statuses.${application.job.status}`)}</dd>
        </div>
        <div>
          <dt className="font-bold text-heading">{t('applications.card.applied')}</dt>
          <dd>{formatDate(application.appliedAt, language)}</dd>
        </div>
      </dl>

      <div className="mt-5 flex translate-y-1 flex-wrap gap-3 opacity-0 transition duration-150 group-hover:translate-y-0 group-hover:opacity-100 group-focus-within:translate-y-0 group-focus-within:opacity-100">
        <Link className="navbar-gomb" to={`/jobs/${application.job.id}`}>
          {t('applications.card.view_job')}
        </Link>

        {canChat ? (
          <Link className="navbar-gomb" to={`/applications/${application.id}/chat`}>
            {t('applications.card.chat')}
          </Link>
        ) : (
          <button
            className="inline-flex items-center justify-center whitespace-nowrap rounded-lg border border-border bg-transparent px-3 py-2 text-sm font-bold text-body opacity-65"
            type="button"
            disabled
          >
            {t('applications.card.chat_locked')}
          </button>
        )}
      </div>
    </article>
  );
}

export default ApplicationCard;
