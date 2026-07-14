package com.bankhub.onboarding.domain.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StandardRiskStrategy implements RiskCalculationStrategy{

    private static final BigDecimal MINIMUM_INCOME = new BigDecimal("1000.00");
    private static final BigDecimal VIP_INCOME_THRESHOLD = new BigDecimal("5000.00");

    @Override
    public RiskResult evaluate(CustomerContext context) {
        BigDecimal income = context.monthlyIncome();

        if (income.compareTo(MINIMUM_INCOME) < 0) {
            return new RiskResult(false, "HIGH", "Renda mensal abaixo do limite mínimo exigido pelo Bank-Hub.");
        }

        if (income.compareTo(VIP_INCOME_THRESHOLD) >= 0) {
            return new RiskResult(true, "LOW", "Cliente aprovado automaticamente com perfil de alta renda.");
        }

        return new RiskResult(true, "MEDIUM", "Cliente aprovado no perfil de crédito padrão.");
    }
}
