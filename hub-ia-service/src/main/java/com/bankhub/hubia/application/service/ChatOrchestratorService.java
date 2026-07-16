package com.bankhub.hubia.application.service;

import com.bankhub.hubia.application.agent.BankAssistantAgent;
import com.bankhub.hubia.application.graph.node.SanitizerNode;
import com.bankhub.hubia.application.graph.node.SecurityCheckNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatOrchestratorService {

    private final SecurityCheckNode securityCheckNode;
    private final SanitizerNode sanitizerNode;
    private final BankAssistantAgent bankAssistantAgent;

    /**
     * Orquestra a requisição do usuário passando por todas as camadas de segurança antes do LLM.
     *
     * @param accountId ID da conta (extraído do Token).
     * @param customerId ID do cliente (extraído do Token).
     * @param rawMessage Mensagem original enviada pelo usuário na requisição HTTP.
     * @return Resposta gerada pela Inteligência Artificial.
     */
    public String processChat(String accountId, String customerId, String rawMessage) {
        log.info("Iniciando orquestração de IA para a Conta: {}", accountId);

        securityCheckNode.execute(accountId);

        String sanitizedMessage = sanitizerNode.execute(rawMessage);

        log.debug("Enviando prompt sanitizado para o LLM responder...");

        String aiResponse = bankAssistantAgent.chat(accountId, accountId, customerId, sanitizedMessage);

        log.info("Processamento de IA concluído com sucesso para a Conta: {}", accountId);

        return aiResponse;
    }
}
