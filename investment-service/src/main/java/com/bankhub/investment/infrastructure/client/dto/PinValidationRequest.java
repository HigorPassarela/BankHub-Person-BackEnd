package com.bankhub.investment.infrastructure.client.dto;

/**
 * Payload M2M para envio do PIN de validação ao Motor de Contas.
 */
public record PinValidationRequest(
        String transactionPin
) {
}
