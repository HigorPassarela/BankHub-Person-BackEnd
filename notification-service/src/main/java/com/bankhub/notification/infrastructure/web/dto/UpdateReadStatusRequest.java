package com.bankhub.notification.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Update notification read status request")
public record UpdateReadStatusRequest(
        @NotNull(message = "Read status is required")
        @Schema(description = "New read status (true for read, false for unread)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean readStatus
) {
}
