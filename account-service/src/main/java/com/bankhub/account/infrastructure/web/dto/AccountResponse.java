package com.bankhub.account.infrastructure.web.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AccountResponse(
        String account,
        BalanceResponse balance,
        String status,
        LocalDateTime lastUpdate
) {

    /**
     * DTO aninhado para representar o bloco do saldo conforme contrato JSON:
     * { "valor": 1500.50, "moeda": "BRL" }
     */
    @Builder
    public record BalanceResponse(
       BigDecimal valor,
       String moeda
    ) {}
}
