import { useCallback, useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';

import type { ChatMessage, Conversation } from '../types/conversation';
import { conversationMessagesQueryKey, conversationsQueryKey } from './useConversations';
import useWebSocket from './useWebSocket';

export function useChatMessageSubscription() {
  const queryClient = useQueryClient();
  const { client, isConnected } = useWebSocket();

  useEffect(() => {
    if (!client || !isConnected) {
      return;
    }

    const subscription = client.subscribe('/user/exchange/amq.direct/messages', (frame) => {
      const message = JSON.parse(frame.body) as ChatMessage;

      queryClient.setQueryData<ChatMessage[]>(
        conversationMessagesQueryKey(message.conversationId),
        (current) => {
          if (!current || current.some((item) => item.id === message.id)) {
            return current;
          }
          return [...current, message];
        }
      );

      queryClient.setQueryData<Conversation[]>(conversationsQueryKey, (current) => {
        if (!current) {
          return current;
        }

        const conversation = current.find((item) => item.id === message.conversationId);
        if (!conversation) {
          return current;
        }

        const updatedConversation = {
          ...conversation,
          lastMessage: message.content,
          lastMessageAt: message.createdAt,
        };
        return [
          updatedConversation,
          ...current.filter((item) => item.id !== message.conversationId),
        ];
      });
    });

    return () => subscription.unsubscribe();
  }, [client, isConnected, queryClient]);
}

export function useSendChatMessage() {
  const { client, isConnected } = useWebSocket();

  const sendMessage = useCallback(
    (conversationId: string, content: string) => {
      if (!client || !isConnected) {
        return false;
      }

      client.publish({
        destination: `/app/conversations/${conversationId}/messages`,
        body: JSON.stringify({ content }),
      });
      return true;
    },
    [client, isConnected]
  );

  return { isConnected, sendMessage };
}
