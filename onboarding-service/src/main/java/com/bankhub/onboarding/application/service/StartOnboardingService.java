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
    public String execute(String documentNumber, BigDecimal monthlyIncome, String fullName, String phone, String address) {
        log.info("Iniciando esteira de Onboarding para um novo prospecto (CPF/CNPJ: {})", documentNumber);

        String generatedCustomerId = UUID.randomUUID().toString();

        Map<String, Object> variables = Map.of(
                "customerId", generatedCustomerId,
                "documentNumber", documentNumber,
                "monthlyIncome", monthlyIncome,
                "fullName", fullName,
                "phone", phone,
                "address", address
        );

        try {
            var event = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId(PROCESS_ID)
                    .latestVersion()
                    .variables(variables)
                    .send().join();

            return String.valueOf(event.getProcessInstanceKey());
        } catch (Exception e) {
            throw new RuntimeException("Falha ao iniciar processo no Camunda.", e);
        }
    }
}
