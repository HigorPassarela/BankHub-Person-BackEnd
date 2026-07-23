package com.bankhub.account.application.port.in;

import java.math.BigDecimal;

/**
 * Porta de entrada para a liquidação de um PIX no motor de contas.
 */
public interface ProcessPixUseCase {
    /**
     * Processa a retirada de dinheiro de uma conta e o envio para outra.
     *
     * @param transactionId ID da transação no Ledger.
     * @param sourceAccountId Conta remetente.
     * @param destinationAccountId Conta recebedora.
     * @param amount Quantia a ser movimentada.
     */
    void execute(String transactionId, String sourceAccountId, String destinationAccountId, BigDecimal amount);

}
