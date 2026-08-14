import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import MessageComposer from '../../../src/components/conversations/MessageComposer';

describe('MessageComposer', () => {
  it('trims, sends and clears a message', async () => {
    const user = userEvent.setup();
    const onSendMessage = vi.fn(() => true);

    render(<MessageComposer isConnected onSendMessage={onSendMessage} />);

    const textbox = screen.getByRole('textbox', { name: 'conversations.composer.label' });
    await user.type(textbox, '  Hello there  ');
    await user.click(screen.getByRole('button', { name: 'conversations.composer.send' }));

    expect(onSendMessage).toHaveBeenCalledWith('Hello there');
    expect(textbox).toHaveValue('');
  });
});
