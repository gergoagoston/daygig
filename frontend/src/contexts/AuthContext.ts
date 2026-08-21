import { createContext } from 'react';

import type { CurrentUser, UserRole } from '../types/user';

export type CurrentUserUpdate = CurrentUser | ((currentUser: CurrentUser) => CurrentUser);

export type AuthContextValue = {
  user: CurrentUser | null;
  role: UserRole | null;
  isAuthenticated: boolean;
  isInitializing: boolean;
  login: (user: CurrentUser) => void;
  updateUser: (update: CurrentUserUpdate) => void;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export default AuthContext;
