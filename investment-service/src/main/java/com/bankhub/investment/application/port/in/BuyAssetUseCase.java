package com.bankhub.investment.application.port.in;

import com.bankhub.investment.domain.Portfolio;

import java.math.BigDecimal;

/**
 * Porta de entrada para a operação de compra de ativos financeiros na bolsa/mercado.
 */
public interface BuyAssetUseCase {

    /**
     * Orquestra a compra de um ativo, desde o débito na conta até a consolidação na carteira.
     *
     * @param customerId ID do cliente logado (Segurança BBA).
     * @param accountId ID da conta que proverá os fundos.
     * @param ticker O código do ativo (Ex: PETR4).
     * @param type O tipo do ativo (Ex: STOCK).
     * @param quantity Quantas cotas o cliente quer comprar.
     * @param jwtToken O token criptografado para propagação (Zero Trust M2M).
     * @return O Portfólio atualizado do cliente.
     */
    Portfolio execute(String customerId, String accountId, String ticker, String type, BigDecimal quantity, String jwtToken);
}
