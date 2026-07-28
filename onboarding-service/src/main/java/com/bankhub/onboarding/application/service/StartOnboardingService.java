package com.bankhub.onboarding.application.service;

import com.bankhub.onboarding.application.port.in.StartOnboardingUseCase;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartOnboardingService implements StartOnboardingUseCase {

    private final ZeebeClient zeebeClient;

    private static final String PROCESS_ID = "OnboardingProcess";

    @Override
    public String execute(String documentNumber, BigDecimal monthlyIncome) {
        log.info("Iniciando esteira de Onboarding para um novo prospecto (CPF/CNPJ: {})", documentNumber);

        String generatedCustomerId = UUID.randomUUID().toString();

        Map<String, Object> variables = Map.of(
                "customerId", generatedCustomerId,
                "documentNumber", documentNumber,
                "monthlyIncome", monthlyIncome
        );

        try {
            var event = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId(PROCESS_ID)
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            String protocolNumber = String.valueOf(event.getProcessInstanceKey());
            log.info("Processo de Onboarding iniciado com sucesso! Protocolo: {}, Prospect ID atrelado: {}", protocolNumber, generatedCustomerId);

            return protocolNumber;

        } catch (Exception e) {
            log.error("Falha ao iniciar processo no Camunda para o prospecto [{}]. Motivo: {}", documentNumber, e.getMessage());
            throw new RuntimeException("Não foi possível iniciar a solicitação. Tente novamente.", e);
        }
    }
}
