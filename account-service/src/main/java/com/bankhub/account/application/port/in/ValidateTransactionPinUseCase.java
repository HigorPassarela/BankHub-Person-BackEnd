package com.bankhub.account.application.port.in;

/**
 * Porta de entrada (Caso de Uso) para validar a assinatura eletrônica (PIN) do cliente antes de uma transação.
 */
public interface ValidateTransactionPinUseCase {

    /**
     * Confirma se o PIN fornecido corresponde ao Hash armazenado no banco de dados.
     *
     * @param accountId ID interno da conta.
     * @param customerId ID do cliente logado (Garante que a conta pertence a ele).
     * @param plainPin O PIN de 4 dígitos enviado em texto plano pelo Front-end.
     * @return Retorna TRUE se for válido. Caso contrário, deve estourar exceção.
     */
    boolean execute(String accountId, String customerId, String plainPin);
}
