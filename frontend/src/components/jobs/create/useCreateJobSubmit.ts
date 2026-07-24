import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { UseFormSetError } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';

import { createJob } from '../../../api/jobsApi';
import {
  defaultJobCreateValues,
  getBackendValidationDetails,
  type JobCreateFormValues,
} from './jobCreateFormModel';

type UseCreateJobSubmitParams = {
  onCreated: () => void;
  setError: UseFormSetError<JobCreateFormValues>;
};

function useCreateJobSubmit({ onCreated, setError }: UseCreateJobSubmitParams) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createJob,
    onSuccess: (createdJob) => {
      void queryClient.invalidateQueries({ queryKey: ['jobs'] });
      void queryClient.invalidateQueries({ queryKey: ['jobs', 'mine'] });
      onCreated();

      if (createdJob.id) {
        navigate(`/jobs/${createdJob.id}`);
        return;
      }

      navigate('/my-jobs');
    },
    onError: (error) => {
      const validationDetails = getBackendValidationDetails(error);

      if (validationDetails) {
        Object.keys(validationDetails).forEach((fieldName) => {
          if (fieldName in defaultJobCreateValues) {
            setError(fieldName as keyof JobCreateFormValues, {
              message: t(`jobs.create.errors.backend.${fieldName}`),
              type: 'server',
            });
          }
        });
        return;
      }

      setError('form', {
        message: t('jobs.create.errors.generic'),
        type: 'server',
      });
    },
  });
}

export default useCreateJobSubmit;
