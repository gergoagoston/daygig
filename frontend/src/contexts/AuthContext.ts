import { createContext } from 'react';

import type { AuthUser } from '../utils/authToken';

export type AuthContextValue = {
  token: string | null;
  user: AuthUser | null;
  role: AuthUser['role'] | null;
  isAuthenticated: boolean;
  login: (token: string) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export default AuthContext;
