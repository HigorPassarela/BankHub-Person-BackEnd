package com.bankhub.hubia.application.validation;

import com.bankhub.hubia.domain.ToolExecutionEvent;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect que intercepta todas as execuções de ferramentas (@Tool) do LangChain4j.
 * Registra as execuções no ToolExecutionContext para permitir validação de alucinações.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ToolExecutionAspect {

    private final ToolExecutionContext toolExecutionContext;

    /**
     * Intercepta todas as chamadas a métodos anotados com @Tool.
     * Registra a execução antes e depois da invocação.
     */
    @Around("@annotation(tool)")
    public Object trackToolExecution(ProceedingJoinPoint joinPoint, Tool tool) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String toolName = method.getName();

        // Extrai parâmetros do método
        Map<String, Object> parameters = extractParameters(joinPoint);

        log.info("[Tool Execution Tracker] Iniciando execução da ferramenta: {}", toolName);

        Instant start = Instant.now();
        Object result = null;
        Throwable executionError = null;

        try {
            // Executa o método da ferramenta
            result = joinPoint.proceed();

            // Registra execução bem-sucedida
            toolExecutionContext.recordExecution(toolName);

            ToolExecutionEvent event = ToolExecutionEvent.success(
                    toolName,
                    parameters,
                    result != null ? result.toString() : "null",
                    toolExecutionContext.getAccountId(),
                    toolExecutionContext.getConversationId()
            );
            toolExecutionContext.addAuditEvent(event);

            log.info("[Tool Execution Tracker] Ferramenta {} executada com sucesso em {}ms",
                    toolName, java.time.Duration.between(start, Instant.now()).toMillis());

            return result;

        } catch (Throwable throwable) {
            executionError = throwable;

            // Registra execução falhada
            ToolExecutionEvent event = ToolExecutionEvent.failed(
                    toolName,
                    parameters,
                    throwable.getMessage(),
                    toolExecutionContext.getAccountId(),
                    toolExecutionContext.getConversationId()
            );
            toolExecutionContext.addAuditEvent(event);

            log.error("[Tool Execution Tracker] Ferramenta {} falhou após {}ms: {}",
                    toolName, java.time.Duration.between(start, Instant.now()).toMillis(), throwable.getMessage());

            throw throwable;
        }
    }

    /**
     * Extrai os parâmetros do método interceptado.
     */
    private Map<String, Object> extractParameters(ProceedingJoinPoint joinPoint) {
        Map<String, Object> params = new HashMap<>();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            params.put(parameterNames[i], parameterValues[i]);
        }

        return params;
    }
}
