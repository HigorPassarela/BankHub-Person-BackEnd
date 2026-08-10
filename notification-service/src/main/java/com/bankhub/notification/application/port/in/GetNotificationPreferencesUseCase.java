package com.bankhub.notification.application.port.in;

import com.bankhub.notification.infrastructure.web.dto.NotificationPreferencesResponse;

public interface GetNotificationPreferencesUseCase {

    /**
     * Get notification preferences for a given account.
     * Returns default preferences if none exist.
     *
     * @param accountId Account ID
     * @return Notification preferences response
     */
    NotificationPreferencesResponse execute(String accountId);
}
