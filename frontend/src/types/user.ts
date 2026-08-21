import type { RatingSummary } from './rating';

export type UserRole = 'ROLE_USER' | 'ROLE_ADMIN';

export type UserSummary = {
  id: string;
  firstName: string;
  lastName: string;
};

export type CurrentUser = {
  id: string;
  email: string;
  role: UserRole;
  firstName: string;
  lastName: string;
  phoneNumber: string | null;
  bio: string | null;
  avatarUrl: string | null;
  createdAt: string;
};

export type UserProfile = {
  id: string;
  firstName: string;
  lastName: string;
  bio: string | null;
  avatarUrl: string | null;
  createdAt: string;
  workerRating: RatingSummary;
  jobOwnerRating: RatingSummary;
  completedJobsAsWorker: number;
  completedJobsAsOwner: number;
};

export type UpdateBioRequest = {
  bio: string | null;
};
