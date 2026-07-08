import httpClient from './httpClient';
import type { AuthResponseDto, LoginUserDto, RegisterUserDto } from '../dtos/auth';

export function registerUser(payload: RegisterUserDto) {
  return httpClient.post<AuthResponseDto>('/auth/register', payload);
}

export function loginUser(payload: LoginUserDto) {
  return httpClient.post<AuthResponseDto>('/auth/login', payload);
}
