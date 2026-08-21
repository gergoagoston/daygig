import type {
  CreateRatingRequest,
  Rating,
  RatingContext,
  RatingEligibility,
} from '../types/rating';
import httpClient from './httpClient';

export async function getRatingEligibility(jobId: string): Promise<RatingEligibility> {
  const response = await httpClient.get<RatingEligibility>(`/jobs/${jobId}/ratings/me`);
  return response.data;
}

export async function createRating(jobId: string, request: CreateRatingRequest): Promise<Rating> {
  const response = await httpClient.post<Rating>(`/jobs/${jobId}/ratings`, request);
  return response.data;
}

export async function getReceivedRatings(
  userId: string,
  context: RatingContext
): Promise<Rating[]> {
  const response = await httpClient.get<Rating[]>(`/users/${userId}/ratings`, {
    params: { context },
  });
  return response.data;
}
