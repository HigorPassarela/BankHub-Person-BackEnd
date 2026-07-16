package com.bankhub.onboarding.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Representa o payload HTTP (JSON) para a solicitação de abertura de conta.
 */
public record OnboardingRequest(

        @NotBlank(message = "O documento (CPF/CNPJ) é obrigatório.")
        String documentNumber,

        @NotNull(message = "A renda mensal deve ser informada.")
        @PositiveOrZero(message = "A renda mensal não pode ser um valor negativo.")
        BigDecimal monthlyIncome
) {
}
