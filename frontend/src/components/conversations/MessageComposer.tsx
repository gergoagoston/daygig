import { type KeyboardEvent, type SubmitEvent, useState } from 'react';
import { useTranslation } from 'react-i18next';

type MessageComposerProps = {
  isConnected: boolean;
  onSendMessage: (content: string) => boolean;
};

function MessageComposer({ isConnected, onSendMessage }: MessageComposerProps) {
  const { t } = useTranslation();
  const [content, setContent] = useState('');

  const submitMessage = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    const normalizedContent = content.trim();
    if (!normalizedContent || normalizedContent.length > 200) {
      return;
    }

    if (onSendMessage(normalizedContent)) {
      setContent('');
    }
  };

  const submitOnEnter = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      event.currentTarget.form?.requestSubmit();
    }
  };

  return (
    <form className="grid gap-2 border-t border-border bg-surface p-3" onSubmit={submitMessage}>
      <textarea
        aria-label={t('conversations.composer.label')}
        className="min-h-20 w-full resize-none rounded-lg border border-border bg-surface px-3 py-2 text-sm text-heading outline-none focus:border-primary focus:ring-2 focus:ring-primary-soft disabled:opacity-65"
        disabled={!isConnected}
        maxLength={200}
        placeholder={t('conversations.composer.placeholder')}
        rows={2}
        value={content}
        onChange={(event) => setContent(event.target.value)}
        onKeyDown={submitOnEnter}
      />
      <div className="flex items-center justify-between gap-3">
        <span className="text-xs text-body">
          {isConnected
            ? t('conversations.composer.character_count', { count: content.length })
            : t('conversations.composer.connecting')}
        </span>
        <button
          className="rounded-lg bg-action px-4 py-2 text-sm font-bold text-action-text transition focus:outline-none focus:ring-2 focus:ring-primary-soft disabled:opacity-65"
          disabled={!isConnected || !content.trim()}
          type="submit"
        >
          {t('conversations.composer.send')}
        </button>
      </div>
    </form>
  );
}

export default MessageComposer;
