import type { LoginRequest, RegisterRequest } from '../types/auth';
import type { CurrentUser } from '../types/user';
import httpClient from './httpClient';

export function loginUser(request: LoginRequest) {
  return httpClient.post<CurrentUser>('/auth/login', request);
}

export function registerUser(request: RegisterRequest) {
  return httpClient.post<void>('/auth/register', request);
}

export function logoutUser() {
  return httpClient.post<void>('/auth/logout');
}
