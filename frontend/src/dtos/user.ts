import type { RatingSummary } from '../types/rating';

export type UserRole = 'ROLE_USER' | 'ROLE_ADMIN';

export type UserOutDto = {
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

export type UserProfileOutDto = {
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

export type BioUpdateInDto = {
  bio: string | null;
};
