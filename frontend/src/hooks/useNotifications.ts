import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';

import {
  getNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from '../api/notificationsApi';
import type { Notification, NotificationSummary } from '../types/notification';
import useWebSocket from './useWebSocket';

export const notificationsQueryKey = ['notifications'] as const;

function useNotifications(enabled: boolean) {
  const queryClient = useQueryClient();
  const { client, isConnected } = useWebSocket();
  const notificationsQuery = useQuery({
    queryKey: notificationsQueryKey,
    queryFn: getNotifications,
    enabled,
  });

  useEffect(() => {
    if (!enabled) {
      queryClient.removeQueries({ queryKey: notificationsQueryKey });
      return;
    }

    if (!client || !isConnected) {
      return;
    }

    const subscription = client.subscribe('/user/exchange/amq.direct/notifications', (message) => {
      const notification = JSON.parse(message.body) as Notification;
      queryClient.setQueryData<NotificationSummary>(notificationsQueryKey, (current) => {
        if (current?.notifications.some((item) => item.id === notification.id)) {
          return current;
        }

        return {
          unreadCount: (current?.unreadCount ?? 0) + 1,
          notifications: [notification, ...(current?.notifications ?? [])].slice(0, 10),
        };
      });
    });

    return () => subscription.unsubscribe();
  }, [client, enabled, isConnected, queryClient]);

  const markAsReadMutation = useMutation({
    mutationFn: markNotificationAsRead,
    onSuccess: (_, notificationId) => {
      queryClient.setQueryData<NotificationSummary>(notificationsQueryKey, (current) => {
        if (!current) {
          return current;
        }

        return {
          unreadCount: Math.max(0, current.unreadCount - 1),
          notifications: current.notifications.filter(
            (notification) => notification.id !== notificationId
          ),
        };
      });
    },
  });

  const markAllAsReadMutation = useMutation({
    mutationFn: markAllNotificationsAsRead,
    onSuccess: () => {
      queryClient.setQueryData<NotificationSummary>(notificationsQueryKey, {
        unreadCount: 0,
        notifications: [],
      });
    },
  });

  return {
    notifications: notificationsQuery.data?.notifications ?? [],
    unreadCount: notificationsQuery.data?.unreadCount ?? 0,
    isLoading: notificationsQuery.isLoading,
    markAsRead: (notificationId: string) => markAsReadMutation.mutateAsync(notificationId),
    markAllAsRead: () => markAllAsReadMutation.mutateAsync(),
  };
}

export default useNotifications;
