import type { CurrentUser, UpdateBioRequest, UserProfile } from '../types/user';
import httpClient from './httpClient';

export async function getCurrentUser(): Promise<CurrentUser> {
  const response = await httpClient.get<CurrentUser>('/users/me');
  return response.data;
}

export async function updateCurrentUserBio(request: UpdateBioRequest): Promise<CurrentUser> {
  const response = await httpClient.patch<CurrentUser>('/users/me', request);
  return response.data;
}

export async function uploadCurrentUserAvatar(file: File): Promise<CurrentUser> {
  const formData = new FormData();
  formData.append('file', file);

  const response = await httpClient.put<CurrentUser>('/users/me/avatar', formData);
  return response.data;
}

export async function deleteCurrentUserAvatar(): Promise<void> {
  await httpClient.delete('/users/me/avatar');
}

export async function getUserProfile(userId: string): Promise<UserProfile> {
  const response = await httpClient.get<UserProfile>(`/users/${userId}/profile`);
  return response.data;
}
