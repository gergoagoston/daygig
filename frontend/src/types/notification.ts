export type NotificationType =
  | 'application.created'
  | 'application.rejected'
  | 'application.withdrawn'
  | 'job.completed';

export type Notification = {
  id: string;
  type: NotificationType;
  jobId: string | null;
  createdAt: string;
};

export type NotificationSummary = {
  unreadCount: number;
  notifications: Notification[];
};
