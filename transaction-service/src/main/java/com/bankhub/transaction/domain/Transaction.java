package com.bankhub.transaction.domain;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record Transaction(
        String id,
        String sourceAccountId,
        String destinationAccountId,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public Transaction {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser estritamente maior que zero.");
        }
    }

    public Transaction complete() {
        return Transaction.builder()
                .id(this.id)
                .sourceAccountId(this.sourceAccountId)
                .destinationAccountId(this.destinationAccountId)
                .amount(this.amount)
                .type(this.type)
                .status(TransactionStatus.COMPLETED)
                .failureReason(null)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Transaction fail(String reason) {
        return Transaction.builder()
                .id(this.id)
                .sourceAccountId(this.sourceAccountId)
                .destinationAccountId(this.destinationAccountId)
                .amount(this.amount)
                .type(this.type)
                .status(TransactionStatus.FAILED)
                .failureReason(reason)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
