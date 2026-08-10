package com.bankhub.notification.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated notification history response")
public record NotificationHistoryResponse(
        @Schema(description = "List of notifications in the current page")
        List<NotificationDto> notifications,

        @Schema(description = "Pagination metadata")
        PageMetadata metadata
) {
}
