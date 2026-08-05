import { useQuery } from '@tanstack/react-query';

import { getConversationMessages, getConversations } from '../api/conversationsApi';

export const conversationsQueryKey = ['conversations'] as const;

export function conversationMessagesQueryKey(conversationId: string) {
  return ['conversations', conversationId, 'messages'] as const;
}

export function useConversations() {
  const conversationsQuery = useQuery({
    queryKey: conversationsQueryKey,
    queryFn: getConversations,
  });

  return {
    conversations: conversationsQuery.data ?? [],
    isError: conversationsQuery.isError,
    isLoading: conversationsQuery.isLoading,
  };
}

export function useConversationMessages(conversationId: string | undefined) {
  const messagesQuery = useQuery({
    queryKey: conversationMessagesQueryKey(conversationId ?? ''),
    queryFn: () => getConversationMessages(conversationId!),
    enabled: Boolean(conversationId),
  });

  return {
    messages: messagesQuery.data ?? [],
    isError: messagesQuery.isError,
    isLoading: messagesQuery.isLoading,
  };
}
