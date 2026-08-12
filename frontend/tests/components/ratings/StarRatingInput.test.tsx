import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import StarRatingInput from '../../../src/components/ratings/StarRatingInput';

describe('StarRatingInput', () => {
  it('shows the selected rating and reports a new selection', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();

    render(<StarRatingInput value={2} onChange={onChange} />);

    const stars = screen.getAllByRole('radio');
    expect(stars).toHaveLength(5);
    expect(stars[1]).toBeChecked();

    const fourthStar = stars.at(3);
    if (!fourthStar) {
      throw new Error('Expected the fourth rating option to be rendered.');
    }

    await user.click(fourthStar);
    expect(onChange).toHaveBeenCalledWith(4);
  });
});
