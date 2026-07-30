package com.bankhub.transaction.infrastructure.web.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record BoletoResolveResponse(
        String barcode,
        String companyName,
        BigDecimal amount,
        LocalDate dueDate,
        boolean isExpired
) {
}
