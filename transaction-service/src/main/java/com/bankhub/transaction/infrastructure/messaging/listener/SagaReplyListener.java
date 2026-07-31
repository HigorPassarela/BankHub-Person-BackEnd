package com.bankhub.transaction.infrastructure.messaging.listener;

import com.bankhub.transaction.application.port.in.CompletePixUseCase;
import com.bankhub.transaction.infrastructure.messaging.dto.SagaReplyMessage;
import com.bankhub.transaction.infrastructure.messaging.dto.TransactionEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaReplyListener {

    private final CompletePixUseCase completePixUseCase;

    private static final String SAGA_REPLY_TOPIC = "bankhub.transaction.replies";
    private static final String CONSUMER_GROUP = "transaction-group";

    @KafkaListener(topics = SAGA_REPLY_TOPIC, groupId = CONSUMER_GROUP)
    public void handleSagaReply(SagaReplyMessage message) {
        log.info("Saga Reply recebido do Kafka. Topic: {}", SAGA_REPLY_TOPIC);

        try {
            String eventType = message.header().eventType();

            if ("PIX_PROCESSED_REPLY".equals(eventType)) {
                String transactionId = message.payload().transactionId();
                String finalStatus = message.payload().sagaStatus();
                String failureReason = message.payload().failureReason();

                log.info("Processando fechamento da Saga PIX. Transação: {} | Status Final: {}", transactionId, finalStatus);

                completePixUseCase.execute(transactionId, finalStatus, failureReason);
            }
        } catch (Exception e) {
            log.error("Erro inesperado ao processar o Saga Reply do PIX: {}", e.getMessage(), e);
        }
    }
}
