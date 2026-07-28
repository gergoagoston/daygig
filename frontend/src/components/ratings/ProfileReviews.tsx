import { useQueries } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import { getReceivedRatings } from '../../api/ratingsApi';
import type { Rating, RatingContext } from '../../types/rating';
import { formatDate } from '../../utils/formatDate';
import UserAvatar from '../profile/UserAvatar';

type ReviewItemProps = {
  language: string;
  rating: Rating;
};

function ReviewItem({ language, rating }: ReviewItemProps) {
  const { t } = useTranslation();
  const reviewerName = `${rating.reviewer.firstName} ${rating.reviewer.lastName}`.trim();

  return (
    <article className="rounded-lg border border-border bg-surface p-4 shadow-panel">
      <div className="flex items-start gap-3">
        <UserAvatar
          avatarUrl={rating.reviewer.avatarUrl}
          firstName={rating.reviewer.firstName}
          lastName={rating.reviewer.lastName}
          size="small"
        />
        <div className="min-w-0 flex-1">
          <Link className="wrap-break-word no-underline!" to={`/users/${rating.reviewer.id}`}>
            {reviewerName}
          </Link>
          <div
            className="mt-1 text-lg tracking-normal text-primary"
            aria-label={t('ratings.review.stars', { count: rating.stars })}
          >
            <span aria-hidden="true">
              {'★'.repeat(rating.stars)}
              <span className="text-border">{'★'.repeat(5 - rating.stars)}</span>
            </span>
          </div>
        </div>
      </div>

      {rating.comment ? (
        <p className="mt-3 whitespace-pre-line leading-relaxed text-heading">{rating.comment}</p>
      ) : null}

      <div className="mt-4 flex flex-wrap items-center justify-between gap-2 text-sm text-body">
        <Link className="wrap-break-word no-underline!" to={`/jobs/${rating.jobId}`}>
          {rating.jobTitle}
        </Link>
        <time dateTime={rating.createdAt}>{formatDate(rating.createdAt, language)}</time>
      </div>
    </article>
  );
}

type ReviewColumnProps = {
  context: RatingContext;
  isError: boolean;
  isLoading: boolean;
  language: string;
  ratings: Rating[] | undefined;
};

function ReviewColumn({ context, isError, isLoading, language, ratings }: ReviewColumnProps) {
  const { t } = useTranslation();

  return (
    <section aria-labelledby={`reviews-${context.toLowerCase()}`}>
      <h3 id={`reviews-${context.toLowerCase()}`} className="text-lg font-bold text-heading">
        {t(`ratings.context.${context}`)}
      </h3>

      {isLoading ? <p className="mt-3 text-body">{t('ratings.reviews.loading')}</p> : null}
      {isError ? (
        <p className="mt-3 font-semibold text-danger" role="alert">
          {t('ratings.errors.load_failed')}
        </p>
      ) : null}
      {!isLoading && !isError && ratings?.length === 0 ? (
        <p className="mt-3 text-body">{t('ratings.reviews.empty')}</p>
      ) : null}

      {ratings && ratings.length > 0 ? (
        <div className="mt-4 grid gap-4">
          {ratings.map((rating) => (
            <ReviewItem key={rating.id} language={language} rating={rating} />
          ))}
        </div>
      ) : null}
    </section>
  );
}

type ProfileReviewsProps = {
  userId: string;
};

function ProfileReviews({ userId }: ProfileReviewsProps) {
  const { i18n, t } = useTranslation();
  const [workerRatingsQuery, ownerRatingsQuery] = useQueries({
    queries: (['WORKER', 'JOB_OWNER'] as const).map((context) => ({
      queryKey: ['user-ratings', userId, context],
      queryFn: () => getReceivedRatings(userId, context),
    })),
  });
  const language = i18n.resolvedLanguage ?? i18n.language;

  return (
    <section className="py-7" aria-labelledby="profile-reviews-title">
      <h2 id="profile-reviews-title" className="text-xl font-bold text-heading">
        {t('ratings.reviews.title')}
      </h2>
      <div className="mt-5 grid items-start gap-8 md:grid-cols-2">
        <ReviewColumn
          context="WORKER"
          isError={workerRatingsQuery.isError}
          isLoading={workerRatingsQuery.isLoading}
          language={language}
          ratings={workerRatingsQuery.data}
        />
        <ReviewColumn
          context="JOB_OWNER"
          isError={ownerRatingsQuery.isError}
          isLoading={ownerRatingsQuery.isLoading}
          language={language}
          ratings={ownerRatingsQuery.data}
        />
      </div>
    </section>
  );
}

export default ProfileReviews;
