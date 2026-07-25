import { useTranslation } from 'react-i18next';

import type { ApplicationStatus, OwnerApplication } from '../../types/application';

type OwnerApplicationListItemProps = {
  application: OwnerApplication;
  isJobCompleted: boolean;
  isUpdating: boolean;
  updatingApplicationId?: string;
  updatingStatus?: ApplicationStatus;
  onUpdateStatus: (applicationId: string, status: ApplicationStatus) => void;
};

const actionButtonClass =
  'inline-flex h-8 items-center justify-center whitespace-nowrap rounded-md border px-2.5 text-xs font-bold transition focus:outline-none focus:ring-2 focus:ring-primary-soft disabled:opacity-65';

function getApplicantName(application: OwnerApplication) {
  const fullName = `${application.applicant.firstName} ${application.applicant.lastName}`.trim();
  return fullName || application.applicant.id;
}

function getAvailableActions(
  status: ApplicationStatus,
  isJobCompleted: boolean
): ApplicationStatus[] {
  if (isJobCompleted) {
    return [];
  }

  if (status === 'PENDING') {
    return ['ACCEPTED', 'REJECTED'];
  }

  if (status === 'ACCEPTED') {
    return ['REJECTED', 'COMPLETED'];
  }

  return [];
}

function getActionClass(status: ApplicationStatus) {
  if (status === 'ACCEPTED') {
    return `${actionButtonClass} border-primary bg-primary-soft text-primary hover:border-primary`;
  }

  if (status === 'COMPLETED') {
    return `${actionButtonClass} border-action bg-action text-action-text`;
  }

  return `${actionButtonClass} border-danger bg-danger-soft text-danger`;
}

function OwnerApplicationListItem({
  application,
  isJobCompleted,
  isUpdating,
  updatingApplicationId,
  updatingStatus,
  onUpdateStatus,
}: OwnerApplicationListItemProps) {
  const { t } = useTranslation();
  const actions = getAvailableActions(application.status, isJobCompleted);
  const isUpdatingThisApplication = isUpdating && updatingApplicationId === application.id;

  return (
    <li className="px-3 py-2.5">
      <div className="grid gap-2">
        <div className="min-w-0">
          <p className="truncate text-sm font-bold text-heading">{getApplicantName(application)}</p>
          <p className="text-xs font-semibold text-body">
            {t(`application.status.${application.status}`)}
          </p>
        </div>

        {actions.length > 0 ? (
          <div className="flex flex-wrap gap-2">
            {actions.map((status) => {
              const isThisActionUpdating = isUpdatingThisApplication && updatingStatus === status;

              return (
                <button
                  key={status}
                  className={getActionClass(status)}
                  type="button"
                  disabled={isUpdating}
                  onClick={() => onUpdateStatus(application.id, status)}
                >
                  {isThisActionUpdating
                    ? t('posted_jobs.applications.actions.updating')
                    : t(`posted_jobs.applications.actions.${status}`)}
                </button>
              );
            })}
          </div>
        ) : null}
      </div>
    </li>
  );
}

export default OwnerApplicationListItem;
