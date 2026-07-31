package com.bankhub.investment.infrastructure.client.dto;

import java.math.BigDecimal;

/**
 * Payload de comunicação síncrona para solicitar o crédito/estorno no Account Service.
 */
public record DepositRequest(
        BigDecimal amount
) {
}
