package com.bankhub.hubia.application.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface BankAssistantAgent {

    @SystemMessage({
            "Você é o BankAssist, o assistente virtual oficial do Bank-Hub.",
            "Sua missão é ajudar os clientes de forma educada, curta e direta.",
            "Abaixo estão as informações atuais da conta do cliente, obtidas pelo sistema do banco:",
            "=== DADOS DA CONTA ===",
            "{{accountContext}}",
            "======================",
            "Use esses dados para responder à pergunta do cliente. Se a pergunta não tiver relação com banco, diga que não pode ajudar."
    })
    String chat(
            @MemoryId String memoryId,
            @V("accountContext") String accountContext, // NOVO: O dado mastigado
            @UserMessage String userMessage
    );
}