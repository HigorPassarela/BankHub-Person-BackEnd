package com.bankhub.transaction.infrastructure.client.dto;

/**
 * Payload M2M (Microsserviço para Microsserviço) usado para enviar
 * a senha do cliente ao Account Service para validação Zero Trust.
 */
public record PinValidationRequest(
        String transactionPin
) {
}