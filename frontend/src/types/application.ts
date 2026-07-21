import type { Currency, JobCategory, JobStatus } from './job';

export type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED';

export type ApplicationJobSummary = {
  id: string;
  title: string;
  category: JobCategory;
  locationName: string;
  hourlyRate: number;
  currency: Currency;
  status: JobStatus;
};

export type Application = {
  id: string;
  status: ApplicationStatus;
  appliedAt: string;
  job: ApplicationJobSummary;
};
