package com.bankhub.notification.infrastructure.messaging.dto;

import java.time.LocalDateTime;

/**
 * Representa o evento assíncrono trafegado no Kafka (CloudEvents).
 */
public record AccountEventMessage(
        Header header,
        Payload payload
) {

    public record Header(
            String correlationId,
            String eventType,
            LocalDateTime timestamp
    ) {
    }

    public record Payload(
            String accountId,
            String status
    ) {
    }
}
