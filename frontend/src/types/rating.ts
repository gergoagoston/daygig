export type RatingContext = 'WORKER' | 'JOB_OWNER';

export type RatingSummary = {
  average: number | null;
  count: number;
};

export type RatingParticipant = {
  id: string;
  firstName: string;
  lastName: string;
  avatarUrl: string | null;
};

export type Rating = {
  id: string;
  stars: number;
  comment: string | null;
  createdAt: string;
  context: RatingContext;
  reviewer: RatingParticipant;
  jobId: string;
  jobTitle: string;
};

export type RatingEligibility = {
  eligible: boolean;
  alreadyRated: boolean;
  reviewee: RatingParticipant | null;
  context: RatingContext | null;
};

export type CreateRatingRequest = {
  stars: number;
  comment: string | null;
};
