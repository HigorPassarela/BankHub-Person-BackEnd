package com.bankhub.transaction.application.port.out;

import com.bankhub.transaction.domain.Transaction;

import java.util.Optional;

/**
 * Porta de saída (Outbound Port) para operações de persistência do Livro-Razão.
 */
public interface TransactionPersistencePort {

    /**
     * Salva ou atualiza uma transação no Ledger.
     */
    Transaction save(Transaction transaction);

    /**
     * Busca uma transação específica pelo ID.
     */
    Optional<Transaction> findById(String transactionId);
}
