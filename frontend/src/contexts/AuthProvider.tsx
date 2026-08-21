import type { ReactNode } from 'react';
import { useEffect, useState } from 'react';

import { logoutUser } from '../api/authApi';
import { getCurrentUser } from '../api/usersApi';
import type { CurrentUser } from '../types/user';
import { subscribeToUnauthorized } from '../utils/authEvents';
import AuthContext, { type CurrentUserUpdate } from './AuthContext';

type AuthProviderProps = {
  children: ReactNode;
};

function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);

  const login = (nextUser: CurrentUser) => {
    setUser(nextUser);
  };

  const updateUser = (update: CurrentUserUpdate) => {
    setUser((currentUser) => {
      if (!currentUser) {
        return null;
      }

      return typeof update === 'function' ? update(currentUser) : update;
    });
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
        const currentUser = await getCurrentUser();
        setUser(currentUser);
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
    updateUser,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
