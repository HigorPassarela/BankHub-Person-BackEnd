package com.bankhub.account.domain;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa um Cartão vinculado a uma conta corrente.
 */
@Builder(toBuilder = true)
public record Card(
        String id,
        String accountId,
        CardType type,
        String cardNumber,
        String cardholderName,
        String expirationDate,
        String cvvHash,
        String physicalPinHash,
        boolean isBlocked,
        boolean nfcEnabled,
        boolean onlinePurchasesEnabled,
        boolean internationalUsageEnabled,
        BigDecimal creditLimit,
        BigDecimal availableLimit,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public Card {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Todo cartão deve pertencer a uma conta válida.");
        }
        if (cardNumber == null || cardNumber.length() != 16) {
            throw new IllegalArgumentException("O número do cartão deve conter exatamente 16 dígitos.");
        }
    }

    /**
     * Oculta o número real do cartão para exibição em extratos (Compliance PCI-DSS).
     * Exemplo: "•••• •••• •••• 1111"
     */
    public String getMaskedNumber() {
        return "•••• •••• •••• " + cardNumber.substring(12, 16);
    }

    public Card toggleBlock() {
        return this.toBuilder()
                .isBlocked(!this.isBlocked)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Card adjustLimit(BigDecimal newLimit) {
        if (newLimit == null || newLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O limite de crédito não pode ser negativo.");
        }

        BigDecimal utilizedAmount = this.creditLimit.subtract(this.availableLimit);
        BigDecimal newAvailableLimit = newLimit.subtract(utilizedAmount);

        if (newAvailableLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O novo limite não pode ser menor que o valor já utilizado na fatura.");
        }

        return this.toBuilder()
                .creditLimit(newLimit)
                .availableLimit(newAvailableLimit)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
