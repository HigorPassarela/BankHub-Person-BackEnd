package com.bankhub.notification.application.port.in;

import com.bankhub.notification.infrastructure.web.dto.NotificationPreferencesRequest;
import com.bankhub.notification.infrastructure.web.dto.NotificationPreferencesResponse;

public interface UpdateNotificationPreferencesUseCase {

    /**
     * Update notification preferences for a given account.
     *
     * @param accountId Account ID
     * @param request   Preferences update request
     * @return Updated notification preferences
     */
    NotificationPreferencesResponse execute(String accountId, NotificationPreferencesRequest request);
}
