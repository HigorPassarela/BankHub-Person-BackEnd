package com.bankhub.notification.application.port.in;

import com.bankhub.notification.infrastructure.web.dto.NotificationHistoryResponse;

public interface QueryNotificationHistoryUseCase {

    /**
     * Query notification history for a given account with pagination.
     *
     * @param accountId Account ID to query notifications for
     * @param page      Page number (0-indexed)
     * @param size      Page size
     * @return Paginated notification history response
     */
    NotificationHistoryResponse execute(String accountId, int page, int size);
}
