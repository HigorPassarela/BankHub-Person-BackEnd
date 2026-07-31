package com.bankhub.investment.application.port.in;

import com.bankhub.investment.domain.Portfolio;

/**
 * Porta de entrada para consulta consolidada da carteira de ativos do cliente.
 */
public interface GetPortfolioUseCase {

    /**
     * Busca a carteira de investimentos. Se não existir, retorna uma carteira vazia.
     *
     * @param customerId ID do cliente logado.
     * @return A raiz de agregação Portfolio.
     */
    Portfolio execute(String customerId);
}
