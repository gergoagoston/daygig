import { useState, type SubmitEvent } from 'react';
import { useTranslation } from 'react-i18next';

import type { CreateRatingRequest, RatingParticipant } from '../../types/rating';
import Button from '../ui/Button';
import Modal from '../ui/Modal';
import Textarea from '../ui/Textarea';
import UserAvatar from '../profile/UserAvatar';
import StarRatingInput from './StarRatingInput';

type RatingModalProps = {
  isOpen: boolean;
  isSubmitting: boolean;
  reviewee: RatingParticipant;
  submitError: boolean;
  onClose: () => void;
  onSubmit: (request: CreateRatingRequest) => void;
};

function RatingModal({
  isOpen,
  isSubmitting,
  reviewee,
  submitError,
  onClose,
  onSubmit,
}: RatingModalProps) {
  const { t } = useTranslation();
  const [comment, setComment] = useState('');
  const [stars, setStars] = useState(0);

  const fullName = `${reviewee.firstName} ${reviewee.lastName}`.trim();

  const handleSubmit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (stars === 0) {
      return;
    }

    onSubmit({
      stars,
      comment: comment.trim() || null,
    });
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={isSubmitting ? () => undefined : onClose}
      title={t('ratings.form.title', { name: fullName })}
      titleId="rating-modal-title"
    >
      <form onSubmit={handleSubmit}>
        <div className="flex items-center gap-3">
          <UserAvatar
            avatarUrl={reviewee.avatarUrl}
            firstName={reviewee.firstName}
            lastName={reviewee.lastName}
            size="small"
          />
          <p className="font-bold text-heading">{fullName}</p>
        </div>

        <div className="mt-6">
          <StarRatingInput disabled={isSubmitting} value={stars} onChange={setStars} />
        </div>

        <div className="mt-6">
          <Textarea
            id="rating-comment"
            label={t('ratings.form.comment')}
            maxLength={500}
            rows={5}
            disabled={isSubmitting}
            placeholder={t('ratings.form.comment_placeholder')}
            value={comment}
            onChange={(event) => setComment(event.target.value)}
          />
          <p className="mt-2 text-right text-sm text-body">
            {t('ratings.form.characters', { count: comment.length })}
          </p>
        </div>

        {submitError ? (
          <p className="mt-4 font-semibold text-danger" role="alert">
            {t('ratings.errors.submit_failed')}
          </p>
        ) : null}

        <div className="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <button
            className="inline-flex items-center justify-center rounded-lg border border-border px-4.5 py-3 font-bold text-heading transition hover:bg-page disabled:opacity-65"
            type="button"
            disabled={isSubmitting}
            onClick={onClose}
          >
            {t('ratings.form.cancel')}
          </button>
          <div className="sm:w-fit">
            <Button type="submit" disabled={stars === 0} isLoading={isSubmitting}>
              {isSubmitting ? t('ratings.form.submitting') : t('ratings.form.submit')}
            </Button>
          </div>
        </div>
      </form>
    </Modal>
  );
}

export default RatingModal;
