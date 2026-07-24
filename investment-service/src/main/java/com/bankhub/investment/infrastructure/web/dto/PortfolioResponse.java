package com.bankhub.investment.infrastructure.web.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Representa o espelho visual da carteira do cliente, formatado para telas.
 */
@Builder
public record PortfolioResponse(
        String portfolioId,
        String customerId,
        List<AssetResponse> assets,
        LocalDateTime lastUpdate
) {
}
