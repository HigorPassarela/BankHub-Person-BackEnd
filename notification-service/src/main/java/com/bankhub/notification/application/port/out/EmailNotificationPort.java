package com.bankhub.notification.application.port.out;

/**
 * Porta de saída para o envio de notificações por e-mail (Suporte a HTML).
 */
public interface EmailNotificationPort {

    /**
     * Envia um e-mail rico formatado em HTML.
     *
     * @param to Endereço de destino.
     * @param subject Assunto do e-mail.
     * @param htmlBody Corpo da mensagem com tags HTML e CSS inline.
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);

}