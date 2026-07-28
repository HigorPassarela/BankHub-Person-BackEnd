package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Account;

/**
 * Porta de entrada (Caso de Uso) para a ativação de uma conta usando Magic Link.
 */
public interface ActivateAccountUseCase {

    /**
     * Valida um Token Efêmero (Magic Link) e ativa a conta vinculada a ele.
     *
     * @param activationToken Token de ativação de 24h gerado no momento do onboarding.
     * @return A entidade de Domínio 'Account' atualizada com o novo status.
     */
    Account execute(String activationToken);

}