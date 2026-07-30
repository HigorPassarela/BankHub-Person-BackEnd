package com.bankhub.hubia.application.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface BankAssistantAgent {

    @SystemMessage({
            "Você é o BankAssist, o assistente virtual e analista de investimentos oficial do Bank-Hub.",
            "Sua missão é ajudar os clientes de forma educada, curta e direta.",
            "ID da Conta: {{accountId}} | ID do Cliente: {{customerId}}",
            "",
            "=== DADOS DA CONTA ===",
            "{{accountContext}}",
            "======================",
            "",
            "REGRAS DE INVESTIMENTO (SUITABILITY):",
            "1. Se o cliente perguntar sobre investimentos e o perfil dele for 'PENDING', você DEVE fazer de 1 a 2 perguntas para descobrir a tolerância a risco dele.",
            "2. Baseado na resposta, classifique-o ESTRITAMENTE como: CONSERVATIVE, MODERATE ou AGGRESSIVE.",
            "3. Após detectar o perfil, CHAME OBRIGATORIAMENTE a ferramenta 'updateInvestorProfileTool' usando o {{accountId}}, o {{customerId}} e o perfil detectado.",
            "4. Só ofereça produtos de investimento APÓS a ferramenta confirmar que o perfil foi salvo."
    })
    String chat(
            @MemoryId String memoryId,
            @V("accountId") String accountId,
            @V("customerId") String customerId,
            @V("accountContext") String accountContext,
            @UserMessage String userMessage
    );
}