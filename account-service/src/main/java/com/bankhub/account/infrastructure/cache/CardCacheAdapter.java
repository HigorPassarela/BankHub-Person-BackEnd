package com.bankhub.account.infrastructure.cache;

import com.bankhub.account.application.port.out.CardCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardCacheAdapter implements CardCachePort {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "card:temp:";
    private static final int EXPIRATION_HOURS = 24;

    @Override
    public void registerTemporaryCard(String cardId) {
        String redisKey = PREFIX + cardId;
        log.info("Registrando Timer de 24h no Redis para o Cartão Temporário: {}", cardId);

        redisTemplate.opsForValue().set(redisKey, "ACTIVE", Duration.ofHours(EXPIRATION_HOURS));
    }

    @Override
    public boolean isTemporaryCardValid(String cardId) {
        String redisKey = PREFIX + cardId;
        boolean exists = redisTemplate.hasKey(redisKey);

        if (Boolean.TRUE.equals(exists)) {
            return true;
        } else {
            log.warn("Cartão Virtual Temporário {} expirou e foi expurgado da memória.", cardId);
            return false;
        }
    }

    @Override
    public void removeTemporaryCard(String cardId) {
        String redisKey = PREFIX + cardId;
        log.info("Removendo Cartão Temporário do Redis manualmente: {}", cardId);
        redisTemplate.delete(redisKey);
    }
}
