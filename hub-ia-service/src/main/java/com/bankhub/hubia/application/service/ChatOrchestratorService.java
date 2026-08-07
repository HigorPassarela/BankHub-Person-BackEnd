package com.bankhub.hubia.application.service;

import com.bankhub.hubia.application.agent.BankAssistantAgent;
import com.bankhub.hubia.application.graph.node.AccountTools;
import com.bankhub.hubia.application.graph.node.SanitizerNode;
import com.bankhub.hubia.application.graph.node.SecurityCheckNode;
import com.bankhub.hubia.application.validation.ToolExecutionAuditLogger;
import com.bankhub.hubia.application.validation.ToolExecutionContext;
import com.bankhub.hubia.application.validation.ToolExecutionValidator;
import com.bankhub.hubia.infrastructure.config.ValidationConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatOrchestratorService {

    private final SecurityCheckNode securityCheckNode;
    private final SanitizerNode sanitizerNode;
    private final BankAssistantAgent bankAssistantAgent;
    private final AccountTools accountTools;
    private final ToolExecutionContext toolExecutionContext;
    private final ToolExecutionValidator toolExecutionValidator;
    private final ToolExecutionAuditLogger toolExecutionAuditLogger;
    private final ValidationConfigurationProperties validationConfig;

    public String processChat(String accountId, String customerId, String rawMessage) {
        // Gera ID único para esta conversa
        String conversationId = UUID.randomUUID().toString();

        log.info("Iniciando orquestração de IA para a Conta: {} (Conversa: {})", accountId, conversationId);

        try {
            // Inicializa contexto de execução de ferramentas (se validação habilitada)
            if (validationConfig.isEnabled()) {
                toolExecutionContext.initialize(conversationId, accountId);
                log.debug("[Validation] Contexto de execução inicializado para conversa {}", conversationId);
            }

            securityCheckNode.execute(accountId);
            String sanitizedMessage = sanitizerNode.execute(rawMessage);

            log.debug("Buscando dados da conta no Account Service...");
            String accountContext = accountTools.getAccountData(accountId, customerId);

            log.debug("Enviando prompt com contexto injetado para o LLM...");
            String aiResponse = bankAssistantAgent.chat(accountId, accountId, customerId, accountContext, sanitizedMessage);

            // Valida a resposta da IA antes de retornar ao usuário (se validação habilitada)
            if (validationConfig.isEnabled() && validationConfig.getHallucinationDetection().isEnabled()) {
                log.debug("[Validation] Validando resposta da IA...");
                ToolExecutionValidator.ValidationResult validationResult = toolExecutionValidator.validateResponse(aiResponse);

                if (!validationResult.isValid) {
                    // Alucinação detectada - retorna mensagem de fallback
                    log.warn("[Validation] Alucinação detectada! Retornando mensagem de fallback ao usuário.");
                    String fallbackMessage = toolExecutionValidator.generateFallbackMessage(validationResult.hallucinatedToolName);

                    log.info("Processamento de IA concluído com fallback (alucinação detectada).");
                    return fallbackMessage;
                }
            }

            log.info("Processamento de IA concluído com sucesso.");
            return aiResponse;

        } finally {
            // Registra relatório de auditoria e limpa contexto (se validação habilitada)
            if (validationConfig.isEnabled()) {
                if (validationConfig.getAudit().isEnabled()) {
                    try {
                        toolExecutionAuditLogger.logConversationSummary();
                    } catch (Exception e) {
                        log.error("[Audit] Erro ao gerar relatório de auditoria: {}", e.getMessage());
                    }
                }

                toolExecutionContext.clear();
                log.debug("[Validation] Contexto de execução limpo para conversa {}", conversationId);
            }
        }
    }
}