package com.bankhub.account.infrastructure.web.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CardResponse(
        String cardId,
        String accountId,
        String type,
        String maskedNumber,
        String expirationDate,
        String cvv,
        String cardholderName,
        boolean isBlocked,
        BigDecimal availableLimit
) {
}
