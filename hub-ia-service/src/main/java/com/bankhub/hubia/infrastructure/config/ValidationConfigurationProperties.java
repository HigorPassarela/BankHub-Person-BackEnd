package com.bankhub.hubia.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propriedades de configuração para validação de alucinações de IA.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bankhub.validation")
public class ValidationConfigurationProperties {

    /**
     * Habilita/desabilita toda a infraestrutura de validação.
     */
    private boolean enabled = true;

    /**
     * Configurações de detecção de alucinação.
     */
    private HallucinationDetection hallucinationDetection = new HallucinationDetection();

    /**
     * Configurações de retry.
     */
    private Retry retry = new Retry();

    /**
     * Configurações de auditoria.
     */
    private Audit audit = new Audit();

    @Data
    public static class HallucinationDetection {
        /**
         * Habilita/desabilita detecção de alucinações.
         */
        private boolean enabled = true;
    }

    @Data
    public static class Retry {
        /**
         * Habilita/desabilita retry automático de ferramentas.
         */
        private boolean enabled = true;

        /**
         * Número máximo de tentativas.
         */
        private int maxAttempts = 3;
    }

    @Data
    public static class Audit {
        /**
         * Habilita/desabilita logging de auditoria.
         */
        private boolean enabled = true;

        /**
         * Habilita/desabilita logging de parâmetros nas mensagens de auditoria.
         */
        private boolean logParameters = true;
    }
}
