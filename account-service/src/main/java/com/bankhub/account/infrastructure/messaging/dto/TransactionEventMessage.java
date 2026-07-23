package com.bankhub.account.infrastructure.messaging.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa o evento assíncrono trafegado no Kafka oriundo do Transaction-Service.
 */
public record TransactionEventMessage(
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
            String transactionId,
            String sourceAccountId,
            String destinationAccountId,
            BigDecimal amount,
            String type,
            String status
    ) {
    }
}
