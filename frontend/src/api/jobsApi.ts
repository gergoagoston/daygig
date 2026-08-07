import type { PageResponse } from '../types/common';
import type { Job, JobCreatePayload, JobDetails, JobsQueryParams, OwnerJob } from '../types/job';
import httpClient from './httpClient';

export async function getJobs(params?: JobsQueryParams): Promise<PageResponse<Job>> {
  const response = await httpClient.get<PageResponse<Job>>('/jobs', { params });
  return response.data;
}

export async function getMyJobs(): Promise<OwnerJob[]> {
  const response = await httpClient.get<OwnerJob[]>('/jobs/mine');
  return response.data;
}

export async function createJob(payload: JobCreatePayload): Promise<Job> {
  const response = await httpClient.post<Job>('/jobs', payload);
  return response.data;
}

export async function getJobById(id: string): Promise<JobDetails> {
  const response = await httpClient.get<JobDetails>(`/jobs/${id}`);
  return response.data;
}

export async function completeJob(jobId: string, applicationId: string): Promise<JobDetails> {
  const response = await httpClient.put<JobDetails>(`/jobs/${jobId}/completion`, {
    applicationId,
  });
  return response.data;
}
