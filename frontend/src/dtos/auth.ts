import type { UserOutDto } from './user';

export type RegisterUserDto = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber?: string;
};

export type LoginUserDto = {
  email: string;
  password: string;
};

export type AuthResponseDto = {
  token: string;
  user: UserOutDto;
};
