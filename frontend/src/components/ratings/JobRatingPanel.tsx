import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

import { createRating, getRatingEligibility } from '../../api/ratingsApi';
import type { CreateRatingRequest } from '../../types/rating';
import Button, { compactActionClassName } from '../ui/Button';
import UserAvatar from '../profile/UserAvatar';
import RatingModal from './RatingModal';

type JobRatingPanelProps = {
  compact?: boolean;
  enabled: boolean;
  jobId: string;
  showReviewee?: boolean;
};

function JobRatingPanel({ compact = false, enabled, jobId, showReviewee }: JobRatingPanelProps) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const eligibilityQuery = useQuery({
    queryKey: ['rating-eligibility', jobId],
    queryFn: () => getRatingEligibility(jobId),
    enabled,
  });
  const eligibility = eligibilityQuery.data;
  const ratingMutation = useMutation({
    mutationFn: (request: CreateRatingRequest) => createRating(jobId, request),
    onSuccess: async () => {
      setIsModalOpen(false);
      await queryClient.invalidateQueries({ queryKey: ['rating-eligibility', jobId] });

      if (eligibility?.reviewee && eligibility.context) {
        await Promise.all([
          queryClient.invalidateQueries({
            queryKey: ['user-profile', eligibility.reviewee.id],
          }),
          queryClient.invalidateQueries({
            queryKey: ['user-ratings', eligibility.reviewee.id, eligibility.context],
          }),
        ]);
      }
    },
  });

  const openRatingModal = () => {
    ratingMutation.reset();
    setIsModalOpen(true);
  };

  if (!enabled || eligibilityQuery.isLoading) {
    return null;
  }

  if (eligibilityQuery.isError) {
    return (
      <p className="text-sm font-semibold text-danger" role="alert">
        {t('ratings.errors.eligibility_failed')}
      </p>
    );
  }

  if (!eligibility?.eligible || !eligibility.reviewee) {
    return null;
  }

  const reviewee = eligibility.reviewee;
  const fullName = `${reviewee.firstName} ${reviewee.lastName}`.trim();
  const shouldShowReviewee = showReviewee ?? !compact;

  return (
    <div className={compact ? 'contents' : undefined}>
      {shouldShowReviewee ? (
        <div className={`flex items-center gap-3 ${compact ? 'order-1 basis-full' : ''}`}>
          <UserAvatar
            avatarUrl={reviewee.avatarUrl}
            firstName={reviewee.firstName}
            lastName={reviewee.lastName}
            size="small"
          />
          <p className="min-w-0 wrap-break-word font-semibold text-heading">{fullName}</p>
        </div>
      ) : null}

      {eligibility.alreadyRated ? (
        <p className={`${compact ? 'order-3' : 'mt-3'} text-sm font-semibold text-body`}>
          {t('ratings.job.submitted')}
        </p>
      ) : compact ? (
        <button
          className={`${compactActionClassName} order-3 hover:border-primary hover:bg-primary hover:text-white`}
          type="button"
          onClick={openRatingModal}
        >
          {t('ratings.job.action')}
        </button>
      ) : (
        <div className="mt-4">
          <Button className="!w-fit" onClick={openRatingModal}>
            {t('ratings.job.action')}
          </Button>
        </div>
      )}

      {isModalOpen ? (
        <RatingModal
          isOpen
          isSubmitting={ratingMutation.isPending}
          reviewee={reviewee}
          submitError={ratingMutation.isError}
          onClose={() => setIsModalOpen(false)}
          onSubmit={(payload) => ratingMutation.mutate(payload)}
        />
      ) : null}
    </div>
  );
}

export default JobRatingPanel;
