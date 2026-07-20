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
    public void provisionAccount(@Variable(name = "customerId") String customerId) {
        log.info("Camunda Worker acionado: Provisionando conta bancária para o cliente [{}]", customerId);

        Map<String, Object> commandMessage = Map.of(
                "header", Map.of(
                        "correlationId", UUID.randomUUID().toString(),
                        "eventType", "CREATE_ACCOUNT_COMMAND",
                        "timestamp", LocalDateTime.now().toString()
                ),
                "payload", Map.of(
                        "customerId", customerId
                )
        );

        kafkaTemplate.send(TOPIC, customerId, commandMessage);

        log.info("Comando de criação de conta enviado ao Kafka com sucesso. Cliente [{}]", customerId);
    }

    /**
     * Fallback acionado pelo Resilience4j em caso de indisponibilidade severa do broker Kafka.
     */
    public void fallbackProvisioning(String customerId, Exception ex) {
        log.error("CRÍTICO: Falha ao enviar comando para o Kafka. Cliente [{}]. Motivo: {}",
                customerId, ex.getMessage());

        // Lançar exceção propaga o erro para o Camunda, que criará um Incidente
        // visível no Zeebe Operate para intervenção manual (ou retentativa via painel).
        throw new RuntimeException("Falha de comunicação com o broker Kafka durante provisionamento.", ex);
    }
}
