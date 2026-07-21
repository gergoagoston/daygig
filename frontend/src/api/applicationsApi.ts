import type { Application } from '../types/application';
import httpClient from './httpClient';

export async function getMyApplications(): Promise<Application[]> {
  const response = await httpClient.get<Application[]>('/applications/mine');
  return response.data;
}
