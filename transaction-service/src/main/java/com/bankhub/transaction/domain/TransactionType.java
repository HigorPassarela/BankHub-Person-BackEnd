package com.bankhub.transaction.domain;

/**
 * Representa a natureza e a direção de uma movimentação financeira no Bank-Hub.
 */
public enum TransactionType {
    PIX_OUT,          // Saída de dinheiro (Débito)
    PIX_IN,           // Entrada de dinheiro (Crédito)
    INTERNAL_TRANSFER
}
