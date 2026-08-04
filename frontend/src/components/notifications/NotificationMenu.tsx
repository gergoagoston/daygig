import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';

import type { Notification } from '../../types/notification';

export type NotificationMenuProps = {
  notifications: Notification[];
  unreadCount: number;
  isLoading: boolean;
  markAsRead: (notificationId: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
};

function NotificationMenu({
  notifications,
  unreadCount,
  isLoading,
  markAsRead,
  markAllAsRead,
}: NotificationMenuProps) {
  const { i18n, t } = useTranslation();
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    const closeOnOutsideClick = (event: PointerEvent) => {
      if (event.target instanceof Node && !menuRef.current?.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('pointerdown', closeOnOutsideClick);
    return () => document.removeEventListener('pointerdown', closeOnOutsideClick);
  }, [isOpen]);

  const openNotification = async (notification: Notification) => {
    await markAsRead(notification.id);
    setIsOpen(false);
    if (notification.jobId) {
      navigate(`/jobs/${notification.jobId}`);
    }
  };

  return (
    <div className="relative" ref={menuRef}>
      <button
        className="notification-button"
        type="button"
        aria-label={t('notifications.open')}
        aria-expanded={isOpen}
        onClick={() => setIsOpen((current) => !current)}
      >
        <svg
          aria-hidden="true"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
          <path d="M13.7 21a2 2 0 0 1-3.4 0" />
        </svg>
        {unreadCount > 0 ? (
          <span className="notification-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
        ) : null}
      </button>

      {isOpen ? (
        <section className="notification-menu" aria-label={t('notifications.title')}>
          <div className="flex items-center justify-between gap-4 border-b border-border px-4 py-3">
            <h2 className="text-base font-extrabold text-heading">{t('notifications.title')}</h2>
            {unreadCount > 0 ? (
              <button
                className="text-sm font-bold text-primary"
                type="button"
                onClick={() => void markAllAsRead()}
              >
                {t('notifications.mark_all_read')}
              </button>
            ) : null}
          </div>

          {isLoading ? (
            <p className="px-4 py-5 text-sm text-muted">{t('notifications.loading')}</p>
          ) : notifications.length === 0 ? (
            <p className="px-4 py-5 text-sm text-muted">{t('notifications.empty')}</p>
          ) : (
            <div className="grid">
              {notifications.map((notification) => (
                <button
                  className="border-b border-border px-4 py-3 text-left hover:bg-primary-soft"
                  type="button"
                  key={notification.id}
                  onClick={() => void openNotification(notification)}
                >
                  <span className="block text-sm font-bold text-heading">
                    {t(`notifications.types.${notification.type}`)}
                  </span>
                  <time className="mt-1 block text-xs text-muted">
                    {new Intl.DateTimeFormat(i18n.resolvedLanguage ?? i18n.language, {
                      dateStyle: 'medium',
                      timeStyle: 'short',
                    }).format(new Date(notification.createdAt))}
                  </time>
                </button>
              ))}
            </div>
          )}
        </section>
      ) : null}
    </div>
  );
}

export default NotificationMenu;
