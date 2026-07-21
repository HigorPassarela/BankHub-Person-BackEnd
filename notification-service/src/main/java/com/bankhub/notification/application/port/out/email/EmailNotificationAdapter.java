package com.bankhub.notification.application.port.out.email;

import com.bankhub.notification.application.port.out.EmailNotificationPort;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationAdapter implements EmailNotificationPort {

    private final JavaMailSender mailSender;

    private static final String FROM_ADDRESS = "noreply@bankhub.com";

    @Override
    @Retry(name = "emailRetry", fallbackMethod = "fallbackEmail")
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        log.info("Preparando envio de e-mail HTML via SMTP para: {}", to);

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM_ADDRESS);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);

            log.info("E-mail HTML enviado com sucesso para: {}", to);
        } catch (Exception e) {
            log.error("Erro ao formatar o e-mail HTML para {}. Motivo: {}", to, e.getMessage());
            throw new RuntimeException("Falha na formatação MIME do E-mail", e);
        }
    }

    /**
     * Fallback executado pelo Resilience4j em caso de indisponibilidade extrema do SMTP.
     */
    public void fallbackEmail(String to, String subject, String htmlBody, Exception ex) {
        log.error("CRÍTICO: Falha definitiva ao enviar e-mail para {}. Assunto: {}. Motivo: {}",
                to, subject, ex.getMessage());
    }
}
