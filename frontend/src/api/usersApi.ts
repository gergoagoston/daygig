import type { BioUpdateInDto, UserOutDto, UserProfileOutDto } from '../dtos/user';
import httpClient from './httpClient';

export async function getCurrentUser(): Promise<UserOutDto> {
  const response = await httpClient.get<UserOutDto>('/users/me');
  return response.data;
}

export async function updateCurrentUserBio(payload: BioUpdateInDto): Promise<UserOutDto> {
  const response = await httpClient.patch<UserOutDto>('/users/me', payload);
  return response.data;
}

export async function uploadCurrentUserAvatar(file: File): Promise<UserOutDto> {
  const formData = new FormData();
  formData.append('file', file);

  const response = await httpClient.put<UserOutDto>('/users/me/avatar', formData);
  return response.data;
}

export async function deleteCurrentUserAvatar(): Promise<void> {
  await httpClient.delete('/users/me/avatar');
}

export async function getUserProfile(userId: string): Promise<UserProfileOutDto> {
  const response = await httpClient.get<UserProfileOutDto>(`/users/${userId}/profile`);
  return response.data;
}
