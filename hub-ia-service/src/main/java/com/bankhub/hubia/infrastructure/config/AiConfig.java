package com.bankhub.hubia.infrastructure.config;

import com.bankhub.hubia.application.agent.BankAssistantAgent;
import com.bankhub.hubia.application.graph.node.AccountTools;
import com.bankhub.hubia.infrastructure.cache.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /**
     * Factory Method que constrói o Agente de IA.
     * O Spring injeta automaticamente o ChatLanguageModel, o nosso CustomRedisChatMemoryStore e as Tools.
     */
    @Bean
    public BankAssistantAgent bankAssistantAgent(
            ChatLanguageModel chatLanguageModel,
            ChatMemoryStore chatMemoryStore,
            AccountTools accountTools) {

        return AiServices.builder(BankAssistantAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                // Injeta as ferramentas Tools e os CircuitBreakers
                .tools(accountTools)
                .build();
    }
}
