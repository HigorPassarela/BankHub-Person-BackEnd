package com.bankhub.investment.infrastructure.client.dto;

import java.math.BigDecimal;

/**
 * Payload de comunicação síncrona para solicitar o débito no Account Service.
 */
public record DebitRequest(BigDecimal amount) {
}
