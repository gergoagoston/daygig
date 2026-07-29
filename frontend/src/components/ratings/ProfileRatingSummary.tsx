import { useTranslation } from 'react-i18next';

import type { RatingSummary } from '../../types/rating';

type StarRatingDisplayProps = {
  label: string;
  value: number | null;
};

function StarRatingDisplay({ label, value }: StarRatingDisplayProps) {
  const filledPercentage = value === null ? 0 : Math.max(0, Math.min(100, (value / 5) * 100));

  return (
    <span className="relative inline-block text-xl leading-none" aria-label={label} title={label}>
      <span className="text-border" aria-hidden="true">
        ★★★★★
      </span>
      <span
        className="absolute inset-y-0 left-0 overflow-hidden whitespace-nowrap text-primary"
        style={{ width: `${filledPercentage}%` }}
        aria-hidden="true"
      >
        ★★★★★
      </span>
    </span>
  );
}

type RatingSummaryItemProps = {
  completedJobs: number;
  label: string;
  summary: RatingSummary;
};

function RatingSummaryItem({ completedJobs, label, summary }: RatingSummaryItemProps) {
  const { t } = useTranslation();
  const ratingLabel =
    summary.average === null
      ? t('ratings.summary.no_rating')
      : t('ratings.review.stars', { count: summary.average });

  return (
    <div className="rounded-md bg-page px-4 py-4">
      <div className="flex flex-wrap items-center gap-3">
        <h3 className="text-base font-bold text-heading">{label}</h3>
        <StarRatingDisplay label={ratingLabel} value={summary.average} />
      </div>
      <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-sm text-body">
        <span>{t('ratings.summary.review_count', { count: summary.count })}</span>
        <span>{t('ratings.summary.completed_jobs', { count: completedJobs })}</span>
      </div>
    </div>
  );
}

type ProfileRatingSummaryProps = {
  completedJobsAsOwner: number;
  completedJobsAsWorker: number;
  jobOwnerRating: RatingSummary;
  workerRating: RatingSummary;
};

function ProfileRatingSummary({
  completedJobsAsOwner,
  completedJobsAsWorker,
  jobOwnerRating,
  workerRating,
}: ProfileRatingSummaryProps) {
  const { t } = useTranslation();

  return (
    <section className="py-7" aria-labelledby="rating-summary-title">
      <h2 id="rating-summary-title" className="text-xl font-bold text-heading">
        {t('ratings.summary.title')}
      </h2>
      <div className="mt-4 grid gap-3 sm:grid-cols-2">
        <RatingSummaryItem
          completedJobs={completedJobsAsWorker}
          label={t('ratings.context.WORKER')}
          summary={workerRating}
        />
        <RatingSummaryItem
          completedJobs={completedJobsAsOwner}
          label={t('ratings.context.JOB_OWNER')}
          summary={jobOwnerRating}
        />
      </div>
    </section>
  );
}

export default ProfileRatingSummary;
