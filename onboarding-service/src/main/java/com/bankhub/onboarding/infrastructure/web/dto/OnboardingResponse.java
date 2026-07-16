package com.bankhub.onboarding.infrastructure.web.dto;

import lombok.Builder;

/**
 * DTO de Resposta para manter a coesão do contrato de saída.
 */
@Builder
public record OnboardingResponse(
        String message,
        String protocol
) {
}
