import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';

import type { ApplyMessage } from '../../hooks/useApplyToJob';
import { useConversations, useCreateConversation } from '../../hooks/useConversations';
import useWithdrawApplication from '../../hooks/useWithdrawApplication';
import type { ApplicationStatus } from '../../types/application';
import type { JobStatus } from '../../types/job';
import StartConversationModal from '../conversations/StartConversationModal';
import JobRatingPanel from '../ratings/JobRatingPanel';
import Button, { compactActionClassName } from '../ui/Button';
import OwnerApplicationsPanel from './OwnerApplicationsPanel';

type JobApplyPanelProps = {
  applyMessage: ApplyMessage | null;
  applicationId?: string;
  applicationStatus?: ApplicationStatus;
  canRate: boolean;
  hasApplied: boolean;
  isApplyDisabled: boolean;
  isApplying: boolean;
  isCheckingApplicationStatus: boolean;
  jobId: string;
  jobStatus: JobStatus;
  completedApplicationId: string | null;
  isOwnJob: boolean;
  onApply: () => void;
};

function getMessageClassName(type: ApplyMessage['type']) {
  if (type === 'success') {
    return 'border-primary bg-primary-soft text-primary';
  }

  if (type === 'warning') {
    return 'border-border bg-page text-body';
  }

  return 'border-danger bg-surface text-danger';
}

function getApplicationStatusClassName(status: ApplicationStatus) {
  if (status === 'REJECTED') {
    return 'border-danger bg-danger-soft text-danger';
  }

  if (status === 'WITHDRAWN') {
    return 'border-border bg-page text-body';
  }

  return 'border-primary bg-primary-soft text-primary';
}

function JobApplyPanel({
  applyMessage,
  applicationId,
  applicationStatus,
  canRate,
  hasApplied,
  isApplyDisabled,
  isApplying,
  isCheckingApplicationStatus,
  jobId,
  jobStatus,
  completedApplicationId,
  isOwnJob,
  onApply,
}: JobApplyPanelProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [isStartConversationOpen, setIsStartConversationOpen] = useState(false);
  const { isWithdrawError, isWithdrawing, withdraw } = useWithdrawApplication(
    applicationId ?? '',
    jobId
  );
  const { conversations } = useConversations(hasApplied && !isOwnJob);
  const conversation = applicationId
    ? (conversations.find((item) => item.applicationId === applicationId) ??
      conversations.find((item) => item.jobId === jobId))
    : undefined;
  const createConversation = useCreateConversation();
  const canWithdraw =
    Boolean(applicationId) && applicationStatus === 'ACTIVE' && jobStatus === 'OPEN';
  const canStartConversation =
    Boolean(applicationId) &&
    hasApplied &&
    !isOwnJob &&
    !conversation &&
    applicationStatus === 'ACTIVE';

  const handleWithdraw = () => {
    if (window.confirm(t('applications.card.withdraw_confirm'))) {
      withdraw();
    }
  };

  const closeStartConversation = () => {
    if (!createConversation.isPending) {
      setIsStartConversationOpen(false);
      createConversation.reset();
    }
  };

  const startConversation = (content: string) => {
    if (!applicationId) {
      return;
    }

    createConversation.mutate(
      { applicationId, content },
      {
        onSuccess: (createdConversation) => {
          setIsStartConversationOpen(false);
          navigate(`/conversations/${createdConversation.id}`);
        },
      }
    );
  };

  return (
    <div className="grid w-full gap-4">
      {applyMessage ? (
        <p
          className={`rounded-lg border p-4 font-semibold ${getMessageClassName(applyMessage.type)}`}
        >
          {applyMessage.text}
        </p>
      ) : null}

      {isOwnJob && jobId ? (
        <OwnerApplicationsPanel
          completedApplicationId={completedApplicationId}
          jobId={jobId}
          jobStatus={jobStatus}
        />
      ) : isCheckingApplicationStatus ? null : hasApplied && !applyMessage ? (
        <div className="grid gap-3">
          {applicationStatus && applicationStatus !== 'ACTIVE' ? (
            <p
              className={`rounded-lg border p-4 font-semibold ${getApplicationStatusClassName(
                applicationStatus
              )}`}
            >
              {t(`application.status.${applicationStatus}`)}
            </p>
          ) : null}

          {canWithdraw ? (
            <Button
              className="justify-self-stretch"
              isLoading={isWithdrawing}
              onClick={handleWithdraw}
            >
              {isWithdrawing ? t('applications.card.withdrawing') : t('applications.card.withdraw')}
            </Button>
          ) : null}

          {conversation || canStartConversation || canRate ? (
            <div className="flex flex-wrap gap-2">
              {canRate ? <JobRatingPanel compact enabled jobId={jobId} showReviewee /> : null}

              {conversation ? (
                <Link
                  className={`${compactActionClassName} order-2 text-heading! no-underline! hover:border-primary hover:bg-primary hover:text-white!`}
                  to={`/conversations/${conversation.id}`}
                >
                  {t('conversations.actions.chat')}
                </Link>
              ) : canStartConversation ? (
                <button
                  className={`${compactActionClassName} order-2 hover:border-primary hover:bg-primary hover:text-white`}
                  type="button"
                  onClick={() => {
                    createConversation.reset();
                    setIsStartConversationOpen(true);
                  }}
                >
                  {t('conversations.actions.chat')}
                </button>
              ) : null}
            </div>
          ) : null}

          {isWithdrawError ? (
            <p className="text-sm text-danger">{t('applications.card.withdraw_failed')}</p>
          ) : null}
        </div>
      ) : jobStatus !== 'OPEN' ? (
        <p className="rounded-lg border border-border bg-page p-4 font-semibold text-body">
          {t('jobs.details.apply.job_closed')}
        </p>
      ) : hasApplied ? null : (
        <Button
          className="justify-self-stretch"
          disabled={isApplyDisabled}
          isLoading={isApplying}
          onClick={onApply}
        >
          {isApplying ? t('jobs.details.apply.submitting') : t('jobs.details.apply.button')}
        </Button>
      )}

      {isStartConversationOpen ? (
        <StartConversationModal
          isError={createConversation.isError}
          isOpen
          isSubmitting={createConversation.isPending}
          onClose={closeStartConversation}
          onSubmit={startConversation}
        />
      ) : null}
    </div>
  );
}

export default JobApplyPanel;
