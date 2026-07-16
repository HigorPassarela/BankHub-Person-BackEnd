package com.bankhub.onboarding.application.service;

import com.bankhub.onboarding.application.port.in.StartOnboardingUseCase;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartOnboardingService implements StartOnboardingUseCase {

    private final ZeebeClient zeebeClient;

    private static final String PROCESS_ID = "OnboardingProcess";

    @Override
    public String execute(String customerId, String documentNumber, BigDecimal monthlyIncome) {
        log.info("Iniciando esteira de Onboarding para o cliente [{}]", customerId);

        Map<String, Object> variables = Map.of(
                "customerId", customerId,
                "documentNumber", documentNumber,
                "monthlyIncome", monthlyIncome
        );

        try {
            ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId(PROCESS_ID)
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            String protocolNumber = String.valueOf(event.getProcessInstanceKey());
            log.info("Processo iniciado com sucesso! Protocolo: {}", protocolNumber);

            return protocolNumber;
        } catch (Exception e) {
            log.error("Falha ao iniciar processo no Camunda para o cliente [{}]. Motivo: {}", customerId, e.getMessage());
            throw new RuntimeException("Não foi possível iniciar o processo de abertura de conta. Tente novamente mais tarde.", e);
        }
    }
}
