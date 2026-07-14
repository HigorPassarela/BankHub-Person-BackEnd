package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Account;

/**
 * Porta de entrada (Caso de Uso) para a consulta de contas bancárias.
 */
public interface FindAccountUseCase {

    /**
     * Busca uma conta no sistema validando a posse do cliente.
     *
     * @param accountId ID da conta desejada.
     * @param customerId ID do cliente que está solicitando a busca (Contexto de Segurança).
     * @return A entidade de Domínio 'Account'.
     * @throws RuntimeException (ou exceção de domínio futura) se a conta não existir ou não pertencer ao cliente.
     */
    Account execute (String accountId, String customerId);
}
