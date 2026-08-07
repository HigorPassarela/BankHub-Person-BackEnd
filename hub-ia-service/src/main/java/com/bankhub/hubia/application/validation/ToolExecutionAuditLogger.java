package com.bankhub.hubia.application.validation;

import com.bankhub.hubia.domain.ToolExecutionEvent;
import com.bankhub.hubia.infrastructure.config.ValidationConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Logger de auditoria para execuções de ferramentas.
 * Registra logs estruturados e fornece relatórios de auditoria.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolExecutionAuditLogger {

    private final ToolExecutionContext toolExecutionContext;
    private final ValidationConfigurationProperties validationConfig;

    /**
     * Registra um log de auditoria estruturado para uma execução de ferramenta.
     */
    public void logToolExecution(ToolExecutionEvent event) {
        if (event == null) {
            log.warn("[Audit Logger] Tentativa de log com evento nulo");
            return;
        }

        switch (event.getStatus()) {
            case SUCCESS:
                logSuccess(event);
                break;
            case FAILED:
                logFailure(event);
                break;
            case HALLUCINATION:
                logHallucination(event);
                break;
            default:
                log.warn("[Audit Logger] Status desconhecido para evento: {}", event.getStatus());
        }
    }

    /**
     * Registra uma execução bem-sucedida.
     */
    private void logSuccess(ToolExecutionEvent event) {
        if (validationConfig.getAudit().isLogParameters()) {
            log.info("[Audit - SUCCESS] tool={}, account={}, conversation={}, timestamp={}, params={}",
                    event.getToolName(),
                    event.getAccountId(),
                    event.getConversationId(),
                    event.getExecutionTimestamp(),
                    formatParameters(event.getParameters())
            );
        } else {
            log.info("[Audit - SUCCESS] tool={}, account={}, conversation={}, timestamp={}",
                    event.getToolName(),
                    event.getAccountId(),
                    event.getConversationId(),
                    event.getExecutionTimestamp()
            );
        }
    }

    /**
     * Registra uma execução falhada.
     */
    private void logFailure(ToolExecutionEvent event) {
        if (validationConfig.getAudit().isLogParameters()) {
            log.error("[Audit - FAILED] tool={}, account={}, conversation={}, timestamp={}, error={}, params={}",
                    event.getToolName(),
                    event.getAccountId(),
                    event.getConversationId(),
                    event.getExecutionTimestamp(),
                    event.getErrorMessage(),
                    formatParameters(event.getParameters())
            );
        } else {
            log.error("[Audit - FAILED] tool={}, account={}, conversation={}, timestamp={}, error={}",
                    event.getToolName(),
                    event.getAccountId(),
                    event.getConversationId(),
                    event.getExecutionTimestamp(),
                    event.getErrorMessage()
            );
        }
    }

    /**
     * Registra uma alucinação detectada.
     */
    private void logHallucination(ToolExecutionEvent event) {
        log.warn("[Audit - HALLUCINATION] tool={}, account={}, conversation={}, timestamp={}, aiResponse={}",
                event.getToolName(),
                event.getAccountId(),
                event.getConversationId(),
                event.getExecutionTimestamp(),
                truncate(event.getResult(), 200)
        );
    }

    /**
     * Formata os parâmetros de forma segura para log.
     */
    private String formatParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "{}";
        }

        // Mascara dados sensíveis
        return parameters.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    // Mascara campos sensíveis
                    if (isSensitiveField(key)) {
                        return key + "=***MASKED***";
                    }

                    return key + "=" + value;
                })
                .collect(Collectors.joining(", ", "{", "}"));
    }

    /**
     * Verifica se um campo contém dados sensíveis.
     */
    private boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }

        String lowerField = fieldName.toLowerCase();
        return lowerField.contains("password") ||
                lowerField.contains("secret") ||
                lowerField.contains("token") ||
                lowerField.contains("pin") ||
                lowerField.contains("cvv") ||
                lowerField.contains("senha");
    }

    /**
     * Trunca uma string para um tamanho máximo.
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * Gera um relatório de auditoria para a conversa atual.
     */
    public AuditReport generateAuditReport() {
        List<ToolExecutionEvent> events = toolExecutionContext.getAuditLog();

        long successCount = events.stream()
                .filter(e -> e.getStatus() == ToolExecutionEvent.ExecutionStatus.SUCCESS)
                .count();

        long failureCount = events.stream()
                .filter(e -> e.getStatus() == ToolExecutionEvent.ExecutionStatus.FAILED)
                .count();

        long hallucinationCount = events.stream()
                .filter(e -> e.getStatus() == ToolExecutionEvent.ExecutionStatus.HALLUCINATION)
                .count();

        List<String> executedTools = events.stream()
                .filter(e -> e.getStatus() == ToolExecutionEvent.ExecutionStatus.SUCCESS)
                .map(ToolExecutionEvent::getToolName)
                .distinct()
                .collect(Collectors.toList());

        List<String> hallucinatedTools = events.stream()
                .filter(e -> e.getStatus() == ToolExecutionEvent.ExecutionStatus.HALLUCINATION)
                .map(ToolExecutionEvent::getToolName)
                .distinct()
                .collect(Collectors.toList());

        return new AuditReport(
                toolExecutionContext.getConversationId(),
                toolExecutionContext.getAccountId(),
                events.size(),
                successCount,
                failureCount,
                hallucinationCount,
                executedTools,
                hallucinatedTools,
                Instant.now()
        );
    }

    /**
     * Relatório de auditoria contendo estatísticas de execução.
     */
    public static class AuditReport {
        public final String conversationId;
        public final String accountId;
        public final int totalEvents;
        public final long successCount;
        public final long failureCount;
        public final long hallucinationCount;
        public final List<String> executedTools;
        public final List<String> hallucinatedTools;
        public final Instant generatedAt;

        public AuditReport(
                String conversationId,
                String accountId,
                int totalEvents,
                long successCount,
                long failureCount,
                long hallucinationCount,
                List<String> executedTools,
                List<String> hallucinatedTools,
                Instant generatedAt
        ) {
            this.conversationId = conversationId;
            this.accountId = accountId;
            this.totalEvents = totalEvents;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.hallucinationCount = hallucinationCount;
            this.executedTools = executedTools;
            this.hallucinatedTools = hallucinatedTools;
            this.generatedAt = generatedAt;
        }

        @Override
        public String toString() {
            return String.format(
                    "AuditReport{conversation=%s, account=%s, total=%d, success=%d, failed=%d, hallucinations=%d, executedTools=%s, hallucinatedTools=%s}",
                    conversationId, accountId, totalEvents, successCount, failureCount, hallucinationCount,
                    executedTools, hallucinatedTools
            );
        }
    }

    /**
     * Registra o relatório de auditoria completo ao final da conversa.
     */
    public void logConversationSummary() {
        AuditReport report = generateAuditReport();

        log.info("[Audit Summary] {}", report);

        if (report.hallucinationCount > 0) {
            log.warn("[Audit Summary] ATENÇÃO: {} alucinações detectadas nesta conversa. Ferramentas: {}",
                    report.hallucinationCount, report.hallucinatedTools);
        }
    }
}
