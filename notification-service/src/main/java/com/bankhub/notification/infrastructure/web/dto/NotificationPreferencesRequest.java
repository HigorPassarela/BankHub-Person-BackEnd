package com.bankhub.notification.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Notification preferences update request")
public record NotificationPreferencesRequest(
        @NotNull(message = "Email enabled flag is required")
        @Schema(description = "Enable or disable email notifications", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean emailEnabled,

        @NotNull(message = "Push enabled flag is required")
        @Schema(description = "Enable or disable push notifications", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean pushEnabled
) {
}
