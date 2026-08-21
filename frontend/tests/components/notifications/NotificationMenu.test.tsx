import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';

import NotificationMenu from '../../../src/components/notifications/NotificationMenu';

describe('NotificationMenu', () => {
  it('marks a chat notification as read and navigates to its conversation', async () => {
    const user = userEvent.setup();
    const markAsRead = vi.fn().mockResolvedValue(undefined);

    render(
      <MemoryRouter initialEntries={['/jobs']}>
        <Routes>
          <Route
            path="/jobs"
            element={
              <NotificationMenu
                notifications={[
                  {
                    id: 'notification-1',
                    type: 'message.created',
                    jobId: 'job-1',
                    conversationId: 'conversation-1',
                    createdAt: '2026-08-13T10:00:00Z',
                  },
                ]}
                unreadCount={1}
                isLoading={false}
                markAsRead={markAsRead}
                markAllAsRead={vi.fn()}
              />
            }
          />
          <Route path="/conversations/:id" element={<p>conversation destination</p>} />
        </Routes>
      </MemoryRouter>
    );

    await user.click(screen.getByRole('button', { name: 'notifications.open' }));
    await user.click(
      screen.getByRole('button', { name: /notifications\.types\.message\.created/ })
    );

    expect(markAsRead).toHaveBeenCalledWith('notification-1');
    expect(await screen.findByText('conversation destination')).toBeInTheDocument();
  });
});
