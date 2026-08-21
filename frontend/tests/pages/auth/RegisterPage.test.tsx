import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';

import { registerUser } from '../../../src/api/authApi';
import RegisterPage from '../../../src/pages/auth/RegisterPage';

vi.mock('../../../src/api/authApi', () => ({
  registerUser: vi.fn(),
}));

describe('RegisterPage', () => {
  it('submits valid registration data and navigates to login', async () => {
    const user = userEvent.setup();
    vi.mocked(registerUser).mockResolvedValue({} as Awaited<ReturnType<typeof registerUser>>);

    render(
      <MemoryRouter initialEntries={['/register']}>
        <Routes>
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/login" element={<p>login destination</p>} />
        </Routes>
      </MemoryRouter>
    );

    await user.type(screen.getByLabelText(/auth\.register\.first_name/), '  Test  ');
    await user.type(screen.getByLabelText(/auth\.register\.last_name/), '  User  ');
    await user.type(screen.getByLabelText(/auth\.register\.email/), '  test@example.com  ');
    await user.type(screen.getByLabelText(/^auth\.register\.password/), 'password123');
    await user.type(screen.getByLabelText(/auth\.register\.confirm_password/), 'password123');
    await user.type(screen.getByLabelText(/auth\.register\.phone_number/), ' 0712345678 ');
    await user.click(screen.getByRole('checkbox'));
    await user.click(screen.getByRole('button', { name: 'auth.register.submit_button' }));

    await waitFor(() => {
      expect(registerUser).toHaveBeenCalledWith({
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        password: 'password123',
        phoneNumber: '0712345678',
        termsAccepted: true,
      });
    });
    expect(await screen.findByText('login destination')).toBeInTheDocument();
  });
});
