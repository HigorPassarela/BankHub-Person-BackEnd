package com.bankhub.transaction.application.port.out.messaging;

import com.bankhub.transaction.application.port.out.TransactionEventPublisherPort;
import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.infrastructure.messaging.dto.TransactionEventMessage;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionPublisherAdapter implements TransactionEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "bankhub.transaction.events";

    @Override
    @Retry(name = "kafkaRetry", fallbackMethod = "fallbackPublish")
    public void publishTransactionInitiatedEvent(Transaction transaction) {
        log.info("Preparando evento Kafka para a Transação [{}] (Saga Initiated)", transaction.id());

        TransactionEventMessage message = TransactionEventMessage.builder()
                .header(TransactionEventMessage.Header.builder()
                        .correlationId(UUID.randomUUID().toString())
                        .eventType("TRANSACTION_INITIATED")
                        .timestamp(LocalDateTime.now())
                        .build())
                .payload(TransactionEventMessage.Payload.builder()
                        .transactionId(transaction.id())
                        .sourceAccountId(transaction.sourceAccountId())
                        .destinationAccountId(transaction.destinationAccountId())
                        .amount(transaction.amount())
                        .type(transaction.type().name())
                        .status(transaction.status().name())
                        .build())
                .build();

        kafkaTemplate.send(TOPIC, transaction.sourceAccountId(), message);

        log.info("Evento TRANSACTION_INITIATED enviado com sucesso. ID Transação: {}", transaction.id());
    }

    public void fallbackPublish(Transaction transaction, Exception ex) {
        log.error("CRÍTICO: Falha ao publicar evento da Transação {}. O motor do PIX pode travar. Motivo: {}",
                transaction.id(), ex.getMessage());
    }
}
