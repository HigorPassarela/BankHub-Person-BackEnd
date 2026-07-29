package com.bankhub.account.application.port.in;

/**
 * Porta de entrada para a exibição protegida da senha do cartão físico (Reveal PIN).
 */
public interface RevealCardPinUseCase {

    /**
     * Valida a senha transacional do cliente e retorna a senha do cartão físico em texto plano.
     *
     * @param accountId ID da conta corrente.
     * @param cardId ID do cartão físico.
     * @param customerId ID do cliente logado (Validação Zero Trust).
     * @param transactionPin A assinatura eletrônica de 4 dígitos para liberar a ação.
     * @return A senha do cartão físico (4 dígitos) em texto plano.
     */
    String execute(String accountId, String cardId, String customerId, String transactionPin);

}