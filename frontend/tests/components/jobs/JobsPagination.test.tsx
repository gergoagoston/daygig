import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import JobsPagination from '../../../src/components/jobs/JobsPagination';

describe('JobsPagination', () => {
  it('navigates forward and changes the page size', async () => {
    const user = userEvent.setup();
    const onPageChange = vi.fn();
    const onPageSizeChange = vi.fn();

    render(
      <JobsPagination
        isFetching={false}
        page={0}
        size={12}
        totalPages={3}
        onPageChange={onPageChange}
        onPageSizeChange={onPageSizeChange}
      />
    );

    expect(screen.getByRole('button', { name: 'jobs.pagination.previous' })).toBeDisabled();

    await user.click(screen.getByRole('button', { name: 'jobs.pagination.next' }));
    expect(onPageChange).toHaveBeenCalledWith(1);

    await user.selectOptions(screen.getByRole('combobox'), '24');
    expect(onPageSizeChange).toHaveBeenCalledWith(24);
  });
});
