import { useQuery } from '@tanstack/react-query';

import { getMyApplications } from '../api/applicationsApi';

export const myApplicationsQueryKey = ['applications', 'mine'] as const;

function useMyApplications(enabled = true) {
  const applicationsQuery = useQuery({
    queryKey: myApplicationsQueryKey,
    queryFn: getMyApplications,
    enabled,
  });

  return {
    applications: applicationsQuery.data ?? [],
    isError: applicationsQuery.isError,
    isLoading: applicationsQuery.isLoading,
  };
}

export default useMyApplications;
