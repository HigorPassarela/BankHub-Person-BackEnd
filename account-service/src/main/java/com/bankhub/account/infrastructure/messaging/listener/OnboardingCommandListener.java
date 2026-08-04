package com.bankhub.account.infrastructure.messaging.listener;

import com.bankhub.account.application.port.in.CreateAccountUseCase;
import com.bankhub.account.infrastructure.messaging.dto.OnboardingCommandMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnboardingCommandListener {

    private final CreateAccountUseCase createAccountUseCase;

    private static final String COMMAND_TOPIC = "bankhub.account.commands";
    private static final String CONSUMER_GROUP = "account-provisioning-group";

    @KafkaListener(
            topics = COMMAND_TOPIC,
            groupId = CONSUMER_GROUP,
            properties = {"spring.json.value.default.type=com.bankhub.account.infrastructure.messaging.dto.OnboardingCommandMessage"}
    )
    public void handleProvisioningCommand(OnboardingCommandMessage message) {
        try {
            String prospectId = message.payload().customerId();

            log.info("Comando de Provisionamento recebido do Camunda. Prospect ID: {}", prospectId);

            createAccountUseCase.execute(prospectId);

            log.info("Conta gerada com sucesso a partir do comando assíncrono.");
        } catch (Exception e) {
            log.error("Erro fatal ao tentar provisionar conta a partir do Kafka: {}", e.getMessage(), e);
        }
    }
}
