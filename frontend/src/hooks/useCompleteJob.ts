import { useMutation, useQueryClient } from '@tanstack/react-query';

import { completeJob } from '../api/jobsApi';
import { getJobApplicationsQueryKey } from './useJobApplications';
import { myApplicationsQueryKey } from './useMyApplications';

function useCompleteJob(jobId: string) {
  const queryClient = useQueryClient();
  const completionMutation = useMutation({
    mutationFn: (applicationId: string) => completeJob(jobId, applicationId),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['jobs', 'mine'] }),
        queryClient.invalidateQueries({ queryKey: ['jobs'] }),
        queryClient.invalidateQueries({ queryKey: ['job', jobId] }),
        queryClient.invalidateQueries({ queryKey: getJobApplicationsQueryKey(jobId) }),
        queryClient.invalidateQueries({ queryKey: myApplicationsQueryKey }),
      ]);
    },
  });

  return {
    complete: (applicationId: string) => completionMutation.mutate(applicationId),
    completingApplicationId: completionMutation.variables,
    isCompleteError: completionMutation.isError,
    isCompleting: completionMutation.isPending,
  };
}

export default useCompleteJob;
