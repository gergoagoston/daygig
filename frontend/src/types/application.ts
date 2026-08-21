import type { Currency, JobCategory, JobStatus } from './job';
import type { UserSummary } from './user';

export type ApplicationStatus = 'ACTIVE' | 'REJECTED' | 'WITHDRAWN';
export type ApplicationStatusUpdate = Exclude<ApplicationStatus, 'ACTIVE'>;

export type ApplicationJobSummary = {
  id: string;
  title: string;
  category: JobCategory;
  locationName: string;
  hourlyRate: number;
  currency: Currency;
  status: JobStatus;
  completedApplicationId: string | null;
  completedAt: string | null;
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

export type UpdateApplicationStatusRequest = {
  status: ApplicationStatusUpdate;
};
