package com.bankhub.onboarding.domain.strategy;

import java.math.BigDecimal;

/**
 * Contém as informações financeiras e cadastrais para a tomada de decisão de risco.
 */
public record CustomerContext(
        String customerId,
        String documentNumber,
        BigDecimal monthlyIncome
) {

    public CustomerContext {
        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A renda mensal deve ser informada e não pode ser negativa.");
        }
    }
}
