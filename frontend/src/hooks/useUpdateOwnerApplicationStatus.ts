import { useMutation, useQueryClient } from '@tanstack/react-query';

import { updateApplicationStatus } from '../api/applicationsApi';
import type { ApplicationStatusUpdate } from '../types/application';
import { getJobApplicationsQueryKey } from './useJobApplications';

type StatusUpdateVariables = {
  applicationId: string;
  status: ApplicationStatusUpdate;
};

function useUpdateOwnerApplicationStatus(jobId: string) {
  const queryClient = useQueryClient();

  const statusMutation = useMutation({
    mutationFn: ({ applicationId, status }: StatusUpdateVariables) =>
      updateApplicationStatus(applicationId, status),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['jobs', 'mine'] }),
        queryClient.invalidateQueries({ queryKey: ['job', jobId] }),
        queryClient.invalidateQueries({ queryKey: getJobApplicationsQueryKey(jobId) }),
        queryClient.invalidateQueries({ queryKey: ['applications'] }),
      ]);
    },
  });

  const updateStatus = (applicationId: string, status: ApplicationStatusUpdate) => {
    statusMutation.mutate({ applicationId, status });
  };

  return {
    isUpdateError: statusMutation.isError,
    isUpdating: statusMutation.isPending,
    updateStatus,
    updatingApplicationId: statusMutation.variables?.applicationId,
    updatingStatus: statusMutation.variables?.status,
  };
}

export default useUpdateOwnerApplicationStatus;
