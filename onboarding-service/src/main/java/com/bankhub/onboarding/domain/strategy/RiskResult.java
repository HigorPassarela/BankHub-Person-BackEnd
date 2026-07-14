package com.bankhub.onboarding.domain.strategy;

/**
 * Contém o resultado final da avaliação do motor de risco.
 */
public record RiskResult(
        boolean approved,
        String riskLevel,
        String reason
) {

    // Construtor compacto para garantir que sempre haja um motivo em caso de reprovação
    public RiskResult {
        if (!approved && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("Uma justificativa (reason) é obrigatória quando o cliente é reprovado.");
        }
    }
}
