import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import JobApplyPanel from '../../../src/components/jobs/JobApplyPanel';
import { useConversations, useCreateConversation } from '../../../src/hooks/useConversations';
import useWithdrawApplication from '../../../src/hooks/useWithdrawApplication';

vi.mock('../../../src/hooks/useWithdrawApplication', () => ({
  default: vi.fn(),
}));

vi.mock('../../../src/hooks/useConversations', () => ({
  useConversations: vi.fn(() => ({ conversations: [], isError: false, isLoading: false })),
  useCreateConversation: vi.fn(),
}));

vi.mock('../../../src/components/jobs/OwnerApplicationsPanel', () => ({
  default: () => <p>owner applications</p>,
}));

vi.mock('../../../src/components/ratings/JobRatingPanel', () => ({
  default: ({ showReviewee }: { showReviewee?: boolean }) => (
    <>
      {showReviewee ? <p>Test Owner</p> : null}
      <button type="button">ratings.job.action</button>
    </>
  ),
}));

describe('JobApplyPanel', () => {
  beforeEach(() => {
    vi.mocked(useCreateConversation).mockReturnValue({
      isError: false,
      isPending: false,
      mutate: vi.fn(),
      reset: vi.fn(),
    } as unknown as ReturnType<typeof useCreateConversation>);
  });

  it('withdraws an active application after user confirmation', async () => {
    const user = userEvent.setup();
    const withdraw = vi.fn();
    vi.mocked(useWithdrawApplication).mockReturnValue({
      isWithdrawError: false,
      isWithdrawing: false,
      withdraw,
    });
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(
      <MemoryRouter>
        <JobApplyPanel
          applyMessage={null}
          applicationId="application-1"
          applicationStatus="ACTIVE"
          canRate={false}
          hasApplied
          isApplyDisabled={false}
          isApplying={false}
          isCheckingApplicationStatus={false}
          jobId="job-1"
          jobStatus="OPEN"
          completedApplicationId={null}
          isOwnJob={false}
          onApply={vi.fn()}
        />
      </MemoryRouter>
    );

    const chatButton = screen.getByRole('button', { name: 'conversations.actions.chat' });
    const withdrawButton = screen.getByRole('button', { name: 'applications.card.withdraw' });

    expect(withdrawButton.parentElement).toBe(chatButton.parentElement);
    expect(withdrawButton).toHaveClass('h-8', 'order-3');
    await user.click(withdrawButton);

    expect(window.confirm).toHaveBeenCalledWith('applications.card.withdraw_confirm');
    expect(withdraw).toHaveBeenCalledOnce();
  });

  it('keeps the existing chat available after the job is completed', () => {
    vi.mocked(useWithdrawApplication).mockReturnValue({
      isWithdrawError: false,
      isWithdrawing: false,
      withdraw: vi.fn(),
    });
    vi.mocked(useConversations).mockReturnValue({
      conversations: [
        {
          id: 'conversation-1',
          applicationId: 'application-1',
          jobId: 'job-1',
          jobTitle: 'Completed job',
          otherParticipant: {
            id: 'owner-1',
            firstName: 'Test',
            lastName: 'Owner',
          },
          lastMessage: 'Thanks!',
          lastMessageAt: '2026-08-21T10:00:00Z',
        },
      ],
      isError: false,
      isLoading: false,
    });

    render(
      <MemoryRouter>
        <JobApplyPanel
          applyMessage={null}
          applicationId="application-1"
          applicationStatus="ACTIVE"
          canRate
          hasApplied
          isApplyDisabled={false}
          isApplying={false}
          isCheckingApplicationStatus={false}
          jobId="job-1"
          jobStatus="COMPLETED"
          completedApplicationId="application-1"
          isOwnJob={false}
          onApply={vi.fn()}
        />
      </MemoryRouter>
    );

    expect(screen.getByRole('link', { name: 'conversations.actions.chat' })).toHaveAttribute(
      'href',
      '/conversations/conversation-1'
    );
    expect(screen.getByRole('button', { name: 'ratings.job.action' }).parentElement).toBe(
      screen.getByRole('link', { name: 'conversations.actions.chat' }).parentElement
    );
    expect(screen.getByText('Test Owner')).toBeInTheDocument();
  });

  it('lets the worker start a chat when no conversation exists yet', () => {
    vi.mocked(useWithdrawApplication).mockReturnValue({
      isWithdrawError: false,
      isWithdrawing: false,
      withdraw: vi.fn(),
    });
    vi.mocked(useConversations).mockReturnValue({
      conversations: [],
      isError: false,
      isLoading: false,
    });

    render(
      <MemoryRouter>
        <JobApplyPanel
          applyMessage={null}
          applicationId="application-1"
          applicationStatus="ACTIVE"
          canRate
          hasApplied
          isApplyDisabled={false}
          isApplying={false}
          isCheckingApplicationStatus={false}
          jobId="job-1"
          jobStatus="COMPLETED"
          completedApplicationId="application-1"
          isOwnJob={false}
          onApply={vi.fn()}
        />
      </MemoryRouter>
    );

    expect(screen.getByRole('button', { name: 'conversations.actions.chat' })).toBeInTheDocument();
    expect(screen.getByText('Test Owner')).toBeInTheDocument();
  });
});
