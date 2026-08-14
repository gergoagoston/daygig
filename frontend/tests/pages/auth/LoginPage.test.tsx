import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';

import { loginUser } from '../../../src/api/authApi';
import type { UserOutDto } from '../../../src/dtos/user';
import useAuth from '../../../src/hooks/useAuth';
import LoginPage from '../../../src/pages/auth/LoginPage';

vi.mock('../../../src/api/authApi', () => ({
  loginUser: vi.fn(),
}));

vi.mock('../../../src/hooks/useAuth', () => ({
  default: vi.fn(),
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

describe('LoginPage', () => {
  it('logs in and returns the user to the originally requested page', async () => {
    const user = userEvent.setup();
    const setAuthenticatedUser = vi.fn();
    vi.mocked(useAuth).mockReturnValue({
      user: null,
      role: null,
      isAuthenticated: false,
      isInitializing: false,
      login: setAuthenticatedUser,
      updateUser: vi.fn(),
      logout: vi.fn(),
    });
    vi.mocked(loginUser).mockResolvedValue({ data: currentUser } as Awaited<
      ReturnType<typeof loginUser>
    >);

    render(
      <MemoryRouter
        initialEntries={[
          {
            pathname: '/login',
            state: { from: { pathname: '/my-jobs', search: '?page=2' } },
          },
        ]}
      >
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/my-jobs" element={<p>protected destination</p>} />
        </Routes>
      </MemoryRouter>
    );

    await user.type(screen.getByLabelText(/auth\.login\.email/), '  worker@example.com  ');
    await user.type(screen.getByLabelText(/auth\.login\.password/), 'password123');
    await user.click(screen.getByRole('button', { name: 'auth.login.submit_button' }));

    await waitFor(() => {
      expect(loginUser).toHaveBeenCalledWith({
        email: 'worker@example.com',
        password: 'password123',
      });
    });
    expect(setAuthenticatedUser).toHaveBeenCalledWith(currentUser);
    expect(await screen.findByText('protected destination')).toBeInTheDocument();
  });
});
