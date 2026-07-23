package com.bankhub.account.infrastructure.messaging.listener;

import com.bankhub.account.application.port.in.ProcessPixUseCase;
import com.bankhub.account.infrastructure.messaging.dto.TransactionEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PixEventListener {

    private final ProcessPixUseCase processPixUseCase;

    private static final String TOPIC = "bankhub.transaction.events";
    private static final String CONSUMER_GROUP = "account-group";

    @KafkaListener(topics =  TOPIC, groupId = CONSUMER_GROUP)
    public void handleTransactionEvent(TransactionEventMessage message) {
        log.info("Evento consumido do Kafka. Topic: {}, CorrelationId: {}",
                TOPIC, message.header().correlationId());

        try {
            String eventType = message.header().eventType();

            if ("TRANSACTION_INITIATED".equals(eventType)) {
                log.info("Processando etapa 2 da Saga de Transferência. ID da Transação: {}",
                        message.payload().transactionId());

                processPixUseCase.execute(
                        message.payload().transactionId(),
                        message.payload().sourceAccountId(),
                        message.payload().destinationAccountId(),
                        message.payload().amount()
                );
            }
        } catch (Exception e) {
            log.error("Erro inesperado ao processar evento de PIX no Account Service: {}", e.getMessage(), e);
        }
    }
}
