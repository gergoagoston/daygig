import type { Job, JobDetails, JobsQueryParams, PageResponse } from '../types/job';
import httpClient from './httpClient';

export async function getJobs(params?: JobsQueryParams): Promise<PageResponse<Job>> {
  const response = await httpClient.get<PageResponse<Job>>('/jobs', { params });
  return response.data;
}

export async function getJobById(id: string): Promise<JobDetails> {
  const response = await httpClient.get<JobDetails>(`/jobs/${id}`);
  return response.data;
}
