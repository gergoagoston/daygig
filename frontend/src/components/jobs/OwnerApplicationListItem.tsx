import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import type { OwnerApplication } from '../../types/application';

type OwnerApplicationListItemProps = {
  application: OwnerApplication;
  completedApplicationId: string | null;
  isCompleting: boolean;
  isJobOpen: boolean;
  isRejecting: boolean;
  onComplete: (applicationId: string) => void;
  onReject: (applicationId: string) => void;
};

const actionButtonClass =
  'inline-flex h-8 items-center justify-center whitespace-nowrap rounded-md border px-2.5 text-xs font-bold transition focus:outline-none focus:ring-2 focus:ring-primary-soft disabled:opacity-65';

function getApplicantName(application: OwnerApplication) {
  const fullName = `${application.applicant.firstName} ${application.applicant.lastName}`.trim();
  return fullName || application.applicant.id;
}

function OwnerApplicationListItem({
  application,
  completedApplicationId,
  isCompleting,
  isJobOpen,
  isRejecting,
  onComplete,
  onReject,
}: OwnerApplicationListItemProps) {
  const { t } = useTranslation();
  const isCompletedWorker = completedApplicationId === application.id;
  const canManage = isJobOpen && application.status === 'ACTIVE';
  const isBusy = isCompleting || isRejecting;

  return (
    <li className="px-3 py-2.5">
      <div className="grid gap-2">
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

        {canManage ? (
          <div className="flex flex-wrap gap-2">
            <button
              className={`${actionButtonClass} border-action bg-action text-action-text`}
              type="button"
              disabled={isBusy}
              onClick={() => onComplete(application.id)}
            >
              {isCompleting
                ? t('posted_jobs.applications.actions.completing')
                : t('posted_jobs.applications.actions.complete')}
            </button>
            <button
              className={`${actionButtonClass} border-danger bg-danger-soft text-danger`}
              type="button"
              disabled={isBusy}
              onClick={() => onReject(application.id)}
            >
              {isRejecting
                ? t('posted_jobs.applications.actions.updating')
                : t('posted_jobs.applications.actions.REJECTED')}
            </button>
          </div>
        ) : null}
      </div>
    </li>
  );
}

export default OwnerApplicationListItem;
