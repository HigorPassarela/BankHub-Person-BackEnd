package com.bankhub.transaction.infrastructure.web.dto;

import lombok.Builder;

/**
 * Resposta contendo os metadados do recebedor de um PIX.
 */
@Builder
public record PixKeyResolveResponse(
        String pixKey,
        String receiverName,
        String maskedCpf,
        String destinationAccountId,
        String bankName
) {
}
