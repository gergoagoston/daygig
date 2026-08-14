import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { getCurrentUser } from '../../src/api/usersApi';
import AuthProvider from '../../src/contexts/AuthProvider';
import type { UserOutDto } from '../../src/dtos/user';
import useAuth from '../../src/hooks/useAuth';
import { notifyUnauthorized } from '../../src/utils/authEvents';

vi.mock('../../src/api/authApi', () => ({
  logoutUser: vi.fn(),
}));

vi.mock('../../src/api/usersApi', () => ({
  getCurrentUser: vi.fn(),
}));

const currentUser: UserOutDto = {
  id: 'user-1',
  email: 'worker@example.com',
  role: 'ROLE_USER',
  firstName: 'Test',
  lastName: 'Worker',
  phoneNumber: null,
  bio: null,
  avatarUrl: null,
  createdAt: '2026-08-13T10:00:00Z',
};

function AuthStateProbe() {
  const { isAuthenticated, isInitializing, user } = useAuth();
  return (
    <p>
      {isInitializing
        ? 'initializing'
        : isAuthenticated
          ? `authenticated:${user?.id}`
          : 'anonymous'}
    </p>
  );
}

describe('AuthProvider', () => {
  it('restores the current user and clears it after an unauthorized event', async () => {
    vi.mocked(getCurrentUser).mockResolvedValue(currentUser);

    render(
      <AuthProvider>
        <AuthStateProbe />
      </AuthProvider>
    );

    expect(await screen.findByText('authenticated:user-1')).toBeInTheDocument();
    notifyUnauthorized();
    expect(await screen.findByText('anonymous')).toBeInTheDocument();
  });
});
