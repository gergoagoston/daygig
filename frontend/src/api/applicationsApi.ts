import type {
  Application,
  ApplicationStatusUpdate,
  ApplicationStatusUpdatePayload,
  OwnerApplication,
} from '../types/application';
import httpClient from './httpClient';

export async function getMyApplications(): Promise<Application[]> {
  const response = await httpClient.get<Application[]>('/applications/mine');
  return response.data;
}

export async function applyToJob(jobId: string): Promise<Application> {
  const response = await httpClient.post<Application>(`/jobs/${jobId}/applications`);
  return response.data;
}

export async function getJobApplications(jobId: string): Promise<OwnerApplication[]> {
  const response = await httpClient.get<OwnerApplication[]>(`/jobs/${jobId}/applications`);
  return response.data;
}

export async function updateApplicationStatus(
  applicationId: string,
  status: ApplicationStatusUpdate
): Promise<void> {
  const payload: ApplicationStatusUpdatePayload = { status };
  await httpClient.patch(`/applications/${applicationId}`, payload);
}
