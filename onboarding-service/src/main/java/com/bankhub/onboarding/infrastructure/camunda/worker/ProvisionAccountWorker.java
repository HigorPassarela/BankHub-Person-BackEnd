package com.bankhub.onboarding.infrastructure.camunda.worker;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProvisionAccountWorker {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "bankhub.account.commands";

    /**
     * @param customerId ID do cliente injetado pelo processo BPMN após a aprovação.
     */
    @JobWorker(type = "provision-account", autoComplete = true)
    @Retry(name = "kafkaRetry", fallbackMethod = "fallbackProvisioning")
    public void provisionAccount(
            @Variable(name = "customerId") String customerId,
            @Variable(name = "fullName") String fullName,
            @Variable(name = "phone") String phone,
            @Variable(name = "address") String address
    ) {
        log.info("Camunda Worker: Provisionando conta para [{}]", fullName);

        Map<String, Object> commandMessage = Map.of(
                "header", Map.of(
                        "correlationId", UUID.randomUUID().toString(),
                        "eventType", "CREATE_ACCOUNT_COMMAND",
                        "timestamp", LocalDateTime.now().toString()
                ),
                "payload", Map.of(
                        "customerId", customerId,
                        "fullName", fullName,
                        "phone", phone,
                        "address", address
                )
        );

        kafkaTemplate.send(TOPIC, customerId, commandMessage);
        log.info("Comando de criação de conta enviado ao Kafka. Cliente [{}]", customerId);
    }

    public void fallbackProvisioning(String customerId, String fullName, String phone, String address, Exception ex) {
        log.error("CRÍTICO: Falha ao enviar comando para o Kafka. Cliente [{}].", customerId, ex);
        throw new RuntimeException("Falha de comunicação com o broker Kafka.", ex);
    }
}
