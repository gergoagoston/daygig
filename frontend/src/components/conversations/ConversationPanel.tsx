import { useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import type { ChatMessage, Conversation } from '../../types/conversation';
import type { UserSummary } from '../../types/user';
import UserAvatar from '../profile/UserAvatar';
import ErrorState from '../ui/ErrorState';
import LoadingState from '../ui/LoadingState';
import MessageComposer from './MessageComposer';

type ConversationPanelProps = {
  conversation?: Conversation;
  currentUserId?: string;
  isConnected: boolean;
  isMessagesError: boolean;
  isMessagesLoading: boolean;
  messages: ChatMessage[];
  requestedConversationId?: string;
  onSendMessage: (content: string) => boolean;
};

function userName(user: UserSummary) {
  const fullName = `${user.firstName} ${user.lastName}`.trim();
  return fullName || user.id;
}

function ConversationPanel({
  conversation,
  currentUserId,
  isConnected,
  isMessagesError,
  isMessagesLoading,
  messages,
  requestedConversationId,
  onSendMessage,
}: ConversationPanelProps) {
  const { i18n, t } = useTranslation();
  const messageListRef = useRef<HTMLDivElement>(null);
  const language = i18n.resolvedLanguage ?? i18n.language;

  useEffect(() => {
    const messageList = messageListRef.current;
    if (messageList) {
      messageList.scrollTop = messageList.scrollHeight;
    }
  }, [conversation?.id, messages.length]);

  const formatDateTime = (value: string) =>
    new Intl.DateTimeFormat(language, {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));

  return (
    <section className={`${requestedConversationId ? 'flex' : 'hidden md:flex'} min-h-0 flex-col`}>
      {!requestedConversationId ? (
        <div className="grid flex-1 place-items-center p-8 text-center text-body">
          <p>{t('conversations.select')}</p>
        </div>
      ) : !conversation ? (
        <div className="grid flex-1 place-items-center p-8">
          <ErrorState message={t('conversations.errors.not_found')} />
        </div>
      ) : (
        <>
          <header className="flex shrink-0 items-center gap-3 border-b border-border px-4 py-3">
            <Link className="text-sm md:hidden" to="/conversations">
              {t('conversations.back')}
            </Link>
            <UserAvatar
              avatarUrl={`/api/users/${conversation.otherParticipant.id}/avatar`}
              firstName={conversation.otherParticipant.firstName}
              lastName={conversation.otherParticipant.lastName}
              size="small"
            />
            <div className="min-w-0">
              <h2 className="truncate font-extrabold text-heading">
                {userName(conversation.otherParticipant)}
              </h2>
              <Link className="block truncate text-sm" to={`/jobs/${conversation.jobId}`}>
                {conversation.jobTitle}
              </Link>
            </div>
          </header>

          <div
            className="flex min-h-0 flex-1 flex-col gap-3 overflow-y-auto bg-page p-4"
            ref={messageListRef}
          >
            {isMessagesLoading ? (
              <LoadingState message={t('conversations.messages_loading')} />
            ) : null}
            {!isMessagesLoading && isMessagesError ? (
              <ErrorState message={t('conversations.errors.messages_load_failed')} />
            ) : null}
            {!isMessagesLoading && !isMessagesError
              ? messages.map((message) => {
                  const ownMessage = message.sender.id === currentUserId;
                  return (
                    <article
                      className={`max-w-[80%] rounded-xl px-4 py-2 shadow-sm ${ownMessage ? 'ml-auto bg-primary text-white' : 'mr-auto border border-border bg-surface text-heading'}`}
                      key={message.id}
                    >
                      <p className="wrap-break-word whitespace-pre-wrap text-sm">
                        {message.content}
                      </p>
                      <time
                        className={`mt-1 block text-xs ${ownMessage ? 'text-white/80' : 'text-body'}`}
                      >
                        {formatDateTime(message.createdAt)}
                      </time>
                    </article>
                  );
                })
              : null}
          </div>

          <MessageComposer isConnected={isConnected} onSendMessage={onSendMessage} />
        </>
      )}
    </section>
  );
}

export default ConversationPanel;
