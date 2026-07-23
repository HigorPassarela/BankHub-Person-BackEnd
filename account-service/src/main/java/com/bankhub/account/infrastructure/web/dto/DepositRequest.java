package com.bankhub.account.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Payload HTTP para realizar um depósito (Cash-In) em uma conta.
 */
public record DepositRequest(

        @NotNull(message = "O valor do depósito é obrigatório.")
        @DecimalMin(value = "0.01", message = "O valor do depósito deve ser maior que zero.")
        BigDecimal amount
) {
}
