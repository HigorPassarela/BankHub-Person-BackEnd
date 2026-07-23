package com.bankhub.transaction.domain;

/**
 * Representa o estado atual de uma transação no padrão Saga Coreografada/Orquestrada.
 */
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED
}
