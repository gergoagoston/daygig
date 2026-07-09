import type { ReactNode } from 'react';
import { useEffect, useState } from 'react';

import { getCurrentUser, logoutUser } from '../api/authApi';
import type { UserOutDto } from '../dtos/user';
import { subscribeToUnauthorized } from '../utils/authEvents';
import AuthContext from './AuthContext';

type AuthProviderProps = {
  children: ReactNode;
};

function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<UserOutDto | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);

  const login = (nextUser: UserOutDto) => {
    setUser(nextUser);
  };

  const logout = async () => {
    try {
      await logoutUser();
    } finally {
      setUser(null);
    }
  };

  useEffect(() => {
    async function initAuth() {
      try {
        const response = await getCurrentUser();
        setUser(response.data);
      } catch {
        setUser(null);
      } finally {
        setIsInitializing(false);
      }
    }

    initAuth();
  }, []);

  useEffect(() => {
    const unsubscribe = subscribeToUnauthorized(() => setUser(null));
    return () => unsubscribe();
  }, []);

  const value = {
    user,
    role: user?.role ?? null,
    isAuthenticated: Boolean(user),
    isInitializing,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
