import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';

import ProtectedRoute from '../../../src/components/common/ProtectedRoute';
import useAuth from '../../../src/hooks/useAuth';

vi.mock('../../../src/hooks/useAuth', () => ({
  default: vi.fn(),
}));

function LoginLocationProbe() {
  const location = useLocation();
  const from = location.state?.from as { pathname?: string; search?: string } | undefined;
  return <p>{`${from?.pathname ?? ''}${from?.search ?? ''}`}</p>;
}

describe('ProtectedRoute', () => {
  it('redirects an anonymous user to login and preserves the requested location', async () => {
    vi.mocked(useAuth).mockReturnValue({
      user: null,
      role: null,
      isAuthenticated: false,
      isInitializing: false,
      login: vi.fn(),
      updateUser: vi.fn(),
      logout: vi.fn(),
    });

    render(
      <MemoryRouter initialEntries={['/my-jobs?page=2']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/my-jobs" element={<p>private page</p>} />
          </Route>
          <Route path="/login" element={<LoginLocationProbe />} />
        </Routes>
      </MemoryRouter>
    );

    expect(await screen.findByText('/my-jobs?page=2')).toBeInTheDocument();
    expect(screen.queryByText('private page')).not.toBeInTheDocument();
  });
});
