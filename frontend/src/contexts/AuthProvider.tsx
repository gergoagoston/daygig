import type { ReactNode } from 'react';
import { useCallback, useMemo, useState } from 'react';

import httpClient from '../api/httpClient';
import AuthContext from './AuthContext';
import type { AuthContextValue } from './AuthContext';
import type { AuthUser } from '../utils/authToken';
import { decodeAuthToken } from '../utils/authToken';

type AuthProviderProps = {
  children: ReactNode;
};

function AuthProvider({ children }: AuthProviderProps) {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<AuthUser | null>(null);

  const login = useCallback((nextToken: string) => {
    const decodedUser = decodeAuthToken(nextToken);

    setToken(nextToken);
    setUser(decodedUser);
    httpClient.defaults.headers.common.Authorization = `Bearer ${nextToken}`;
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
    delete httpClient.defaults.headers.common.Authorization;
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      user,
      role: user?.role ?? null,
      isAuthenticated: Boolean(token && user),
      login,
      logout,
    }),
    [login, logout, token, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
