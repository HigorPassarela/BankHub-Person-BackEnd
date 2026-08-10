package com.bankhub.notification.application.port.in;

import java.util.UUID;

public interface UpdateReadStatusUseCase {

    /**
     * Mark a notification as read.
     *
     * @param notificationId Notification ID
     */
    void markAsRead(UUID notificationId);

    /**
     * Mark a notification as unread.
     *
     * @param notificationId Notification ID
     */
    void markAsUnread(UUID notificationId);

    /**
     * Mark all notifications as read for a given account.
     *
     * @param accountId Account ID
     * @return Number of notifications marked as read
     */
    int markAllAsRead(String accountId);
}
