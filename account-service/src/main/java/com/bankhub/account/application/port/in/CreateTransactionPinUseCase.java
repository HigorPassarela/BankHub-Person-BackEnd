package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Account;

/**
 * Porta de entrada para a criação ou alteração do PIN de transação (4 dígitos).
 */
public interface CreateTransactionPinUseCase {

    /**
     * Define a senha transacional exigida para movimentações (PIX, Compras, etc).
     *
     * @param accountId  ID interno da conta.
     * @param customerId ID do cliente logado (Segurança Zero Trust).
     * @param plainPin   O PIN de 4 dígitos em texto plano (Será hasheado).
     * @return A conta atualizada.
     */
    Account execute(String accountId, String customerId, String plainPin);
}
