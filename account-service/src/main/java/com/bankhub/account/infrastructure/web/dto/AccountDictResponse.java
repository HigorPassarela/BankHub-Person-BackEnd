package com.bankhub.account.infrastructure.web.dto;

import lombok.Builder;

@Builder
public record AccountDictResponse(
        String accountId,
        String customerId,
        String agency,
        String accountNumber,
        String fullName
) {}