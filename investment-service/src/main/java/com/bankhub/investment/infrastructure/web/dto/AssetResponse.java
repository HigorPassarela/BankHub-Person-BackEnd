package com.bankhub.investment.infrastructure.web.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AssetResponse(
        String ticker,
        String type,
        BigDecimal quantity,
        BigDecimal averagePrice
) {
}
