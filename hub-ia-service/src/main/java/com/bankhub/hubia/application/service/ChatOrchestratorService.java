package com.bankhub.hubia.application.service;

import com.bankhub.hubia.application.agent.BankAssistantAgent;
import com.bankhub.hubia.application.graph.node.AccountTools;
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
    private final AccountTools accountTools;

    public String processChat(String accountId, String customerId, String rawMessage) {
        log.info("Iniciando orquestração de IA para a Conta: {}", accountId);

        securityCheckNode.execute(accountId);
        String sanitizedMessage = sanitizerNode.execute(rawMessage);

        log.debug("Buscando dados da conta no Account Service...");
        String accountContext = accountTools.getAccountData(accountId, customerId);

        log.debug("Enviando prompt com contexto injetado para o LLM...");
        String aiResponse = bankAssistantAgent.chat(accountId, accountContext, sanitizedMessage);

        log.info("Processamento de IA concluído.");
        return aiResponse;
    }
}