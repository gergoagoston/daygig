import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';

import JobApplyPanel from '../../../src/components/jobs/JobApplyPanel';
import { useConversations } from '../../../src/hooks/useConversations';
import useWithdrawApplication from '../../../src/hooks/useWithdrawApplication';

vi.mock('../../../src/hooks/useWithdrawApplication', () => ({
  default: vi.fn(),
}));

vi.mock('../../../src/hooks/useConversations', () => ({
  useConversations: vi.fn(() => ({ conversations: [], isError: false, isLoading: false })),
}));

vi.mock('../../../src/components/jobs/OwnerApplicationsPanel', () => ({
  default: () => <p>owner applications</p>,
}));

describe('JobApplyPanel', () => {
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
      <JobApplyPanel
        applyMessage={null}
        applicationId="application-1"
        applicationStatus="ACTIVE"
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
    );

    await user.click(screen.getByRole('button', { name: 'applications.card.withdraw' }));

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
  });
});
