package com.bankhub.hubia.infrastructure.cache;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate redisTemplate;
    private static final String REDIS_KEY_PREFIX = "chat:memory:";

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + memoryId);

        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        return ChatMessageDeserializer.messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // Transforma a lista de mensagens para JSON
        String json = ChatMessageSerializer.messagesToJson(messages);

        // Salva no redis com time to live de 1 hora para não sobrecarregar
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + memoryId, json, Duration.ofHours(1));

        log.debug("Memória do chat {} atualizada no Redis.", memoryId);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redisTemplate.delete(REDIS_KEY_PREFIX + memoryId);
    }
}
