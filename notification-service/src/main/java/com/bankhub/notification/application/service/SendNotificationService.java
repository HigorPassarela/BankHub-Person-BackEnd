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
    public void execute(String accountId, String eventType, String status, String agency, String accountNumber) {
        log.info("Orquestrando notificação em HTML para a conta: {}, Evento: {}", accountId, eventType);

        String customerEmail = "cliente-" + accountId + "@bankhub.local";
        String subject = formatSubject(eventType);

        String htmlBody = formatHtmlBody(accountId, eventType, status, agency, accountNumber);

        emailPort.sendHtmlEmail(customerEmail, subject, htmlBody);

        log.info("Notificação HTML orquestrada e enviada à porta de saída para: {}", customerEmail);
    }

    /**
     * Utiliza Pattern Matching do Java 21 para definir o assunto do e-mail.
     */
    private String formatSubject(String eventType) {
        return switch (eventType) {
            case "ACCOUNT_CREATED" -> "Bem-vindo ao Bank-Hub! Finalize sua abertura de conta \uD83C\uDF89";
            case "ACCOUNT_BLOCKED" -> "ALERTA: Sua conta foi bloqueada \uD83D\uDEA8";
            default -> "Atualização importante na sua conta Bank-Hub";
        };
    }

    /**
     * Template HTML responsivo e com design corporativo de banco digital.
     * Incorpora a visão da Season 2 (Botão para criar senha).
     */
    private String formatHtmlBody(String accountId, String eventType, String status, String agency, String accountNumber) {
        String activationLink = "http://bank-hub.com/ativar/" + accountId;

        return String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: #ffffff; padding: 40px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }
                    .header { text-align: center; border-bottom: 2px solid #6c5ce7; padding-bottom: 20px; margin-bottom: 20px; }
                    .header h1 { color: #6c5ce7; margin: 0; font-size: 28px; letter-spacing: -1px; }
                    .content { color: #333333; line-height: 1.6; font-size: 16px; }
                    .account-box { background-color: #f8f9fa; border-left: 4px solid #00b894; padding: 15px; margin: 25px 0; border-radius: 0 4px 4px 0; }
                    .button { display: block; width: 220px; margin: 30px auto; padding: 15px 0; text-align: center; background-color: #6c5ce7; color: #ffffff; text-decoration: none; font-weight: bold; border-radius: 50px; font-size: 16px; transition: background-color 0.3s; }
                    .footer { margin-top: 40px; text-align: center; font-size: 12px; color: #999999; border-top: 1px solid #eeeeee; padding-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Bank-Hub</h1>
                    </div>
                    <div class="content">
                        <p>Olá, futuro cliente Bank-Hub!</p>
                        <p>É com grande alegria que informamos que o seu processo de análise foi aprovado e a sua conta acaba de nascer no nosso ecossistema!</p>
                        
                        <div class="account-box">
                            <strong>Agência:</strong> %s <br>
                            <strong>Número da Conta:</strong> %s <br>
                            <strong>Status Atual:</strong> <span style="color: #ff9f43; font-weight: bold;">%s</span> <br>
                            <strong>Ação do Sistema:</strong> %s
                        </div>
                        
                        <p>Para começarmos a investir na Bolsa de Valores, comprar CDBs e realizar PIX ilimitados, precisamos apenas do último passo de segurança.</p>
                        
                        <a href="%s" class="button">Criar Minha Senha</a>
                        
                        <p>Caso o botão acima não funcione, copie e cole este link no seu navegador: <br> <a href="%s" style="color: #6c5ce7; word-break: break-all;">%s</a></p>
                    </div>
                    <div class="footer">
                        <p>Se você não solicitou a abertura desta conta, por favor ignore este e-mail.</p>
                        <p>&copy; 2026 Bank-Hub Ecossistema Digital. Todos os direitos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """, agency, accountNumber, status, eventType, activationLink, activationLink, activationLink);
    }
}
