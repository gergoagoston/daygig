import type { UserSummary } from './user';

export const jobCategories = [
  'CLEANING',
  'MOVING',
  'GARDENING',
  'DELIVERY',
  'BABYSITTING',
  'PET_CARE',
  'HANDYMAN',
  'EVENT_HELP',
  'OTHER',
] as const;

export type JobCategory = (typeof jobCategories)[number];

export type JobStatus = 'OPEN' | 'COMPLETED' | 'CANCELLED';

export type Currency = 'RON';

export type Job = {
  id: string;
  title: string;
  description: string;
  category: JobCategory;
  status: JobStatus;
  hourlyRate: number;
  currency: Currency;
  locationName: string;
  createdAt: string;
  completedApplicationId: string | null;
  completedAt: string | null;
};

export type OwnerJob = Job & {
  applicationCount: number;
};

export type CreateJobRequest = {
  title: string;
  description: string;
  category: JobCategory;
  hourlyRate: number;
  locationName: string;
  latitude: number;
  longitude: number;
};

export type JobDetails = Job & {
  status: JobStatus;
  approximateLatitude: number;
  approximateLongitude: number;
  locationRadiusMeters: number;
  owner: UserSummary;
};

export type JobSearchParams = {
  page?: number;
  size?: number;
  sort?: string | string[];
  q?: string;
  category?: JobCategory;
  minHourlyRate?: number;
  maxHourlyRate?: number;
  latitude?: number;
  longitude?: number;
  maxDistanceKm?: number;
};

export type CompleteJobRequest = {
  applicationId: string;
};
