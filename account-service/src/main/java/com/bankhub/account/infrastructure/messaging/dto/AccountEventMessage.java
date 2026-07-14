package com.bankhub.account.infrastructure.messaging.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AccountEventMessage(
        Header header,
        Payload payload
) {

    @Builder
    public record Header(
            String correlationId,
            String eventType,
            LocalDateTime timestamp
    ) {}

    @Builder
    public record Payload(
       String accountId,
       String status
    ) {}
}
