package com.bankhub.onboarding.infrastructure.camunda.config;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CamundaDeployerConfig {

    private final ZeebeClient zeebeClient;

    /**
     * Gatilho automático disparado quando a aplicação termina de iniciar.
     * Injeta o arquivo BPMN diretamente no motor do Camunda 8.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void deployedBpmnProcess() {
        try {
            log.info("Iniciando injeção automática do desenho BPMN no cluster Camunda...");

            var event = zeebeClient.newDeployResourceCommand()
                    .addResourceFromClasspath("onboarding-process.bpmn")
                    .send()
                    .join();

            log.info("Deploy concluído com sucesso! Processo(s) injetado(s): {}",
                    event.getProcesses().get(0).getBpmnProcessId());
        } catch (Exception e) {
            log.error("Falha ao injetar o arquivo BPMN no Camunda. Verifique se a sintaxe do arquivo está correta. Erro: {}", e.getMessage());
        }
    }

}
