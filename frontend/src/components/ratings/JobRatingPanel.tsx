import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

import { createRating, getRatingEligibility } from '../../api/ratingsApi';
import type { CreateRatingRequest } from '../../types/rating';
import Button from '../ui/Button';
import UserAvatar from '../profile/UserAvatar';
import RatingModal from './RatingModal';

type JobRatingPanelProps = {
  compact?: boolean;
  enabled: boolean;
  jobId: string;
};

function JobRatingPanel({ compact = false, enabled, jobId }: JobRatingPanelProps) {
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

  return (
    <div>
      {!compact ? (
        <div className="flex items-center gap-3">
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
        <p className={`${compact ? '' : 'mt-3'} text-sm font-semibold text-body`}>
          {t('ratings.job.submitted')}
        </p>
      ) : (
        <div className={compact ? '' : 'mt-4'}>
          <Button
            className="!w-fit"
            onClick={() => {
              ratingMutation.reset();
              setIsModalOpen(true);
            }}
          >
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
