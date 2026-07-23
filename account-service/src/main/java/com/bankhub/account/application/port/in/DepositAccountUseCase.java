package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Account;

import java.math.BigDecimal;

/**
 * Porta de entrada (Caso de Uso) para operações de Cash-In (Depósito).
 */
public interface DepositAccountUseCase {

    /**
     * Adiciona fundos a uma conta existente e ativa.
     *
     * @param accountId  ID da conta a receber o depósito.
     * @param customerId ID do usuário autenticado (Validação BBA).
     * @param amount     Quantia a ser depositada.
     * @return A entidade Account com o saldo atualizado.
     */
    Account execute(String accountId, String customerId, BigDecimal amount);
}
