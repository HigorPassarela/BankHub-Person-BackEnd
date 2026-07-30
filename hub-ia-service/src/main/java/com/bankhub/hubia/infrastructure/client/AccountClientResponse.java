package com.bankhub.hubia.infrastructure.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa a estrutura JSON retornada pela API do microsserviço de contas.
 */
public record AccountClientResponse(
        String account,
        BankDetailsResponse bankDetails,
        BalanceResponse balance,
        String status,
        String investorProfile,
        LocalDateTime lastUpdate
) {

    public record BankDetailsResponse(
            String agency,
            String number
    ) {}

    public record BalanceResponse(
            BigDecimal valor,
            String moeda
    ) {}

}