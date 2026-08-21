import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';

import OwnerApplicationsPanel from '../../../src/components/jobs/OwnerApplicationsPanel';
import useCompleteJob from '../../../src/hooks/useCompleteJob';
import { useConversations, useCreateConversation } from '../../../src/hooks/useConversations';
import useJobApplications from '../../../src/hooks/useJobApplications';
import useUpdateOwnerApplicationStatus from '../../../src/hooks/useUpdateOwnerApplicationStatus';

vi.mock('../../../src/hooks/useJobApplications', () => ({ default: vi.fn() }));
vi.mock('../../../src/hooks/useCompleteJob', () => ({ default: vi.fn() }));
vi.mock('../../../src/hooks/useUpdateOwnerApplicationStatus', () => ({ default: vi.fn() }));
vi.mock('../../../src/hooks/useConversations', () => ({
  useConversations: vi.fn(),
  useCreateConversation: vi.fn(),
}));
vi.mock('../../../src/components/ratings/JobRatingPanel', () => ({ default: () => null }));

describe('OwnerApplicationsPanel', () => {
  it('rejects the selected active application', async () => {
    const user = userEvent.setup();
    const updateStatus = vi.fn();
    vi.mocked(useJobApplications).mockReturnValue({
      applications: [
        {
          id: 'application-1',
          status: 'ACTIVE',
          appliedAt: '2026-08-13T10:00:00Z',
          applicant: { id: 'applicant-1', firstName: 'Test', lastName: 'Applicant' },
        },
      ],
      isError: false,
      isLoading: false,
    });
    vi.mocked(useConversations).mockReturnValue({
      conversations: [],
      isError: false,
      isLoading: false,
    });
    vi.mocked(useCreateConversation).mockReturnValue({
      isError: false,
      isPending: false,
      mutate: vi.fn(),
      reset: vi.fn(),
    } as unknown as ReturnType<typeof useCreateConversation>);
    vi.mocked(useUpdateOwnerApplicationStatus).mockReturnValue({
      isUpdateError: false,
      isUpdating: false,
      updateStatus,
      updatingApplicationId: undefined,
      updatingStatus: undefined,
    });
    vi.mocked(useCompleteJob).mockReturnValue({
      complete: vi.fn(),
      completingApplicationId: undefined,
      isCompleteError: false,
      isCompleting: false,
    });

    render(
      <MemoryRouter>
        <OwnerApplicationsPanel jobId="job-1" jobStatus="OPEN" completedApplicationId={null} />
      </MemoryRouter>
    );

    await user.click(
      screen.getByRole('button', { name: 'posted_jobs.applications.actions.REJECTED' })
    );

    expect(updateStatus).toHaveBeenCalledWith('application-1', 'REJECTED');
  });
});
