package com.bankhub.onboarding.infrastructure.camunda.worker;

import com.bankhub.onboarding.domain.strategy.CustomerContext;
import com.bankhub.onboarding.domain.strategy.RiskCalculationStrategy;
import com.bankhub.onboarding.domain.strategy.RiskResult;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiskAnalysisWorker {

    private final RiskCalculationStrategy riskStrategy;

    /**
     * @param customerId ID do cliente injetado pelo processo BPMN.
     * @param documentNumber CPF do cliente injetado pelo processo.
     * @param monthlyIncome Renda mensal injetada pelo processo.
     * @return Variáveis que serão devolvidas ao fluxo do Camunda.
     */
    @JobWorker(type = "analyze-risk", autoComplete = true)
    public Map<String, Object> analyzeRisk(
            @Variable(name = "customerId") String customerId,
            @Variable(name = "documentNumber") String documentNumber,
            @Variable(name = "monthlyIncome") BigDecimal monthlyIncome) {

        log.info("Camunda Worker acionado: Iniciando análise de risco para o cliente [{}]", customerId);

        CustomerContext context = new CustomerContext(customerId, documentNumber, monthlyIncome);

        RiskResult result = riskStrategy.evaluate(context);

        log.info("Análise concluída para o cliente [{}]. Aprovado: {}, Risco: {}",
                customerId, result.approved(), result.riskLevel());

        return Map.of(
                "isApproved", result.approved(),
                "riskLevel", result.riskLevel(),
                "riskReason", result.reason()
        );
    }
}
