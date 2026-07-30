package com.bankhub.transaction.application.port.in;

import com.bankhub.transaction.domain.Transaction;

import java.util.List;

/**
 * Porta de entrada (Caso de Uso) para buscar o extrato (histórico) do cliente.
 */
public interface GetStatementUseCase {
    List<Transaction> execute(String accountId);
}
