package com.bankhub.notification.infrastructure.messaging.listener;

import com.bankhub.notification.application.port.in.SendNotificationUseCase;
import com.bankhub.notification.infrastructure.messaging.dto.AccountEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventListener {

    private final SendNotificationUseCase sendNotificationUseCase;

    private static final String TOPIC = "bankhub.account.events";
    private static final String CONSUMER_GROUP = "notification-group";

    @KafkaListener(topics = TOPIC, groupId = CONSUMER_GROUP)
    public void handleAccountEvent(AccountEventMessage message) {
        log.info("Evento consumido do Kafka. Topic: {}, CorrelationId: {}",
                TOPIC, message.header().correlationId());

        try {
            String eventType = message.header().eventType();
            String accountId = message.payload().accountId();
            String status = message.payload().status();

            String agency = message.payload().agency();
            String accountNumber = message.payload().accountNumber();

            log.info("Acionando caso de uso para a Conta: {} | Evento: {}", accountId, eventType);

            sendNotificationUseCase.execute(accountId, eventType, status, agency, accountNumber);

        } catch (Exception e) {
            log.error("Erro inesperado ao processar evento Kafka para notificação: {}", e.getMessage(), e);
        }
    }
}
