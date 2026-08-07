package com.bankhub.hubia.application.validation;

import com.bankhub.hubia.domain.ToolExecutionEvent;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contexto de execução de ferramentas por conversa.
 * Rastreia quais ferramentas foram efetivamente executadas durante uma interação com a IA.
 *
 * Escopo: Prototype com proxy para permitir injeção em singletons.
 * Cada conversa deve obter uma nova instância deste contexto.
 */
@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ToolExecutionContext {

    @Getter
    private final Set<String> executedTools = ConcurrentHashMap.newKeySet();

    @Getter
    private final List<ToolExecutionEvent> auditLog = Collections.synchronizedList(new ArrayList<>());

    private String conversationId;
    private String accountId;

    /**
     * Inicializa o contexto para uma nova conversa.
     */
    public void initialize(String conversationId, String accountId) {
        this.conversationId = conversationId;
        this.accountId = accountId;
        this.executedTools.clear();
        this.auditLog.clear();
    }

    /**
     * Registra que uma ferramenta foi executada.
     */
    public void recordExecution(String toolName) {
        executedTools.add(toolName);
    }

    /**
     * Adiciona um evento de auditoria.
     */
    public void addAuditEvent(ToolExecutionEvent event) {
        auditLog.add(event);
    }

    /**
     * Verifica se uma ferramenta foi executada nesta conversa.
     */
    public boolean wasExecuted(String toolName) {
        return executedTools.contains(toolName);
    }

    /**
     * Limpa o contexto (deve ser chamado ao final da conversa).
     */
    public void clear() {
        executedTools.clear();
        auditLog.clear();
        conversationId = null;
        accountId = null;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getAccountId() {
        return accountId;
    }
}
