import { useTranslation } from 'react-i18next';

import type { ApplyMessage } from '../../hooks/useApplyToJob';
import Button from '../ui/Button';

type JobApplyPanelProps = {
  applyMessage: ApplyMessage | null;
  hasApplied: boolean;
  isApplyDisabled: boolean;
  isApplying: boolean;
  isCheckingApplicationStatus: boolean;
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

function JobApplyPanel({
  applyMessage,
  hasApplied,
  isApplyDisabled,
  isApplying,
  isCheckingApplicationStatus,
  isOwnJob,
  onApply,
}: JobApplyPanelProps) {
  const { t } = useTranslation();

  return (
    <div className="grid w-full gap-4">
      {applyMessage ? (
        <p
          className={`rounded-lg border p-4 font-semibold ${getMessageClassName(applyMessage.type)}`}
        >
          {applyMessage.text}
        </p>
      ) : null}

      {isOwnJob ? (
        <p className="rounded-lg border border-border bg-surface p-4 font-semibold text-body">
          {t('jobs.details.apply.own_job')}
        </p>
      ) : isCheckingApplicationStatus ? null : hasApplied && !applyMessage ? (
        <p className="rounded-lg border border-primary bg-primary-soft p-4 font-semibold text-primary">
          {t('jobs.details.apply.already_applied')}
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
    </div>
  );
}

export default JobApplyPanel;
