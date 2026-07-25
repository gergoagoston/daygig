import { useQuery } from '@tanstack/react-query';

import { getJobApplications } from '../api/applicationsApi';

export function getJobApplicationsQueryKey(jobId: string) {
  return ['jobs', jobId, 'applications'] as const;
}

function useJobApplications(jobId: string) {
  const applicationsQuery = useQuery({
    queryKey: getJobApplicationsQueryKey(jobId),
    queryFn: () => getJobApplications(jobId),
    enabled: Boolean(jobId),
  });

  return {
    applications: applicationsQuery.data ?? [],
    isError: applicationsQuery.isError,
    isLoading: applicationsQuery.isLoading,
  };
}

export default useJobApplications;
