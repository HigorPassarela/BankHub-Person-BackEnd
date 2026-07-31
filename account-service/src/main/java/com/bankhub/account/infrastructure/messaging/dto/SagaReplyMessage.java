package com.bankhub.account.infrastructure.messaging.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Representa a resposta de um passo da Saga (Ex: Liquidação de PIX concluída ou falha).
 */
@Builder
public record SagaReplyMessage(
        Header header,
        Payload payload
) {

    @Builder
    public record Header(
            String correlationId,
            String eventType,
            LocalDateTime timestamp
    ) {
    }

    @Builder
    public record Payload(
            String transactionId,
            String sagaStatus,
            String failureReason
    ) {
    }
}
