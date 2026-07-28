import { createContext } from 'react';

import type { UserOutDto, UserRole } from '../dtos/user';

export type CurrentUserUpdate = UserOutDto | ((currentUser: UserOutDto) => UserOutDto);

export type AuthContextValue = {
  user: UserOutDto | null;
  role: UserRole | null;
  isAuthenticated: boolean;
  isInitializing: boolean;
  login: (user: UserOutDto) => void;
  updateUser: (update: CurrentUserUpdate) => void;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export default AuthContext;
