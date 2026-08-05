import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router-dom';

import UserAvatar from '../components/profile/UserAvatar';
import EmptyState from '../components/ui/EmptyState';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';
import useAuth from '../hooks/useAuth';
import { useConversationMessages, useConversations } from '../hooks/useConversations';
import type { UserSummary } from '../types/user';

function userName(user: UserSummary) {
  const fullName = `${user.firstName} ${user.lastName}`.trim();
  return fullName || user.id;
}

function ConversationsPage() {
  const { conversationId } = useParams();
  const { i18n, t } = useTranslation();
  const { user } = useAuth();
  const { conversations, isError, isLoading } = useConversations();
  const selectedConversation = conversations.find(
    (conversation) => conversation.id === conversationId
  );
  const messagesQuery = useConversationMessages(selectedConversation?.id);
  const language = i18n.resolvedLanguage ?? i18n.language;

  const formatDateTime = (value: string) =>
    new Intl.DateTimeFormat(language, {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));

  if (isLoading) {
    return (
      <main className="app-page">
        <LoadingState message={t('conversations.loading')} />
      </main>
    );
  }

  if (isError) {
    return (
      <main className="app-page">
        <ErrorState message={t('conversations.errors.load_failed')} />
      </main>
    );
  }

  if (conversations.length === 0) {
    return (
      <main className="app-page">
        <EmptyState title={t('conversations.empty.title')} />
      </main>
    );
  }

  return (
    <main className="app-page">
      <div className="mx-auto grid min-h-[calc(100svh-136px)] max-w-6xl overflow-hidden rounded-xl border border-border bg-surface shadow-panel md:grid-cols-[minmax(260px,340px)_1fr]">
        <aside
          className={`${conversationId ? 'hidden md:flex' : 'flex'} min-h-0 flex-col border-border md:border-r`}
        >
          <h1 className="m-0! border-b border-border px-5 py-4 text-2xl! font-extrabold!">
            {t('conversations.title')}
          </h1>
          <div className="grid overflow-y-auto">
            {conversations.map((conversation) => {
              const selected = conversation.id === conversationId;
              return (
                <Link
                  className={`grid grid-cols-[auto_1fr] gap-3 border-b border-border px-4 py-3 no-underline! ${selected ? 'bg-primary-soft' : 'hover:bg-primary-soft'}`}
                  key={conversation.id}
                  to={`/conversations/${conversation.id}`}
                >
                  <UserAvatar
                    avatarUrl={`/api/users/${conversation.otherParticipant.id}/avatar`}
                    firstName={conversation.otherParticipant.firstName}
                    lastName={conversation.otherParticipant.lastName}
                    size="small"
                  />
                  <span className="min-w-0">
                    <span className="block truncate text-sm font-extrabold text-heading">
                      {userName(conversation.otherParticipant)}
                    </span>
                    <span className="block truncate text-xs font-semibold text-primary">
                      {conversation.jobTitle}
                    </span>
                    <span className="mt-1 block truncate text-sm font-normal text-body">
                      {conversation.lastMessage}
                    </span>
                    <time className="mt-1 block text-xs font-normal text-body">
                      {formatDateTime(conversation.lastMessageAt)}
                    </time>
                  </span>
                </Link>
              );
            })}
          </div>
        </aside>

        <section className={`${conversationId ? 'flex' : 'hidden md:flex'} min-h-0 flex-col`}>
          {!conversationId ? (
            <div className="grid flex-1 place-items-center p-8 text-center text-body">
              <p>{t('conversations.select')}</p>
            </div>
          ) : !selectedConversation ? (
            <div className="grid flex-1 place-items-center p-8">
              <ErrorState message={t('conversations.errors.not_found')} />
            </div>
          ) : (
            <>
              <header className="flex items-center gap-3 border-b border-border px-4 py-3">
                <Link className="text-sm md:hidden" to="/conversations">
                  {t('conversations.back')}
                </Link>
                <UserAvatar
                  avatarUrl={`/api/users/${selectedConversation.otherParticipant.id}/avatar`}
                  firstName={selectedConversation.otherParticipant.firstName}
                  lastName={selectedConversation.otherParticipant.lastName}
                  size="small"
                />
                <div className="min-w-0">
                  <h2 className="truncate font-extrabold text-heading">
                    {userName(selectedConversation.otherParticipant)}
                  </h2>
                  <Link
                    className="block truncate text-sm"
                    to={`/jobs/${selectedConversation.jobId}`}
                  >
                    {selectedConversation.jobTitle}
                  </Link>
                </div>
              </header>

              <div className="flex flex-1 flex-col gap-3 overflow-y-auto bg-page p-4">
                {messagesQuery.isLoading ? (
                  <LoadingState message={t('conversations.messages_loading')} />
                ) : null}
                {!messagesQuery.isLoading && messagesQuery.isError ? (
                  <ErrorState message={t('conversations.errors.messages_load_failed')} />
                ) : null}
                {!messagesQuery.isLoading && !messagesQuery.isError
                  ? messagesQuery.messages.map((message) => {
                      const ownMessage = message.sender.id === user?.id;
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
            </>
          )}
        </section>
      </div>
    </main>
  );
}

export default ConversationsPage;
