import type { UserSummary } from './user';

export type Conversation = {
  id: string;
  applicationId: string;
  jobId: string;
  jobTitle: string;
  otherParticipant: UserSummary;
  lastMessage: string;
  lastMessageAt: string;
};

export type ChatMessage = {
  id: string;
  conversationId: string;
  sender: UserSummary;
  content: string;
  createdAt: string;
};
