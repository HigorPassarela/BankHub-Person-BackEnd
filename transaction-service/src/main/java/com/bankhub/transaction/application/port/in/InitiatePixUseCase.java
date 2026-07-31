package com.bankhub.transaction.application.port.in;

import com.bankhub.transaction.domain.Transaction;

import java.math.BigDecimal;

/**
 * Porta de entrada (Caso de Uso) para iniciar uma transferência PIX entre contas.
 */
public interface InitiatePixUseCase {

    /**
     * Inicia a etapa 1 da Saga de Transferência.
     *
     * @param sourceAccountId      ID do cliente remetente (Dono do dinheiro).
     * @param destinationAccountId ID da conta recebedora.
     * @param amount               Quantia a ser transferida.
     * @param transactionPin       Senha transacional de 4 dígitos exigida pelo Frontend (NOVO).
     * @return A transação registrada no Ledger como PENDENTE.
     * @Param customerId           ID do customer
     */
    Transaction execute(String customerId, String sourceAccountId, String destinationAccountId, BigDecimal amount, String transactionPin, String category);
}