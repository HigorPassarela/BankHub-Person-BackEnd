package com.bankhub.notification.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Notification preferences response")
public record NotificationPreferencesResponse(
        @Schema(description = "Account ID", example = "ACC123456")
        String accountId,

        @Schema(description = "Email notifications enabled", example = "true")
        Boolean emailEnabled,

        @Schema(description = "Push notifications enabled", example = "false")
        Boolean pushEnabled,

        @Schema(description = "Last update timestamp", example = "2026-08-07T10:30:00")
        LocalDateTime updatedAt
) {
}
