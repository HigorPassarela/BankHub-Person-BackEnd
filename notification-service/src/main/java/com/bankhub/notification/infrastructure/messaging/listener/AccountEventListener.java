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

            log.info("Acionando caso de uso para a Conta: {} | Evento: {}", accountId, eventType);

            sendNotificationUseCase.execute(accountId, eventType, status);
        } catch (Exception e) {
            log.error("Erro inesperado ao processar evento Kafka para notificação: {}", e.getMessage(), e);
            // O Spring Kafka lida com o offset (acknowledgment). Em caso de erro aqui,
            // a estratégia de retentativa padrão do contêiner do Kafka entra em ação.
        }
    }
}
