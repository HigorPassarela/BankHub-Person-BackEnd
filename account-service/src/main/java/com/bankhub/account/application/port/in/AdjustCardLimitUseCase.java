package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Card;

import java.math.BigDecimal;

/**
 * Porta de entrada (Caso de Uso) para o ajuste do limite de crédito do cartão.
 */
public interface AdjustCardLimitUseCase {

    /**
     * Ajusta o limite de crédito e o limite disponível de um cartão específico.
     *
     * @param accountId  ID da conta corrente.
     * @param cardId     ID do cartão que sofrerá o reajuste.
     * @param customerId ID do cliente logado (Segurança).
     * @param newLimit   Novo valor estabelecido pelo usuário.
     * @return O Cartão atualizado.
     */
    Card execute(String accountId, String cardId, String customerId, BigDecimal newLimit);
}
