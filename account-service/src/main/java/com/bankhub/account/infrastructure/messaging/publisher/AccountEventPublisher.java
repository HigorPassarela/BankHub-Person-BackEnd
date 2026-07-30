package com.bankhub.account.infrastructure.messaging.publisher;

import com.bankhub.account.domain.event.AccountCreatedEvent;
import com.bankhub.account.domain.event.PixProcessedEvent;
import com.bankhub.account.infrastructure.messaging.dto.AccountEventMessage;
import com.bankhub.account.infrastructure.messaging.dto.SagaReplyMessage;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "bankhub.account.events";
    private static final String SAGA_REPLY_TOPIC = "bankhub.transaction.replies";

    /**
     * Ouve o evento interno disparado pelo Facade APÓS o commit no banco de dados.
     */
    @EventListener
    @Retry(name = "kafkaRetry", fallbackMethod = "fallbackPublish")
    public void handleAccountCreatedEvent(AccountCreatedEvent event) {
        log.info("Preparando envio de evento Kafka para a conta: {}", event.account().id());

        String agency = event.account().accountNumber() != null ? event.account().accountNumber().agency() : "N/A";
        String number = event.account().accountNumber() != null ? event.account().accountNumber().number() : "N/A";

        AccountEventMessage message = AccountEventMessage.builder()
                .header(AccountEventMessage.Header.builder()
                        .correlationId(UUID.randomUUID().toString())
                        .eventType("ACCOUNT_CREATED")
                        .timestamp(LocalDateTime.now())
                        .build())
                .payload(AccountEventMessage.Payload.builder()
                        .accountId(event.account().id())
                        .status(event.account().status().name())
                        .agency(agency)
                        .accountNumber(number)
                        .activationToken(event.activationToken())
                        .build())
                .build();

        kafkaTemplate.send(TOPIC, event.account().id(), message);

        log.info("Evento ACCOUNT_CREATED enviado com sucesso ao tópico {} para a conta {}", TOPIC, event.account().id());
    }

    @EventListener
    @Retry(name = "kafkaRetry", fallbackMethod = "fallbackSagaReply")
    public void handlePixProcessedEvent(PixProcessedEvent event) {
        log.info("Preparando Saga Reply Kafka para a Transação [{}] -> Status: {}", event.transactionId(), event.sagaStatus());

        SagaReplyMessage replyMessage = SagaReplyMessage.builder()
                .header(SagaReplyMessage.Header.builder()
                        .correlationId(UUID.randomUUID().toString())
                        .eventType("PIX_PROCESSED_REPLY")
                        .timestamp(LocalDateTime.now())
                        .build())
                .payload(SagaReplyMessage.Payload.builder()
                        .transactionId(event.transactionId())
                        .sagaStatus(event.sagaStatus())
                        .failureReason(event.failureReason() != null ? event.failureReason() : "OK")
                        .build())
                .build();

        kafkaTemplate.send(SAGA_REPLY_TOPIC, event.transactionId(), replyMessage);

        log.info("Saga Reply enviado com sucesso ao tópico {}", SAGA_REPLY_TOPIC);
    }

    public void fallbackPublish(AccountCreatedEvent event, Exception ex) {
        log.error("CRÍTICO: Falha ao enviar evento Kafka após retentativas. Conta ID: {}. Motivo: {}",
                event.account().id(), ex.getMessage());
    }

    public void fallbackSagaReply(PixProcessedEvent event, Exception ex) {
        log.error("CRÍTICO: Falha ao enviar SAGA REPLY para a Transação {}. O motor de PIX não será notificado! Motivo: {}",
                event.transactionId(), ex.getMessage());
    }
}
