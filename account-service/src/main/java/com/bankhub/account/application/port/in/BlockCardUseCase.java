package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Card;

/**
 * Porta de entrada (Caso de Uso) para o bloqueio e desbloqueio temporário de cartões.
 */
public interface BlockCardUseCase {

    /**
     * Alterna o status de bloqueio de um cartão específico.
     *
     * @param accountId  ID da conta (Garante vínculo).
     * @param cardId     ID do cartão que será modificado.
     * @param customerId ID do cliente logado (Segurança Zero Trust).
     * @return O Cartão com seu status atualizado.
     */
    Card execute(String accountId, String cardId, String customerId);
}
