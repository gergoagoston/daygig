import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import {
  createConversation,
  getConversationMessages,
  getConversations,
} from '../api/conversationsApi';
import type { Conversation } from '../types/conversation';

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

export function useCreateConversation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ applicationId, content }: { applicationId: string; content: string }) =>
      createConversation(applicationId, content),
    onSuccess: (conversation) => {
      queryClient.setQueryData<Conversation[]>(conversationsQueryKey, (current) => {
        if (current?.some((item) => item.id === conversation.id)) {
          return current;
        }
        return [conversation, ...(current ?? [])];
      });
    },
  });
}
