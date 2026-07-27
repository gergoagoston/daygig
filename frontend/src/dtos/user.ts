export type UserRole = 'ROLE_USER' | 'ROLE_ADMIN';

export type UserOutDto = {
  id: string;
  email: string;
  role: UserRole;
  firstName: string;
  lastName: string;
  phoneNumber: string | null;
  bio: string | null;
  avatarUrl: string | null;
  createdAt: string;
};
