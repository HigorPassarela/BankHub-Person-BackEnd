package com.bankhub.account.domain.event;

/**
 * Evento de domínio disparado após a tentativa de processar um PIX no sistema de contas.
 *
 * @param transactionId ID da transação no Ledger.
 * @param sagaStatus    Status resultante (COMPLETED ou FAILED).
 * @param failureReason Motivo do erro (se houver).
 */
public record PixProcessedEvent(
        String transactionId,
        String sagaStatus,
        String failureReason
) {
}
