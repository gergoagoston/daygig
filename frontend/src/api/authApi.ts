import httpClient from './httpClient';
import type { AuthResponseDto, RegisterUserDto } from '../dtos/auth';

export function registerUser(payload: RegisterUserDto) {
  return httpClient.post<AuthResponseDto>('/auth/register', payload);
}
