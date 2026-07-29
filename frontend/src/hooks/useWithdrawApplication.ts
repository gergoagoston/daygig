import { useMutation, useQueryClient } from '@tanstack/react-query';

import { updateApplicationStatus } from '../api/applicationsApi';
import { myApplicationsQueryKey } from './useMyApplications';

function useWithdrawApplication(applicationId: string, jobId: string) {
  const queryClient = useQueryClient();
  const withdrawalMutation = useMutation({
    mutationFn: () => updateApplicationStatus(applicationId, 'WITHDRAWN'),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: myApplicationsQueryKey }),
        queryClient.invalidateQueries({ queryKey: ['job', jobId] }),
      ]);
    },
  });

  return {
    isWithdrawError: withdrawalMutation.isError,
    isWithdrawing: withdrawalMutation.isPending,
    withdraw: () => withdrawalMutation.mutate(),
  };
}

export default useWithdrawApplication;
