import { type SubmitEvent, useState } from 'react';
import { useTranslation } from 'react-i18next';

import Modal from '../ui/Modal';

type StartConversationModalProps = {
  isError: boolean;
  isOpen: boolean;
  isSubmitting: boolean;
  onClose: () => void;
  onSubmit: (content: string) => void;
};

function StartConversationModal({
  isError,
  isOpen,
  isSubmitting,
  onClose,
  onSubmit,
}: StartConversationModalProps) {
  const { t } = useTranslation();
  const [content, setContent] = useState('');
  const normalizedContent = content.trim();

  const submit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!normalizedContent || normalizedContent.length > 200 || isSubmitting) {
      return;
    }
    onSubmit(normalizedContent);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={t('conversations.start.title')}
      titleId="start-conversation-title"
    >
      <form className="grid gap-4" onSubmit={submit}>
        <label className="grid gap-2 font-bold text-heading" htmlFor="first-chat-message">
          {t('conversations.start.message_label')}
          <textarea
            className="min-h-32 resize-y rounded-lg border border-border bg-surface px-3 py-2 font-normal text-heading outline-none focus:border-primary focus:ring-2 focus:ring-primary-soft disabled:opacity-65"
            disabled={isSubmitting}
            id="first-chat-message"
            maxLength={200}
            placeholder={t('conversations.start.message_placeholder')}
            required
            value={content}
            onChange={(event) => setContent(event.target.value)}
          />
        </label>

        <div className="flex items-center justify-between gap-3">
          <span className="text-xs text-body">{content.length}/200</span>
          {isError ? (
            <span className="text-sm font-semibold text-danger">
              {t('conversations.start.error')}
            </span>
          ) : null}
        </div>

        <div className="flex flex-wrap justify-end gap-2">
          <button
            className="rounded-lg border border-border px-4 py-2 font-bold text-heading"
            disabled={isSubmitting}
            type="button"
            onClick={onClose}
          >
            {t('conversations.start.cancel')}
          </button>
          <button
            className="rounded-lg bg-action px-4 py-2 font-bold text-action-text disabled:opacity-65"
            disabled={!normalizedContent || isSubmitting}
            type="submit"
          >
            {isSubmitting ? t('conversations.start.submitting') : t('conversations.start.submit')}
          </button>
        </div>
      </form>
    </Modal>
  );
}

export default StartConversationModal;
