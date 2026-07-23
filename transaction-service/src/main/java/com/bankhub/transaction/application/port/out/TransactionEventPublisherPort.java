package com.bankhub.transaction.application.port.out;

import com.bankhub.transaction.domain.Transaction;

/**
 * Porta de saída para a publicação assíncrona de eventos transacionais (Saga).
 */
public interface TransactionEventPublisherPort {
    /**
     * Informa ao ecossistema que uma transação (PIX) iniciou e aguarda liquidação de saldo.
     *
     * @param transaction A transação recém salva no Ledger (Status PENDING).
     */
    void publishTransactionInitiatedEvent(Transaction transaction);
}
