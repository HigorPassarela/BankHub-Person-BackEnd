package com.bankhub.account.infrastructure.web.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AccountResponse(
        String account,
        String customerId,
        BankDetailsResponse bankDetails,
        BalanceResponse balance,
        String status,
        boolean hasTransactionPin,
        boolean identityVerified,
        String selfieUrl,
        String investorProfile,
        LocalDateTime lastUpdate,
        String fullName,
        String phone,
        String address
) {
    @Builder
    public record BankDetailsResponse(String agency, String number) {}
    @Builder
    public record BalanceResponse(BigDecimal valor, String moeda) {}
}