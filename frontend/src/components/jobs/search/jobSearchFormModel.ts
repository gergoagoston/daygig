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
};

export const defaultJobSearchValues: JobSearchFormValues = {
  q: '',
  category: '',
  minHourlyRate: '',
  maxHourlyRate: '',
  sort: 'createdAt,desc',
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
    });
}

export function toJobsQueryParams(
  values: JobSearchFormValues,
  page: number,
  size: number
): JobsQueryParams {
  const query = values.q.trim();

  return {
    page,
    size,
    sort: values.sort,
    ...(query ? { q: query } : {}),
    ...(values.category ? { category: values.category as JobCategory } : {}),
    ...(values.minHourlyRate !== '' ? { minHourlyRate: Number(values.minHourlyRate) } : {}),
    ...(values.maxHourlyRate !== '' ? { maxHourlyRate: Number(values.maxHourlyRate) } : {}),
  };
}

export function hasJobFilters(params: JobsQueryParams) {
  return Boolean(
    params.q ||
    params.category ||
    params.minHourlyRate !== undefined ||
    params.maxHourlyRate !== undefined
  );
}
