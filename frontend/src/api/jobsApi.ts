import type { PageResponse } from '../types/common';
import type {
  CompleteJobRequest,
  CreateJobRequest,
  Job,
  JobDetails,
  JobSearchParams,
  OwnerJob,
} from '../types/job';
import httpClient from './httpClient';

export async function getJobs(params?: JobSearchParams): Promise<PageResponse<Job>> {
  const response = await httpClient.get<PageResponse<Job>>('/jobs', { params });
  return response.data;
}

export async function getMyJobs(): Promise<OwnerJob[]> {
  const response = await httpClient.get<OwnerJob[]>('/jobs/mine');
  return response.data;
}

export async function createJob(request: CreateJobRequest): Promise<Job> {
  const response = await httpClient.post<Job>('/jobs', request);
  return response.data;
}

export async function getJobById(id: string): Promise<JobDetails> {
  const response = await httpClient.get<JobDetails>(`/jobs/${id}`);
  return response.data;
}

export async function completeJob(jobId: string, applicationId: string): Promise<JobDetails> {
  const request: CompleteJobRequest = { applicationId };
  const response = await httpClient.put<JobDetails>(`/jobs/${jobId}/completion`, request);
  return response.data;
}
