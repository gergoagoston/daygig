import type { ChatMessage, Conversation } from '../types/conversation';
import httpClient from './httpClient';

export async function getConversations(): Promise<Conversation[]> {
  const response = await httpClient.get<Conversation[]>('/conversations');
  return response.data;
}

export async function getConversationMessages(conversationId: string): Promise<ChatMessage[]> {
  const response = await httpClient.get<ChatMessage[]>(`/conversations/${conversationId}/messages`);
  return response.data;
}
