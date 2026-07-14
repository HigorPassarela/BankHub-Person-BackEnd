package com.bankhub.notification.application.port.out;

/**
 * Porta de saída para o envio de notificações por e-mail.
 */
public interface EmailNotificationPort {

    /**
     * Envia um e-mail de texto simples.
     *
     * @param to Endereço de destino.
     * @param subject Assunto do e-mail.
     * @param body Corpo da mensagem.
     */
    void sendEmail(String to, String subject, String body);
}
