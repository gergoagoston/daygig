import type { Currency, JobCategory, JobStatus } from './job';
import type { UserSummary } from './user';

export type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'COMPLETED' | 'REJECTED' | 'CANCELLED';

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

export type OwnerApplication = {
  id: string;
  status: ApplicationStatus;
  appliedAt: string;
  applicant: UserSummary;
};

export type ApplicationStatusUpdatePayload = {
  status: ApplicationStatus;
};
