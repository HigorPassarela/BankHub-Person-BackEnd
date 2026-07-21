package com.bankhub.notification.application.port.in;

/**
 * Porta de entrada (Caso de Uso) para o disparo de notificações no ecossistema Bank-Hub.
 */
public interface SendNotificationUseCase {

    /**
     * Orquestra o envio de uma notificação com base em um evento de domínio da conta.
     *
     * @param accountId ID da conta (Mongo).
     * @param eventType O tipo de evento ocorrido (ex: ACCOUNT_CREATED).
     * @param status O novo status da conta (ex: PENDING_ACTIVATION).
     * @param agency Agência da conta.
     * @param accountNumber Número amigável da conta.
     */
    void execute(String accountId, String eventType, String status, String agency, String accountNumber);

}