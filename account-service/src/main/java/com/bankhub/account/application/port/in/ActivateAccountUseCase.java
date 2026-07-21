package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Account;

/**
 * Porta de entrada (Caso de Uso) para a ativação de uma conta pendente.
 */
public interface ActivateAccountUseCase {

    /**
     * Valida e ativa uma conta bancária que estava pendente de confirmação.
     *
     * @param accountId  ID da conta a ser ativada.
     * @param customerId ID do cliente titular (Para validação de segurança BBA).
     * @return A entidade de Domínio 'Account' atualizada com o novo status.
     */
    Account execute(String accountId, String customerId);
}
