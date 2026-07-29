package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Card;

/**
 * Porta de entrada para a emissão de um novo cartão.
 */
public interface GenerateCardUseCase {

    /**
     * Orquestra a geração segura de um novo cartão bancário.
     *
     * @param accountId   ID da conta corrente.
     * @param customerId  ID do cliente logado (Validação Zero Trust).
     * @param cardType    Tipo do cartão (PHYSICAL, VIRTUAL, TEMPORARY).
     * @param physicalPin Opcional: Senha de 4 dígitos da maquininha (Obrigatório apenas para PHYSICAL).
     * @return O Cartão gerado (Com o PAN real exposto apenas na primeira resposta).
     */
    Card execute(String accountId, String customerId, String cardType, String physicalPin);
}
