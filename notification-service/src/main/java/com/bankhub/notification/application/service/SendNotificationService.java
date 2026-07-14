package com.bankhub.notification.application.service;

import com.bankhub.notification.application.port.in.SendNotificationUseCase;
import com.bankhub.notification.application.port.out.EmailNotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendNotificationService implements SendNotificationUseCase {

    private final EmailNotificationPort emailPort;

    @Override
    public void execute(String accountId, String eventType, String status) {
        log.info("Orquestrando notificação para a conta: {}, Evento: {}", accountId, eventType);

        String customerEmail = "cliente-" + accountId + "@bankhub.local";

        String subject = formatSubject(eventType);
        String body = formatBody(accountId, eventType, status);

        emailPort.sendEmail(customerEmail, subject, body);

        log.info("Notificação orquestrada e enviada à porta de saída para o e-mail: {}", customerEmail);
    }

    /**
     * Utiliza Pattern Matching do Java 21 para definir o assunto do e-mail.
     */
    private String formatSubject(String eventType) {
        return switch (eventType) {
            case "ACCOUNT_CREATED" -> "Bem-vindo ao Bank-Hub! Sua conta foi criada.";
            case "ACCOUNT_BLOCKED" -> "ALERTA DE SEGURANÇA: Sua conta foi bloqueada.";
            default -> "Atualização importante na sua conta Bank-Hub.";
        };
    }

    /**
     * Formata o corpo da mensagem.
     */
    private String formatBody(String accountId, String eventType, String status) {
        return String.format("""
                Olá!
                
                Registramos uma atualização no nosso sistema para a sua conta (ID: %s).
                
                Evento ocorrido: %s
                Status atual da conta: %s
                
                Se você não reconhece esta ação, entre em contato com nosso suporte imediatamente.
                
                Atenciosamente,
                Equipe Bank-Hub.
                """, accountId, eventType, status);
    }
}
