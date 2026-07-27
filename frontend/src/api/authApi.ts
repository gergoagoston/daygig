import type { LoginUserDto, RegisterUserDto } from '../dtos/auth';
import type { UserOutDto } from '../dtos/user';
import httpClient from './httpClient';

export function loginUser(payload: LoginUserDto) {
  return httpClient.post<UserOutDto>('/auth/login', payload);
}

export function registerUser(payload: RegisterUserDto) {
  return httpClient.post<UserOutDto>('/auth/register', payload);
}

export function logoutUser() {
  return httpClient.post<void>('/auth/logout');
}

export function getCurrentUser() {
  return httpClient.get<UserOutDto>('/users/me');
}
