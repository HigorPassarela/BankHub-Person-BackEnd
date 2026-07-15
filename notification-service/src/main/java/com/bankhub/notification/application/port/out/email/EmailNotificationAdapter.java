package com.bankhub.notification.application.port.out.email;

import com.bankhub.notification.application.port.out.EmailNotificationPort;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationAdapter implements EmailNotificationPort {

    private final JavaMailSender mailSender;

    private static final String FROM_ADDRESS = "noreply@bankhub.com";

    @Override
    @Retry(name = "emailRetry", fallbackMethod = "fallbackEmail")
    public void sendEmail(String to, String subject, String body) {
        log.info("Preparando envio de e-mail via SMTP para: {}", to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_ADDRESS);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        log.info("E-Mail enviado com sucesso para: {}", to);
    }

    /**
     * Fallback executado pelo Resilience4j caso o servidor SMTP esteja fora do ar.
     */
    public void fallbackEmail(String to, String subject, String body, Exception ex) {
        log.error("CRÍTICO: Falha definitiva ao enviar e-mail para {}. Assunto: {}. Motivo: {}",
                to, subject, ex.getMessage());
        // Em um sistema robusto, poderíamos persistir esta mensagem em uma "Dead Letter Queue" (DLQ)
        // ou tabela de retentativa para que não seja perdida para sempre.
    }
}
