package com.bankhub.hubia.infrastructure.web.dto;

import lombok.Builder;

/**
 * Representa a resposta padronizada da Inteligência Artificial.
 */
@Builder
public record ChatResponse(
        String reply
) {
}
