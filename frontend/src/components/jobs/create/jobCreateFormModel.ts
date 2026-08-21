import axios from 'axios';
import { z } from 'zod';

import { jobCategories, type CreateJobRequest, type JobCategory } from '../../../types/job';

type Translate = (key: string) => string;

export type JobCreateFormValues = {
  title: string;
  description: string;
  category: string;
  hourlyRate: string;
  locationName: string;
  latitude: string;
  longitude: string;
  form?: string;
};

export const defaultJobCreateValues: JobCreateFormValues = {
  title: '',
  description: '',
  category: '',
  hourlyRate: '',
  locationName: '',
  latitude: '',
  longitude: '',
};

export function createJobSchema(t: Translate) {
  return z.object({
    title: z
      .string()
      .trim()
      .min(1, t('jobs.create.errors.title_required'))
      .max(50, t('jobs.create.errors.title_too_long')),
    description: z
      .string()
      .trim()
      .min(1, t('jobs.create.errors.description_required'))
      .max(1000, t('jobs.create.errors.description_too_long')),
    category: z
      .string()
      .min(1, t('jobs.create.errors.category_required'))
      .refine((value) => jobCategories.includes(value as JobCategory), {
        message: t('jobs.create.errors.category_required'),
      }),
    hourlyRate: z
      .string()
      .trim()
      .min(1, t('jobs.create.errors.hourly_rate_required'))
      .refine((value) => Number.isFinite(Number(value)) && Number(value) > 0, {
        message: t('jobs.create.errors.hourly_rate_positive'),
      }),
    locationName: z.string().trim().min(1, t('jobs.create.errors.location_name_required')),
    latitude: z
      .string()
      .min(1, t('jobs.create.errors.location_required'))
      .refine((value) => {
        const latitude = Number(value);
        return Number.isFinite(latitude) && latitude >= -90 && latitude <= 90;
      }, t('jobs.create.errors.location_invalid')),
    longitude: z
      .string()
      .min(1, t('jobs.create.errors.location_required'))
      .refine((value) => {
        const longitude = Number(value);
        return Number.isFinite(longitude) && longitude >= -180 && longitude <= 180;
      }, t('jobs.create.errors.location_invalid')),
  });
}

export function toCreateJobRequest(values: JobCreateFormValues): CreateJobRequest {
  return {
    title: values.title.trim(),
    description: values.description.trim(),
    category: values.category as JobCategory,
    hourlyRate: Number(values.hourlyRate),
    locationName: values.locationName.trim(),
    latitude: Number(values.latitude),
    longitude: Number(values.longitude),
  };
}

export function getBackendValidationDetails(error: unknown): Record<string, string> | null {
  if (!axios.isAxiosError(error)) {
    return null;
  }

  const details = error.response?.data?.details;

  if (!details || typeof details !== 'object' || Array.isArray(details)) {
    return null;
  }

  return details as Record<string, string>;
}
