import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';

import ConversationList from '../components/conversations/ConversationList';
import ConversationPanel from '../components/conversations/ConversationPanel';
import EmptyState from '../components/ui/EmptyState';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';
import useAuth from '../hooks/useAuth';
import { useChatMessageSubscription, useSendChatMessage } from '../hooks/useChatMessaging';
import { useConversationMessages, useConversations } from '../hooks/useConversations';

function ConversationsPage() {
  useChatMessageSubscription();
  const { conversationId } = useParams();
  const { t } = useTranslation();
  const { user } = useAuth();
  const { isConnected, sendMessage } = useSendChatMessage();
  const { conversations, isError, isLoading } = useConversations();
  const selectedConversation = conversations.find(
    (conversation) => conversation.id === conversationId
  );
  const messagesQuery = useConversationMessages(selectedConversation?.id);

  const sendSelectedConversationMessage = (content: string) => {
    if (!selectedConversation) {
      return false;
    }
    return sendMessage(selectedConversation.id, content);
  };
  const pageClassName = 'app-page h-full !min-h-0 overflow-hidden';

  if (isLoading) {
    return (
      <main className={pageClassName}>
        <LoadingState message={t('conversations.loading')} />
      </main>
    );
  }

  if (isError) {
    return (
      <main className={pageClassName}>
        <ErrorState message={t('conversations.errors.load_failed')} />
      </main>
    );
  }

  if (conversations.length === 0) {
    return (
      <main className={pageClassName}>
        <EmptyState title={t('conversations.empty.title')} />
      </main>
    );
  }

  return (
    <main className={pageClassName}>
      <div className="mx-auto grid h-full min-h-0 max-w-6xl overflow-hidden rounded-xl border border-border bg-surface shadow-panel md:grid-cols-[minmax(260px,340px)_1fr]">
        <ConversationList conversations={conversations} selectedConversationId={conversationId} />
        <ConversationPanel
          conversation={selectedConversation}
          currentUserId={user?.id}
          isConnected={isConnected}
          isMessagesError={messagesQuery.isError}
          isMessagesLoading={messagesQuery.isLoading}
          messages={messagesQuery.messages}
          requestedConversationId={conversationId}
          onSendMessage={sendSelectedConversationMessage}
        />
      </div>
    </main>
  );
}

export default ConversationsPage;
