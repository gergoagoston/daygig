import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import JobApplyPanel from '../../../src/components/jobs/JobApplyPanel';
import useWithdrawApplication from '../../../src/hooks/useWithdrawApplication';

vi.mock('../../../src/hooks/useWithdrawApplication', () => ({
  default: vi.fn(),
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
});
