package com.bankhub.investment.domain;

import java.math.BigDecimal;

/**
 * Value Object que representa um ativo financeiro específico na posse do cliente.
 */
public record Asset(
        String ticker,       // Código na Bolsa ou nome do CDB (Ex: "PETR4", "CDB_100_CDI")
        AssetType type,      // O tipo de investimento (STOCK, CDB, FII)
        BigDecimal quantity, // Quantidade de cotas compradas
        BigDecimal averagePrice // Preço médio pago na hora da compra
) {
    public Asset {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("O Ticker do ativo não pode ser nulo ou vazio.");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A quantidade comprada deve ser maior que zero.");
        }
        if (averagePrice == null || averagePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço médio de compra não pode ser negativo.");
        }
        ticker = ticker.toUpperCase().trim();
    }
}
