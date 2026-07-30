package com.bankhub.transaction.infrastructure.client.dto;

import lombok.Builder;

@Builder
public record AccountDictResponse(
        String accountId,
        String customerId,
        String agency,
        String accountNumber
) {
}
