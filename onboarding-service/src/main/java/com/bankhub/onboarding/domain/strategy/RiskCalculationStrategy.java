package com.bankhub.onboarding.domain.strategy;

/**
 * Interface base para o padrão Strategy de avaliação de risco.
 */
public interface RiskCalculationStrategy {

    /**
     * Avalia o perfil do cliente e retorna o veredito de aprovação.
     *
     * @param context Dados financeiros e cadastrais do cliente.
     * @return O resultado detalhado da avaliação.
     */
    RiskResult evaluate(CustomerContext context);
}
