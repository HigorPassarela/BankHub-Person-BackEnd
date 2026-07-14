package com.bankhub.account.domain;

import java.math.BigDecimal;

public record Balance(BigDecimal amount, String currency) {

    public Balance {
        if (amount == null) {
            throw new IllegalArgumentException("O valor do saldo não pode ser nulo");
        }
        if (currency == null || currency.isBlank()) {
            currency = "BRL";
        }
    }

    public static Balance zero() {
        return new Balance(BigDecimal.ZERO, "BRL");
    }
}
