package com.bankhub.account.infrastructure.messaging.publisher;

import com.bankhub.account.domain.event.AccountCreatedEvent;
import com.bankhub.account.infrastructure.messaging.dto.AccountEventMessage;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "bankhub.account.events";

    /**
     * Ouve o evento interno disparado pelo Facade APÓS o commit no banco de dados.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retry(name = "kafkaRetry", fallbackMethod = "fallbackPublish")
    public void handleAccountCreatedEvent(AccountCreatedEvent event) {
        log.info("Preparando envio de evento Kafka para a conta: {}", event.account().id());

        AccountEventMessage message = AccountEventMessage.builder()
                .header(AccountEventMessage.Header.builder()
                        .correlationId(UUID.randomUUID().toString())
                        .eventType("ACCOUNT_CREATED")
                        .timestamp(LocalDateTime.now())
                        .build())
                .payload(AccountEventMessage.Payload.builder()
                        .accountId(event.account().id())
                        .status(event.account().status().name())
                        .build())
                .build();

        kafkaTemplate.send(TOPIC, event.account().id(), message);

        log.info("Evento ACCOUNT_CREATED enviado com sucesso ao tópico {} para a conta {}", TOPIC, event.account().id());
    }

    /**
     * Fallback executado pelo Resilience4j caso o Kafka esteja inoperante.
     */
    public void fallbackPublish(AccountCreatedEvent event, Exception ex) {
        log.error("CRÍTICO: Falha ao enviar evento Kafka após retentativas. Conta ID: {}. Motivo: {}",
                event.account().id(), ex.getMessage());
        // Em um ambiente de produção avançado, aqui salvaríamos em uma tabela 'Outbox' no Mongo
        // para uma rotina de repescagem tentar enviar depois.
    }
}
