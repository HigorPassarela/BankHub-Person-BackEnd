package com.bankhub.notification.application.port.in;

/**
 * Porta de entrada (Caso de Uso) para o disparo de notificações no ecossistema Bank-Hub.
 */
public interface SendNotificationUseCase {

    /**
     * Orquestra o envio de uma notificação com base em um evento de domínio da conta.
     *
     * @param accountId ID da conta que sofreu a alteração.
     * @param eventType O tipo de evento ocorrido (ex: ACCOUNT_CREATED).
     * @param status O novo status da conta (ex: ACTIVE).
     */
    void execute(String accountId, String eventType, String status);
}
