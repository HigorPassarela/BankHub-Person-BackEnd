package com.bankhub.transaction.infrastructure.web.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resposta devolvida ao front-end como comprovante da transação (Recibo inicial).
 */
@Builder
public record PixResponse(
        String transactionId,
        String destinationAccountId,
        BigDecimal amount,
        String status,
        LocalDateTime timestamp
) {
}
