export type NotificationType =
  | 'application.created'
  | 'application.rejected'
  | 'application.withdrawn'
  | 'job.completed'
  | 'conversation.started'
  | 'message.created';

export type Notification = {
  id: string;
  type: NotificationType;
  jobId: string | null;
  conversationId: string | null;
  createdAt: string;
};

export type NotificationSummary = {
  unreadCount: number;
  notifications: Notification[];
};
