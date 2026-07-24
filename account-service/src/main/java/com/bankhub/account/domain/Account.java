package com.bankhub.account.domain;

import com.bankhub.account.domain.exception.InsufficientFundsException;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record Account(
        String id,
        String customerId,
        AccountNumber accountNumber,
        Balance balance,
        AccountStatus status,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public Account activate() {
        return Account.builder()
                .id(this.id)
                .customerId(this.customerId)
                .accountNumber(this.accountNumber)
                .balance(this.balance)
                .status(AccountStatus.ACTIVE)
                .version(this.version)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Account block() {
        return Account.builder()
                .id(this.id)
                .customerId(this.customerId)
                .accountNumber(this.accountNumber)
                .balance(this.balance)
                .status(AccountStatus.BLOCKED)
                .version(this.version)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Account credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de crédito deve ser maior que zero.");
        }

        Balance newBalance = new Balance(this.balance.amount().add(amount), this.balance.currency());

        return Account.builder()
                .id(this.id)
                .customerId(this.customerId)
                .accountNumber(this.accountNumber)
                .balance(newBalance)
                .status(this.status)
                .version(this.version)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Account debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de débito deve ser maior que zero.");
        }

        if (this.balance.amount().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Saldo insuficiente para realizar esta operação.");
        }

        Balance newBalance = new Balance(this.balance.amount().subtract(amount), this.balance.currency());

        return Account.builder()
                .id(this.id)
                .customerId(this.customerId)
                .accountNumber(this.accountNumber)
                .balance(newBalance)
                .status(this.status)
                .version(this.version)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}