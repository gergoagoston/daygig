import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

import { applyToJob, getMyApplications } from '../api/applicationsApi';
import { isHttpConflictError } from '../api/httpClient';

export type ApplyMessage = {
  type: 'success' | 'warning' | 'error';
  text: string;
};

type ApplyState = {
  jobId: string;
  hasApplied: boolean;
  message: ApplyMessage | null;
};

type UseApplyToJobOptions = {
  jobId: string;
  enabled: boolean;
};

function useApplyToJob({ jobId, enabled }: UseApplyToJobOptions) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const canUseApplications = enabled && Boolean(jobId);
  const [applyState, setApplyState] = useState<ApplyState>({
    jobId: '',
    hasApplied: false,
    message: null,
  });

  const { data: myApplications = [], isLoading: isLoadingMyApplications } = useQuery({
    queryKey: ['applications', 'mine'],
    queryFn: getMyApplications,
    enabled: canUseApplications,
  });

  const applyMutation = useMutation({
    mutationFn: () => applyToJob(jobId),
    onMutate: () => {
      setApplyState({
        jobId,
        hasApplied: false,
        message: null,
      });
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['applications', 'mine'] });
      setApplyState({
        jobId,
        hasApplied: true,
        message: {
          type: 'success',
          text: t('jobs.details.apply.success'),
        },
      });
    },
    onError: (error) => {
      const isDuplicate = isHttpConflictError(error);

      setApplyState({
        jobId,
        hasApplied: isDuplicate,
        message: {
          type: isDuplicate ? 'warning' : 'error',
          text: isDuplicate
            ? t('jobs.details.apply.duplicate')
            : t('jobs.details.apply.generic_error'),
        },
      });
    },
  });

  const applyMessage = canUseApplications && applyState.jobId === jobId ? applyState.message : null;
  const hasAppliedFromCurrentAction =
    canUseApplications && applyState.jobId === jobId && applyState.hasApplied;
  const hasAppliedFromServer =
    canUseApplications && myApplications.some((application) => application.job.id === jobId);
  const hasApplied = hasAppliedFromCurrentAction || hasAppliedFromServer;

  const apply = () => {
    if (!jobId) {
      return;
    }

    applyMutation.mutate();
  };

  return {
    apply,
    applyMessage,
    hasApplied,
    isApplying: applyMutation.isPending,
    isCheckingApplicationStatus: canUseApplications && isLoadingMyApplications,
  };
}

export default useApplyToJob;
