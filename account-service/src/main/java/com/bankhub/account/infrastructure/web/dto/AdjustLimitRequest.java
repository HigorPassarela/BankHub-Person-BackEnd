package com.bankhub.account.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Payload HTTP recebido para ajustar o limite do cartão.
 */
public record AdjustLimitRequest(

        @NotNull(message = "O novo limite é obrigatório.")
        @DecimalMin(value = "0.00", message = "O limite de crédito não pode ser negativo.")
        BigDecimal newLimit
) {
}
