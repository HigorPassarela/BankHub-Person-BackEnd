package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Account;

/**
 * Porta de entrada (Caso de Uso) para a criação de uma nova conta bancária.
 */
public interface CreateAccountUseCase {

    /**
     * Orquestra a criação de uma nova conta para o cliente informado.
     *
     * @param customerId ID do cliente que será o titular da conta.
     * @return A entidade de Domínio 'Account' recém-criada e persistida.
     */
    Account execute(String customerId);
    
}
