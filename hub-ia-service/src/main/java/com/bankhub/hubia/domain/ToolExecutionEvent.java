package com.bankhub.hubia.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Representa um evento de execução de ferramenta (Tool).
 * Usado para rastrear todas as invocações de ferramentas LLM e detectar alucinações.
 */
@Data
@Builder
public class ToolExecutionEvent {

    private String toolName;
    private Map<String, Object> parameters;
    private Instant executionTimestamp;
    private ExecutionStatus status;
    private String result;
    private String errorMessage;
    private String accountId;
    private String conversationId;

    public enum ExecutionStatus {
        SUCCESS,
        FAILED,
        HALLUCINATION
    }

    /**
     * Cria um evento de execução bem-sucedida.
     */
    public static ToolExecutionEvent success(String toolName, Map<String, Object> parameters, String result, String accountId, String conversationId) {
        return ToolExecutionEvent.builder()
                .toolName(toolName)
                .parameters(parameters)
                .executionTimestamp(Instant.now())
                .status(ExecutionStatus.SUCCESS)
                .result(result)
                .accountId(accountId)
                .conversationId(conversationId)
                .build();
    }

    /**
     * Cria um evento de execução falhada.
     */
    public static ToolExecutionEvent failed(String toolName, Map<String, Object> parameters, String errorMessage, String accountId, String conversationId) {
        return ToolExecutionEvent.builder()
                .toolName(toolName)
                .parameters(parameters)
                .executionTimestamp(Instant.now())
                .status(ExecutionStatus.FAILED)
                .errorMessage(errorMessage)
                .accountId(accountId)
                .conversationId(conversationId)
                .build();
    }

    /**
     * Cria um evento de alucinação detectada.
     */
    public static ToolExecutionEvent hallucination(String toolName, String responseExcerpt, String accountId, String conversationId) {
        return ToolExecutionEvent.builder()
                .toolName(toolName)
                .executionTimestamp(Instant.now())
                .status(ExecutionStatus.HALLUCINATION)
                .result(responseExcerpt)
                .accountId(accountId)
                .conversationId(conversationId)
                .build();
    }
}
