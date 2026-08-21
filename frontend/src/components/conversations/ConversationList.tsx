import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import type { Conversation } from '../../types/conversation';
import type { UserSummary } from '../../types/user';
import UserAvatar from '../profile/UserAvatar';

type ConversationListProps = {
  conversations: Conversation[];
  selectedConversationId?: string;
};

function userName(user: UserSummary) {
  const fullName = `${user.firstName} ${user.lastName}`.trim();
  return fullName || user.id;
}

function ConversationList({ conversations, selectedConversationId }: ConversationListProps) {
  const { i18n, t } = useTranslation();
  const language = i18n.resolvedLanguage ?? i18n.language;
  const formatDateTime = (value: string) =>
    new Intl.DateTimeFormat(language, {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));

  return (
    <aside
      className={`${selectedConversationId ? 'hidden md:flex' : 'flex'} min-h-0 flex-col border-border md:border-r`}
    >
      <h1 className="m-0! border-b border-border px-5 py-4 text-2xl! font-extrabold!">
        {t('conversations.title')}
      </h1>
      <div className="min-h-0 flex-1 overflow-y-auto">
        {conversations.map((conversation) => {
          const selected = conversation.id === selectedConversationId;
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
  );
}

export default ConversationList;
