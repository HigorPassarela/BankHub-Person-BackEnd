package com.bankhub.hubia.infrastructure.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa a estrutura JSON retornada pela API do microsserviço de contas.
 */
public record AccountClientResponse(
        String account,
        BalanceResponse balance,
        String status,
        LocalDateTime lastUpdate
) {

    public record BalanceResponse(
            BigDecimal valor,
            String moeda
    ){}
}
