package com.bankhub.account.infrastructure.messaging.listener;

import com.bankhub.account.application.port.in.DepositAccountUseCase;
import com.bankhub.account.infrastructure.messaging.dto.RefundCommandMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundCommandListener {

    private final DepositAccountUseCase depositAccountUseCase;

    private static final String COMMAND_TOPIC = "bankhub.account.commands";
    private static final String CONSUMER_GROUP = "account-refund-group";

    @KafkaListener(
            topics = COMMAND_TOPIC,
            groupId = CONSUMER_GROUP,
            properties = {"spring.json.value.default.type=com.bankhub.account.infrastructure.messaging.dto.RefundCommandMessage"}
    )
    public void handleRefundCommand(RefundCommandMessage message) {
        try {
            if (message.header() != null && "REFUND_ACCOUNT_COMMAND".equals(message.header().eventType())) {

                String accountId = message.payload().accountId();
                String customerId = message.payload().customerId();
                BigDecimal amount = message.payload().amount();

                log.warn("Comando de Estorno (Saga Compensation) recebido do Kafka. Conta: {}, Valor: {}", accountId, amount);

                depositAccountUseCase.execute(accountId, customerId, amount);

                log.info("Estorno concluído com sucesso via Kafka. O saldo da conta {} foi restaurado.", accountId);
            }
        } catch (Exception e) {
            log.error("Erro CRÍTICO ao processar estorno via Kafka para a mensagem {}: {}",
                    message.header().correlationId(), e.getMessage(), e);
            throw e;
        }
    }
}
