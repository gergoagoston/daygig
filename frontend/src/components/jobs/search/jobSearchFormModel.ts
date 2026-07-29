import { z } from 'zod';

import { jobCategories, type JobCategory, type JobsQueryParams } from '../../../types/job';

type Translate = (key: string) => string;

export const jobSortOptions = [
  'createdAt,desc',
  'createdAt,asc',
  'hourlyRate,desc',
  'hourlyRate,asc',
] as const;

export type JobSortOption = (typeof jobSortOptions)[number];

export type JobSearchFormValues = {
  q: string;
  category: string;
  minHourlyRate: string;
  maxHourlyRate: string;
  sort: JobSortOption;
  latitude: string;
  longitude: string;
  maxDistanceKm: string;
};

export type AppliedJobsQueryParams = JobsQueryParams & {
  page: number;
  size: number;
};

export const defaultJobSearchValues: JobSearchFormValues = {
  q: '',
  category: '',
  minHourlyRate: '',
  maxHourlyRate: '',
  sort: 'createdAt,desc',
  latitude: '',
  longitude: '',
  maxDistanceKm: '',
};

function optionalHourlyRate(t: Translate) {
  return z
    .string()
    .trim()
    .refine(
      (value) => value === '' || (Number.isFinite(Number(value)) && Number(value) >= 0),
      t('jobs.search.errors.hourly_rate_invalid')
    );
}

function optionalCoordinate(minimum: number, maximum: number, message: string) {
  return z
    .string()
    .trim()
    .refine((value) => {
      if (value === '') {
        return true;
      }

      const coordinate = Number(value);
      return Number.isFinite(coordinate) && coordinate >= minimum && coordinate <= maximum;
    }, message);
}

export function createJobSearchSchema(t: Translate) {
  return z
    .object({
      q: z.string().trim().max(100, t('jobs.search.errors.query_too_long')),
      category: z
        .string()
        .refine(
          (value) => value === '' || jobCategories.includes(value as JobCategory),
          t('jobs.search.errors.category_invalid')
        ),
      minHourlyRate: optionalHourlyRate(t),
      maxHourlyRate: optionalHourlyRate(t),
      sort: z.enum(jobSortOptions),
      latitude: optionalCoordinate(-90, 90, t('jobs.search.errors.location_invalid')),
      longitude: optionalCoordinate(-180, 180, t('jobs.search.errors.location_invalid')),
      maxDistanceKm: z
        .string()
        .trim()
        .refine((value) => {
          if (value === '') {
            return true;
          }

          const distance = Number(value);
          return Number.isFinite(distance) && distance >= 5 && distance <= 200;
        }, t('jobs.search.errors.distance_invalid')),
    })
    .superRefine((values, context) => {
      if (
        values.minHourlyRate !== '' &&
        values.maxHourlyRate !== '' &&
        Number(values.minHourlyRate) > Number(values.maxHourlyRate)
      ) {
        context.addIssue({
          code: 'custom',
          message: t('jobs.search.errors.hourly_rate_range'),
          path: ['maxHourlyRate'],
        });
      }

      const locationValues = [values.latitude, values.longitude, values.maxDistanceKm];
      const providedLocationValues = locationValues.filter((value) => value !== '').length;

      if (providedLocationValues > 0 && providedLocationValues < locationValues.length) {
        context.addIssue({
          code: 'custom',
          message: t('jobs.search.errors.location_incomplete'),
          path: ['maxDistanceKm'],
        });
      }
    });
}

export function toJobsQueryParams(
  values: JobSearchFormValues,
  page: number,
  size: number
): AppliedJobsQueryParams {
  const query = values.q.trim();
  const hasLocation =
    values.latitude !== '' && values.longitude !== '' && values.maxDistanceKm !== '';

  return {
    page,
    size,
    sort: values.sort,
    ...(query ? { q: query } : {}),
    ...(values.category ? { category: values.category as JobCategory } : {}),
    ...(values.minHourlyRate !== '' ? { minHourlyRate: Number(values.minHourlyRate) } : {}),
    ...(values.maxHourlyRate !== '' ? { maxHourlyRate: Number(values.maxHourlyRate) } : {}),
    ...(hasLocation
      ? {
          latitude: Number(values.latitude),
          longitude: Number(values.longitude),
          maxDistanceKm: Number(values.maxDistanceKm),
        }
      : {}),
  };
}

export function hasJobFilters(params: JobsQueryParams) {
  return Boolean(
    params.q ||
    params.category ||
    params.minHourlyRate !== undefined ||
    params.maxHourlyRate !== undefined ||
    params.latitude !== undefined
  );
}
