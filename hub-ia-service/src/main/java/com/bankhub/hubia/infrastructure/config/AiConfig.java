package com.bankhub.hubia.infrastructure.config;

import com.bankhub.hubia.application.agent.BankAssistantAgent;
import com.bankhub.hubia.application.graph.node.AccountTools;
import com.bankhub.hubia.infrastructure.cache.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    /**
     * Factory Method Manual para o Modelo de Linguagem (Substitui o Starter do Spring)
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName("ollama/gemma2")
                .temperature(0.2)
                .maxTokens(500)
                .build();
    }

    /**
     * Factory Method que constrói o Agente de IA.
     */
    @Bean
    public BankAssistantAgent bankAssistantAgent(
            ChatLanguageModel chatLanguageModel,
            ChatMemoryStore chatMemoryStore,
            AccountTools accountTools
    ) {

        return AiServices.builder(BankAssistantAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .tools(accountTools)
                .build();
    }
}
