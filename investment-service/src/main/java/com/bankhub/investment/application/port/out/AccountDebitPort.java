package com.bankhub.investment.application.port.out;

import java.math.BigDecimal;

/**
 * Porta de saída para operações financeiras inter-serviços (Débito em conta corrente).
 */
public interface AccountDebitPort {

    /**
     * Tenta descontar fundos do cliente no serviço central de contas.
     *
     * @param accountId ID da conta a ser debitada.
     * @param customerId ID do cliente (Dono da conta).
     * @param jwtToken O token criptografado para propagar a segurança Zero Trust.
     * @param amount Quantia a ser cobrada.
     * @throws RuntimeException (Sinalizando recusa se a conta estiver sem saldo ou bloqueada).
     */
    void debitFunds(String accountId, String customerId, String jwtToken, BigDecimal amount);

    /**
     * Devolve o dinheiro para a conta corrente do cliente em caso de erro na compra do ativo.
     */
    void refundFunds(String accountId, String customerId, String jwtToken, BigDecimal amount);
}
