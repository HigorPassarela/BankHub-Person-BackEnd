package com.bankhub.notification.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Notification data transfer object")
public record NotificationDto(
        @Schema(description = "Notification unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Notification type", example = "ACCOUNT_ACTIVATION")
        String type,

        @Schema(description = "Notification title", example = "Conta ativada com sucesso")
        String title,

        @Schema(description = "Notification message content", example = "Sua conta foi ativada. Agência: 0001 Conta: 12345-6")
        String message,

        @Schema(description = "Timestamp when notification was sent", example = "2026-08-07T10:30:00")
        LocalDateTime sentAt,

        @Schema(description = "Timestamp when notification was read", example = "2026-08-07T12:45:00", nullable = true)
        LocalDateTime readAt,

        @Schema(description = "Notification read status", example = "false")
        Boolean readStatus
) {
}
