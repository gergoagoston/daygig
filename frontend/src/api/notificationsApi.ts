import type { NotificationSummary } from '../types/notification';
import httpClient from './httpClient';

export async function getNotifications(): Promise<NotificationSummary> {
  const response = await httpClient.get<NotificationSummary>('/notifications');
  return response.data;
}

export async function markNotificationAsRead(notificationId: string): Promise<void> {
  await httpClient.patch(`/notifications/${notificationId}/read`);
}

export async function markAllNotificationsAsRead(): Promise<void> {
  await httpClient.patch('/notifications/read-all');
}
