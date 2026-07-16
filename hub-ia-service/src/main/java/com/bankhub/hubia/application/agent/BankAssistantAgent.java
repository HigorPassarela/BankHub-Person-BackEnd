package com.bankhub.hubia.application.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Interface proxy que define o comportamento do Assistente Virtual de IA.
 */
public interface BankAssistantAgent {

    /**
     * Envia uma mensagem para o modelo de Inteligência Artificial.
     *
     * @param chatId ID único da sessão do chat (usado para recuperar a memória no Redis).
     * @param accountId ID da conta (injetado no prompt para a IA usar nas Tools).
     * @param customerId ID do cliente (injetado no prompt para a IA usar nas Tools).
     * @param userMessage A mensagem sanitizada do usuário.
     * @return A resposta gerada pelo LLM.
     */
    @SystemMessage({
            "Você é o BankAssist, o assistente virtual oficial do Bank-Hub.",
            "Sua missão é ajudar os clientes com dúvidas, consultar saldos e orientá-los de forma educada e concisa.",
            "NUNCA invente dados. Se você não souber a resposta, peça desculpas e diga que não encontrou a informação.",
            "Use as ferramentas (Tools) disponíveis para obter dados reais quando o cliente perguntar sobre seu saldo.",
            "O ID da conta do cliente que está falando com você é {{accountId}} e o ID do cliente é {{customerId}}.",
            "Use essas variáveis EXATAS sempre que precisar acionar uma ferramenta que exija esses parâmetros."
    })
    String chat(
            @MemoryId String chatId,
            @V("AccountId") String accountId,
            @V("customerId") String customerId,
            @UserMessage String userMessage
    );
}
