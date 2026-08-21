import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import type { OwnerApplication } from '../../types/application';
import UserAvatar from '../profile/UserAvatar';
import JobRatingPanel from '../ratings/JobRatingPanel';
import { compactActionClassName } from '../ui/Button';

type OwnerApplicationListItemProps = {
  application: OwnerApplication;
  completedApplicationId: string | null;
  conversationId?: string;
  isCompleting: boolean;
  isJobOpen: boolean;
  isRejecting: boolean;
  jobId: string;
  onComplete: (applicationId: string) => void;
  onReject: (applicationId: string) => void;
  onStartConversation: (applicationId: string) => void;
};

function getApplicantName(application: OwnerApplication) {
  const fullName = `${application.applicant.firstName} ${application.applicant.lastName}`.trim();
  return fullName || application.applicant.id;
}

function OwnerApplicationListItem({
  application,
  completedApplicationId,
  conversationId,
  isCompleting,
  isJobOpen,
  isRejecting,
  jobId,
  onComplete,
  onReject,
  onStartConversation,
}: OwnerApplicationListItemProps) {
  const { t } = useTranslation();
  const isCompletedWorker = completedApplicationId === application.id;
  const canManage = isJobOpen && application.status === 'ACTIVE';
  const canStartConversation = !conversationId && application.status === 'ACTIVE';
  const isBusy = isCompleting || isRejecting;

  return (
    <li className="px-3 py-2.5">
      <div className="grid gap-2">
        <div className="flex min-w-0 items-center gap-3">
          <UserAvatar
            avatarUrl={`/api/users/${application.applicant.id}/avatar`}
            firstName={application.applicant.firstName}
            lastName={application.applicant.lastName}
            size="small"
          />
          <div className="min-w-0">
            <p className="truncate text-sm font-bold">
              <Link className="no-underline!" to={`/users/${application.applicant.id}`}>
                {getApplicantName(application)}
              </Link>
            </p>
            <p className="text-xs font-semibold text-body">
              {isCompletedWorker
                ? t('posted_jobs.applications.completed_worker')
                : t(`application.status.${application.status}`)}
            </p>
          </div>
        </div>

        {canManage || conversationId || canStartConversation || isCompletedWorker ? (
          <div className="flex flex-wrap gap-2">
            {canManage ? (
              <>
                <button
                  className={`${compactActionClassName} hover:border-emerald-600 hover:bg-emerald-600 hover:text-white`}
                  type="button"
                  disabled={isBusy}
                  onClick={() => onComplete(application.id)}
                >
                  {isCompleting
                    ? t('posted_jobs.applications.actions.completing')
                    : t('posted_jobs.applications.actions.complete')}
                </button>
                <button
                  className={`${compactActionClassName} hover:border-danger hover:bg-danger hover:text-white`}
                  type="button"
                  disabled={isBusy}
                  onClick={() => onReject(application.id)}
                >
                  {isRejecting
                    ? t('posted_jobs.applications.actions.updating')
                    : t('posted_jobs.applications.actions.REJECTED')}
                </button>
              </>
            ) : null}

            {conversationId ? (
              <Link
                className={`${compactActionClassName} order-2 text-heading! no-underline! hover:border-primary hover:bg-primary hover:text-white!`}
                to={`/conversations/${conversationId}`}
              >
                {t('conversations.actions.chat')}
              </Link>
            ) : canStartConversation ? (
              <button
                className={`${compactActionClassName} order-2 hover:border-primary hover:bg-primary hover:text-white`}
                type="button"
                disabled={isBusy}
                onClick={() => onStartConversation(application.id)}
              >
                {t('conversations.actions.chat')}
              </button>
            ) : null}

            {isCompletedWorker ? <JobRatingPanel compact enabled jobId={jobId} /> : null}
          </div>
        ) : null}
      </div>
    </li>
  );
}

export default OwnerApplicationListItem;
