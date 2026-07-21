import type { Application } from '../types/application';
import type { PageResponse } from '../types/common';
import type { Job, JobDetails, JobsQueryParams } from '../types/job';
import httpClient from './httpClient';

export async function getJobs(params?: JobsQueryParams): Promise<PageResponse<Job>> {
  const response = await httpClient.get<PageResponse<Job>>('/jobs', { params });
  return response.data;
}

export async function getMyJobs(): Promise<Job[]> {
  const response = await httpClient.get<Job[]>('/jobs/mine');
  return response.data;
}

export async function getJobById(id: string): Promise<JobDetails> {
  const response = await httpClient.get<JobDetails>(`/jobs/${id}`);
  return response.data;
}

export async function applyToJob(jobId: string): Promise<Application> {
  const response = await httpClient.post<Application>(`/jobs/${jobId}/applications`);
  return response.data;
}
